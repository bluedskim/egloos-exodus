package org.dskim.egloosExodus.conf;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.objects.ObjectRepository;
import org.dskim.egloosExodus.model.Blog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class SpringConf {
	private static final Logger logger = LoggerFactory.getLogger(SpringConf.class);

	@Value(("${nitrite.path}"))
	String nitritePath;

	@Bean
	public ConcurrentLinkedQueue<Blog> blogList() {
	return new ConcurrentLinkedQueue();
	}

	/*
	@Bean(name = "blogList")
	public ConcurrentSkipListMap blogList() {
		logger.debug("initializing blogList");
		return new ConcurrentSkipListMap();
	}
	*/

	/*
	@Bean
	public HugoDelegator hugoDelegator() {
		return new HugoDelegator();
	}
	*/

	@Bean(name = "threadPoolTaskExecutor")
	public Executor threadPoolTaskExecutor() {
		logger.debug("initializing threadPoolTaskExecutor");
		return new ThreadPoolTaskExecutor();
	}

	@Bean
	public Nitrite nitrite() {
		Nitrite db = Nitrite.builder()
				.compressed()
				.filePath(nitritePath)
				.openOrCreate()
				;
		return db;
	}

	@Bean
	public NitriteCollection stat() {
		return nitrite().getCollection("stat");
	}

	@Bean
	public ObjectRepository<Blog> downloadQueueRepo() {
		return nitrite().getRepository(Blog.class);
	}
}