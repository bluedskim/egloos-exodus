package org.dskim.egloosExodus.controller;

import lombok.Data;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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

    Blog currentBlog;

    @Async("threadPoolTaskExecutor")
    public void downloadBlog(Blog blog) throws Exception {
        this.currentBlog = blog;
        hugo.init(blog, "ananke");

        try {
            egloosBlogDownloader.downLoadBlog(hugo, blog.getBlogBaseUrl());
        } catch(Exception e) {
            logger.error("Exeption 발생하여 다운로드 중지!!!", e);
            this.currentBlog = null;
        }

        //egloosBlogDownloader.downLoadBlog("blogName", "js61030.egloos.com"); // 실패
        //egloosBlogDownloader.downLoadBlog("blogName", "news.egloos.com"); // 성공
        //egloosBlogDownloader.downLoadBlog("blogName", "jculture.egloos.com"); //실패

        hugo.generateStaticFles();
        this.currentBlog = null;
    }
}
