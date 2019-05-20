package org.dskim.egloosExodus.conf;

import org.dskim.egloosExodus.staticSiteGenerator.HugoDelegator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentSkipListMap;

@Configuration
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
}