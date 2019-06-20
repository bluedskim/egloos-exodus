package org.dskim.egloosExodus.tomlDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TomljDemo {
    private static Logger logger = LoggerFactory.getLogger(Toml4jDemo.class);

    public static void main(String[] args) throws IOException {

        Path source = Paths.get("/home/bluedskim/IdeaProjects/egloosexodus/hugoResources/hugo-theme-bootstrap4-blog.toml");
        TomlParseResult result = Toml.parse(source);
        result.errors().forEach(error -> System.err.println(error.toString()));

        String value = result.getString("a. dotted . key");
    }
}
