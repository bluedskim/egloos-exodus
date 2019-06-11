package org.dskim.egloosExodus.springDataJsondb;

import org.dskim.egloosExodus.springDataJsondb.jsondbRepo.BlogRepo;
import org.dskim.egloosExodus.model.Blog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication
//@EnableJsonDBRepositories(basePackages = "org.dskim.egloosExodus.springDataJsondb.jsondbRepo")
//@EnableJsonDBRepositories
//@ContextConfiguration(classes =JsonDbConf.class)
@ComponentScan
public class JsonDbTest implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(JsonDbTest.class);

    /*
    private String dbFilesLocation = "/home/bluedskim/IdeaProjects/egloosexodus/jsonDbFiles";
    private String dbScanPackage = "org.dskim.egloosExodus.springDataJsondb.jsondbRepo";

    @Bean
    public JsonDBTemplate jsonDBTemplate() {
        return new JsonDBTemplate(dbFilesLocation, dbScanPackage);
    }
    */

    @Autowired
    BlogRepo blogRepo;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(JsonDbTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Blog blog = new Blog("shed");
        blog.setBlogName("shed's blog");

        blogRepo.save(blog);
    }
}
