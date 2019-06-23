package org.dskim.egloosExodus.controller;

import lombok.Data;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.dskim.egloosExodus.repository.BlogRepository;
import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Data
@DependsOn("blogList")
public class BlogDownloaderManager {
    private static Logger logger = LoggerFactory.getLogger(BlogDownloaderManager.class);

    @Autowired
    HugoDelegator hugo;

    @Autowired
    EgloosBlogDownloader egloosBlogDownloader;

    @Autowired
    ConcurrentLinkedQueue blogList;

    @Autowired
    BlogRepository blogRepo;

    @Value("${maxPostCount}")
    int maxPostCount;

    Blog currentBlog;

    //@Async("threadPoolTaskExecutor")
    public void downloadBlog(Blog blog) throws Exception {
        this.currentBlog = blog;

        // resume 하는 경우가 아니라면 초기화 한다.
        if(blog.getCurrentPostUrl() == null) {
            hugo.init(blog);

            blog.setDownloadStartDate(new DateTime());
            blogRepo.save(blog);
        } else {
            logger.debug("resuming blog.getCurrentPostUrl()={}", blog.getCurrentPostUrl());
            blog.setBlogBaseUrl(blog.getCurrentPostUrl());
        }

        try {
            egloosBlogDownloader.downLoadBlog(hugo, blog, false, maxPostCount);
        } catch(Exception e) {
            logger.error("Exeption 발생하여 다운로드 중지!!!", e);
            this.currentBlog = null;
        }

        //egloosBlogDownloader.downLoadBlog("blogName", "js61030.egloos.com"); // 실패
        //egloosBlogDownloader.downLoadBlog("blogName", "news.egloos.com"); // 성공
        //egloosBlogDownloader.downLoadBlog("blogName", "jculture.egloos.com"); //실패

        hugo.generateStaticFles(blog);
        this.currentBlog = null;

        blog.setDownloadEndDate(new DateTime());
        blog.setDownloaded(true);
        blogRepo.save(blog);
    }

    @Scheduled(fixedDelay = 1000 * 10)
    public void watchQueue() throws Exception {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "regDate"));
        List<Blog> blogList = blogRepo.findAllByIsDownloaded(false, pageable);

        logger.debug("blogList={}", blogList);
        if(blogList != null && blogList.size() > 0) {
            downloadBlog(blogList.get(0));
        }
    }
}
