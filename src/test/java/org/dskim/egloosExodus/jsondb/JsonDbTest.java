package org.dskim.egloosExodus.jsondb;

import io.jsondb.InvalidJsonDbApiUsageException;
import io.jsondb.JsonDBTemplate;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.model.BlogInQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JsonDbTest implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(JsonDbTest.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(JsonDbTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //Actual location on disk for database files, process should have read-write permissions to this folder
        String dbFilesLocation = "/home/bluedskim/IdeaProjects/egloosexodus/jsonDbFiles";

        String baseScanPackage = "org.dskim.egloosExodus.model";

        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

        /*
        Blog blog = new Blog("shed");
        blog.setBlogName("shed's blog");
        try{
            jsonDBTemplate.createCollection(Blog.class);
        } catch (InvalidJsonDbApiUsageException e) {
            logger.error("InvalidJsonDbApiUsageException 무시함", e);
        }
        jsonDBTemplate.insert(blog);
        blog = (Blog)jsonDBTemplate.findById("000000", Blog.class);
        logger.debug("findById={}", blog.getUserId());
        */
        BlogInQ blog = new BlogInQ("shed");
        //blog.setBlogUrl("url");
        try{
            jsonDBTemplate.createCollection(BlogInQ.class);
        } catch (InvalidJsonDbApiUsageException e) {
            logger.error("InvalidJsonDbApiUsageException 무시함", e);
        }
        jsonDBTemplate.insert(blog);
        blog = (BlogInQ)jsonDBTemplate.findById("000000", BlogInQ.class);
        logger.debug("blog={}", blog);
    }
}
