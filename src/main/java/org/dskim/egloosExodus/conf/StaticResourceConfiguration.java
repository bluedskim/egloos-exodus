package org.dskim.egloosExodus.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfiguration implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceConfiguration.class);

    @Value("${blog.rootDir}")
    private String blogRootDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if(blogRootDir != null) {
            logger.info("블로그 다운로드 경로={}", blogRootDir);
            registry.addResourceHandler("/b/**").addResourceLocations(blogRootDir);
        }
    }
}
