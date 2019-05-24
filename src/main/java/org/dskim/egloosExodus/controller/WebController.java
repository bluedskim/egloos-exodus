package org.dskim.egloosExodus.controller;

import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.processor.EgloosBlogDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {
    private static Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    BlogDownloaderManager blogDownloaderManager;

    @Autowired
    EgloosBlogDownloader egloosBlogDownloader;

    @GetMapping("")
    public String index(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        logger.debug("blogDownloaderManager.getCurrentBlog()={}", blogDownloaderManager.getCurrentBlog());

        model.addAttribute("name", name);
        model.addAttribute("currentBlog", blogDownloaderManager.getCurrentBlog());
        return "index";
    }

    @PostMapping("download")
    public String download(Blog blog, Model model) throws Exception {
        logger.debug("blog={}", blog);
        logger.debug("blogDownloaderManager.getCurrentBlog()={}", blogDownloaderManager.getCurrentBlog());

        blogDownloaderManager.downloadBlog(blog);

        model.addAttribute("currentBlog", blog);
        return "index";
    }
}
