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

	//String blogName = "하고 싶은 걸 하세요 Do What You Want";
	//String blogBaseUrl = "http://shed.egloos.com/1207526";

	//String blogName = "산바람이 만난 자연의 친구들";
	//String blogBaseUrl = "http://yeohans.egloos.com/5979275";	// 사진이 넘 많다.

	//String blogName = "플로렌스의 네티하비 블로그";
	//String blogBaseUrl = "http://netyhobby.egloos.com/1025728";

	// 날짜가 없다?
	//String blogName = "무릉도원에서 삼라만담";
	//String blogBaseUrl = "http://atonal.egloos.com/391150";

	String blogName = "라이트노벨 레이블 노블엔진";
	String blogBaseUrl = "http://novelengin.egloos.com/1327230";

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
