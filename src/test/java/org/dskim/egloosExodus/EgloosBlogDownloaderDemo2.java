package org.dskim.egloosExodus;

import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.dskim.egloosExodus.model.Post;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.util.List;

//@SpringBootApplication
public class EgloosBlogDownloaderDemo2 implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(EgloosBlogDownloaderDemo2.class);

	String blogName = "shed";
	String blogBaseUrl = "shed.egloos.com";

	@Autowired
	EgloosBlogDownloader egloosBlogDownloader;

	@Autowired
	HugoDelegator hugo;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(EgloosBlogDownloaderDemo2.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//egloosBlogDownloader.getBlogContent("http://shed.egloos.com/1828197");
		List<Post> postList = egloosBlogDownloader.getBlogContent("http://shed.egloos.com/3961921");
		logger.debug("postList={}", postList);
	}
}
