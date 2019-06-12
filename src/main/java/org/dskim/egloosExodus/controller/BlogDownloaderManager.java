package org.dskim.egloosExodus.controller;

import lombok.Data;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

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
    ObjectRepository<Blog> downloadQueueRepo;

    Blog currentBlog;

    //@Async("threadPoolTaskExecutor")
    public void downloadBlog(Blog blog) throws Exception {
        this.currentBlog = blog;

        // resume 하는 경우가 아니라면 초기화 한다.
        if(blog.getCurrentPostUrl() == null) {
            hugo.init(blog, "ananke");
        } else {
            logger.debug("resuming blog.getCurrentPostUrl()={}", blog.getCurrentPostUrl());
            blog.setBlogBaseUrl(blog.getCurrentPostUrl());
            hugo.setBlog(blog);
        }

        try {
            egloosBlogDownloader.downLoadBlog(hugo, blog);
        } catch(Exception e) {
            logger.error("Exeption 발생하여 다운로드 중지!!!", e);
            this.currentBlog = null;
        }

        //egloosBlogDownloader.downLoadBlog("blogName", "js61030.egloos.com"); // 실패
        //egloosBlogDownloader.downLoadBlog("blogName", "news.egloos.com"); // 성공
        //egloosBlogDownloader.downLoadBlog("blogName", "jculture.egloos.com"); //실패

        hugo.generateStaticFles();
        this.currentBlog = null;

        //TODO blog repo에서 방금 처리완료한 blog 삭제
        WriteResult deleteResult = downloadQueueRepo.remove(blog);
        logger.debug("deleteResult.getAffectedCount()={}", deleteResult.getAffectedCount());
    }

    @Scheduled(fixedDelay = 1000)
    public void watchQueue() throws Exception {
        //logger.debug("checking queue...");
        Cursor results = downloadQueueRepo.find(FindOptions.limit(0, 1));
        logger.debug("queue size={}", results.size());
        if(results.size() > 0) {
            /*
            Blog tempBlog = new Blog();
            tempBlog.setBlogBaseUrl(((Blog)results.firstOrDefault()).getBlogBaseUrl());
            logger.debug("downloadQueueRepo.isClosed()={}", downloadQueueRepo.isClosed());
            downloadBlog(tempBlog);
            */
            downloadBlog((Blog)results.firstOrDefault());
        }
    }
}
