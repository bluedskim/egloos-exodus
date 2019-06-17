package org.dskim.egloosExodus.controller;

import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
    EgloosBlogDownloader egloosBlogDownloader;

    @Value("${blog.durationMin}")
    long blogDurationMin;

    @Value(("${blog.rootDir}"))
    String rootDirPath;

    @Autowired
    ObjectRepository<Blog> downloadQueueRepo;

    @GetMapping("")
    public String index(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        //logger.debug("blogDownloaderManager.getCurrentBlog()={}", blogDownloaderManager.getCurrentBlog());

        model.addAttribute("name", name);
        model.addAttribute("currentBlog", blogDownloaderManager.getCurrentBlog());

        Cursor<Blog> waitingList = downloadQueueRepo.find();
        logger.debug("waitingList.size()={}", waitingList.size());
        model.addAttribute("waitingList", waitingList.toList());
        return "index";
    }

    /**
     * 바로 다룬로드 하는거 지원하지 않습니다.
     * @deprecated
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

    @PostMapping("queue")
    public ModelAndView addToDownloadQueue(Blog blog, Model model) throws Exception {
        if(blog.getBlogBaseUrl().indexOf("http://") < 0) {
            blog.setBlogBaseUrl("http://" + blog.getBlogBaseUrl());
        }
        blog.setUserId(StringUtils.substringBetween(blog.getBlogBaseUrl(), "://", "."));
        logger.debug("blog={}", blog);

        downloadQueueRepo.insert(blog);
        Cursor<Blog> waitingList = downloadQueueRepo.find();
        logger.debug("waitingList.size()={}", waitingList.size());
        model.addAttribute("waitingList", waitingList.toList());

        model.addAttribute("currentBlog", blog);
        return new ModelAndView("redirect:/");
    }

    /**
     * http://localhost:8080/ee/deleteOldBlog?blogDurationMin=1440
     *
     * @return
     * @throws Exception
     */
    @GetMapping("deleteOldBlog")
    @ResponseBody
    @Scheduled(fixedDelay = 3600000, initialDelay = 3600000)    // 한시간에 한번씩
    public JSONObject deleteOldBlog(
            //@RequestParam(value="blogDurationMin", required=false) Long blogDurationMin
        ) throws Exception {
        //if(blogDurationMin == null) blogDurationMin = new Long(this.blogDurationMin);
        DateTime limitDateTime = new DateTime(DateTimeUtils.currentTimeMillis() - blogDurationMin * 60 * 1000);
        logger.debug("blogDurationMin={}, limitDateTime={}", blogDurationMin, limitDateTime);

        int deletedCount = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        boolean deleted = false;
        File rootDir = new File(rootDirPath);
        LinkedList<File> tgzFiles = (LinkedList<File>)FileUtils.listFiles(rootDir, new WildcardFileFilter("*.tgz"), null);
        for (File tgzFile : tgzFiles) {
            logger.debug("tgzFile={}, lastModified={}, date={}, deleted?={}", tgzFile.getName(), tgzFile.lastModified(), sdf.format(tgzFile.lastModified()), limitDateTime.isAfter(tgzFile.lastModified()));
            if(limitDateTime.isAfter(tgzFile.lastModified())) {
                FileUtils.deleteDirectory(tgzFile);
                logger.debug(" \t deleted={}", deleted);
            }
        }

        File[] blogDirs = rootDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        for (File blogDir : blogDirs) {
            logger.debug("blogDir={}, lastModified={}, date={}, deleted?={}", blogDir.getName(), blogDir.lastModified(), sdf.format(blogDir.lastModified()), limitDateTime.isAfter(blogDir.lastModified()));
            if(limitDateTime.isAfter(blogDir.lastModified())) {
                FileUtils.deleteDirectory(blogDir);//
                logger.debug(" \t deleted={}", deleted);
            }
        }

        JSONObject deleteOldBlogRtn = new JSONObject();
        deleteOldBlogRtn.put("deletedCount", deletedCount);
        deleteOldBlogRtn.put("blogDurationMin", blogDurationMin);
        deleteOldBlogRtn.put("limitDateTime", limitDateTime.toDate());
        return deleteOldBlogRtn;
    }
}
