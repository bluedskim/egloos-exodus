package org.dskim.egloosExodus.controller;

import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BlogDownloaderManager {
    private static Logger logger = LoggerFactory.getLogger(BlogDownloaderManager.class);

    @Autowired
    HugoDelegator hugo;

    @Autowired
    EgloosBlogDownloader egloosBlogDownloader;

    @Async("threadPoolTaskExecutor")
    public void downloadBlog(Blog blog) throws Exception {
        hugo.init(blog.getBlogName(), "ananke");

        logger.info("EXECUTING : command line runner");
        egloosBlogDownloader.downLoadBlog(hugo, blog.getBlogBaseUrl());

        //egloosBlogDownloader.downLoadBlog("blogName", "js61030.egloos.com"); // 실패
        //egloosBlogDownloader.downLoadBlog("blogName", "news.egloos.com"); // 성공
        //egloosBlogDownloader.downLoadBlog("blogName", "jculture.egloos.com"); //실패

        hugo.generateStaticFles();
    }
}
