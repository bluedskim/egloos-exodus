package org.dskim.egloosExodus;

import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class EgloosBlogDownloaderTest implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(EgloosBlogDownloaderTest.class);

    //String blogName = "netyhobby";
    String blogName = "하고 싶은 걸 하세요 Do What You Want";
    //String blogBaseUrl = "yeohans.egloos.com";	// 사진이 넘 많다.
    String blogBaseUrl = "shed.egloos.com";

    @Autowired
    EgloosBlogDownloader egloosBlogDownloader;

    @Autowired
    HugoDelegator hugo;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EgloosBlogDownloaderTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("EXECUTING : command line runner");
        //egloosBlogDownloader.downLoadBlog(hugo, blogBaseUrl);
        //egloosBlogDownloader.getFirstPostUrl("http://shed.egloos.com");
        egloosBlogDownloader.getPost("http://shed.egloos.com/4068678");
    }
}
