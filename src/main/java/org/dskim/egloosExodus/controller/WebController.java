package org.dskim.egloosExodus.controller;

import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.dskim.egloosExodus.repository.BlogRepository;
import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

@Controller
public class WebController {
    private static Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    BlogDownloaderManager blogDownloaderManager;

    @Autowired
    HugoDelegator hugo;

    @Autowired
    EgloosBlogDownloader egloosBlogDownloader;

    @Value("${blog.durationMin}")
    long blogDurationMin;

    @Value("${version}")
    String version;

    @Value(("${blog.rootDir}"))
    String rootDirPath;

    @Autowired
    BlogRepository blogRepo;

    @Value("${maxPostCount}")
    int maxPostCount;

    @RequestMapping("")
    public String index(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        //logger.debug("blogDownloaderManager.getCurrentBlog()={}", blogDownloaderManager.getCurrentBlog());

        model.addAttribute("name", name);
        model.addAttribute("currentBlog", blogDownloaderManager.getCurrentBlog());
        model.addAttribute("usableSpace", new File("/").getUsableSpace() /1024 /1024 /1024);

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "regDate"));
        model.addAttribute("waitingList", blogRepo.findAllByIsDownloaded(false, pageable));
        model.addAttribute("version", version);
        model.addAttribute("maxPostCount", maxPostCount);
        model.addAttribute("Integer_MAX_VALUE", Integer.MAX_VALUE);

        return "index";
    }

    /**
     * @deprecated
     * 바로 다룬로드 하는거 지원하지 않습니다.
     *
     * @param blog
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("download")
    public ModelAndView download(Blog blog, Model model) throws Exception {
        if(blog.getBlogBaseUrl().indexOf("http://") < 0) {
            blog.setBlogBaseUrl("http://" + blog.getBlogBaseUrl());
        }
        logger.debug("blog={}", blog);
        logger.debug("blogDownloaderManager.getCurrentBlog()={}", blogDownloaderManager.getCurrentBlog());

        blogDownloaderManager.downloadBlog(blog);

        model.addAttribute("currentBlog", blog);
        return new ModelAndView("redirect:/");
    }

    /**
     * 미리보기
     *
     * @deprecated
     * @param blog
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("preview")
    public ModelAndView preview(Blog blog, @RequestParam(defaultValue = "15") int maxPostCount, Model model) throws Exception {
        if(blog.getBlogBaseUrl().indexOf("http://") < 0) {
            blog.setBlogBaseUrl("http://" + blog.getBlogBaseUrl());
        }
        blog.setUserId(StringUtils.substringBetween(blog.getBlogBaseUrl(), "://", "."));
        blog.setServiceName(StringUtils.substringBetween(blog.getBlogBaseUrl(), blog.getUserId() + ".", ".com"));
        logger.debug("previewBlog={}", blog);

        hugo.init(blog);
        egloosBlogDownloader.downLoadBlog(hugo, blog, true, maxPostCount);
        hugo.generateStaticFles(blog);

        model.addAttribute("previewBlog", blog);
        return new ModelAndView("forward:/");
    }

    @PostMapping("queue")
    public ModelAndView addToDownloadQueue(Blog blog, Model model) throws Exception {
        if(blog.getBlogBaseUrl().indexOf("http://") < 0) {
            blog.setBlogBaseUrl("http://" + blog.getBlogBaseUrl());
        }
        blog.setUserId(StringUtils.substringBetween(blog.getBlogBaseUrl(), "://", "."));
        blog.setServiceName(StringUtils.substringBetween(blog.getBlogBaseUrl(), blog.getUserId() + ".", ".com"));
        logger.debug("blog={}", blog);
        model.addAttribute("addedBlog", blog);

        long alreadyRegisteredBlogCount = blogRepo.countByUserIdAndServiceName(blog.getUserId(), blog.getServiceName());
        model.addAttribute("alreadyRegisteredBlogCount", alreadyRegisteredBlogCount);

        if(alreadyRegisteredBlogCount == 0) {
            blog = blogRepo.save(blog);
        } else {
            blog = blogRepo.findByUserIdAndServiceName(blog.getUserId(), blog.getServiceName());
        }

        return new ModelAndView("forward:/");
    }

    /**
     * http://localhost:8080/ee/deleteOldBlog?blogDurationMin=1440
     *
     * @return
     * @throws Exception
     */
    @GetMapping("deleteOldBlog")
    @ResponseBody
    @PostConstruct  // 기동하자마자 무조건 한번 돈다
    @Scheduled(fixedDelay = 3600000)    // 한시간에 한번씩
    public JSONObject deleteOldBlog(
            //@RequestParam(value="blogDurationMin", required=false) Long blogDurationMin
        ) throws Exception {
        //if(blogDurationMin == null) blogDurationMin = new Long(this.blogDurationMin);
        DateTime limitDateTime = new DateTime(DateTimeUtils.currentTimeMillis() - blogDurationMin * 60 * 1000);
        logger.debug("blogDurationMin={}, limitDateTime={}", blogDurationMin, limitDateTime);

        int deletedCount = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        File rootDir = new File(rootDirPath);
        LinkedList<File> tgzFiles = (LinkedList<File>)FileUtils.listFiles(rootDir, new WildcardFileFilter("*.tgz"), null);
        for (File tgzFile : tgzFiles) {
            logger.debug("tgzFile={}, lastModified={}, date={}, deleted?={}", tgzFile.getName(), tgzFile.lastModified(), sdf.format(tgzFile.lastModified()), limitDateTime.isAfter(tgzFile.lastModified()));
            if(limitDateTime.isAfter(tgzFile.lastModified())) {
                tgzFile.delete();
                deletedCount++;
                logger.debug(" \t deletedCount={}", deletedCount);
            }
        }

        File[] blogDirs = rootDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        for (File blogDir : blogDirs) {
            logger.debug("blogDir={}, lastModified={}, date={}, deleted?={}", blogDir.getName(), blogDir.lastModified(), sdf.format(blogDir.lastModified()), limitDateTime.isAfter(blogDir.lastModified()));
            if(limitDateTime.isAfter(blogDir.lastModified())) {
                FileUtils.deleteDirectory(blogDir);//
                deletedCount++;
                logger.debug(" \t deletedCount={}", deletedCount);
            }
        }

        JSONObject deleteOldBlogRtn = new JSONObject();
        deleteOldBlogRtn.put("deletedCount", deletedCount);
        deleteOldBlogRtn.put("blogDurationMin", blogDurationMin);
        deleteOldBlogRtn.put("limitDateTime", limitDateTime.toDate());
        logger.debug("deleteOldBlogRtn={}", deleteOldBlogRtn.toJSONString());
        return deleteOldBlogRtn;
    }
}
