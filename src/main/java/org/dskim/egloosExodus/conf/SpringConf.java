package org.dskim.egloosExodus.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class SpringConf {
	//@Bean
	/*
	public ConcurrentLinkedQueue<Element> blogList() {
	return new ConcurrentLinkedQueue<Element>();
	}
	*/

	@Bean
	public ConcurrentSkipListMap blogList() {
		return new ConcurrentSkipListMap();
	}

	/*
	@Bean
	public HugoDelegator hugoDelegator() {
		return new HugoDelegator();
	}
	*/

	@Bean(name = "threadPoolTaskExecutor")
	public Executor threadPoolTaskExecutor() {
		return new ThreadPoolTaskExecutor();
	}
}