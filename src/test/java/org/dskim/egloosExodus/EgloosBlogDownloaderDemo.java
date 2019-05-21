package org.dskim.egloosExodus;

import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EgloosBlogDownloaderDemo implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(EgloosBlogDownloaderDemo.class);

	//String blogName = "netyhobby";
	String blogName = "하고 싶은 걸 하세요 Do What You Want";
	//String blogBaseUrl = "yeohans.egloos.com";	// 사진이 넘 많다.
	String blogBaseUrl = "shed.egloos.com";

	@Autowired
	EgloosBlogDownloader egloosBlogDownloader;

	@Autowired
	HugoDelegator hugo;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(EgloosBlogDownloaderDemo.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		hugo.init(blogName, blogName, "ananke");

		logger.info("EXECUTING : command line runner");
		egloosBlogDownloader.downLoadBlog(hugo, blogBaseUrl);
		//egloosBlogDownloader.downLoadBlog("blogName", "js61030.egloos.com"); // 실패
		//egloosBlogDownloader.downLoadBlog("blogName", "news.egloos.com"); // 성공
		//egloosBlogDownloader.downLoadBlog("blogName", "jculture.egloos.com"); //실패

		hugo.generateStaticFles();
	}
}
