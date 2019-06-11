package org.dskim.egloosExodus.springDataJsondb;

import io.jsondb.JsonDBTemplate;
import org.mambofish.spring.data.jsondb.repository.EnableJsonDBRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableJsonDBRepositories
//@EnableJsonDBRepositories(basePackages = "org.dskim.egloosExodus.springDataJsondb.jsondbRepo")
public class JsonDbConf {
    private static Logger logger = LoggerFactory.getLogger(JsonDbConf.class);

    @Bean
    public JsonDBTemplate jsonDBTemplate() {
        logger.debug("init JsonDBTemplate");
        return new JsonDBTemplate("/home/bluedskim/IdeaProjects/egloosexodus/jsonDbFiles", "org.dskim.egloosExodus.jsondb");
    }
}
