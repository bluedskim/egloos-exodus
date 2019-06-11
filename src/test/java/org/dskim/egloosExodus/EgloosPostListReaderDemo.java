package org.dskim.egloosExodus;

import org.dskim.egloosExodus.processor.EgloosPostListReader;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

//@SpringBootApplication
public class EgloosPostListReaderDemo implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(EgloosPostListReaderDemo.class);

    @Autowired
    EgloosPostListReader egloosPostListReader;

    @Autowired
    ConcurrentSkipListMap blogList;

    public static void main(String[] args) {
        SpringApplication.run(EgloosPostListReaderDemo.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        logger.info("EXECUTING : command line runner");
        //ConcurrentSkipListMap blogList = egloosPostListReader.getPostList("http://shed.egloos.com");

        Map.Entry<String, Element> entry;

        /*
        while (( entry = blogList.pollFirstEntry()) != null) {
            Element link = entry.getValue();
            logger.debug(" \t text={} link={}", link.text(), link.attr("href"));
        }
        */
    }
}
