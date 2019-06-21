package org.dskim.egloosExodus.processor;

import org.dskim.egloosExodus.model.Blog;
import org.junit.Test;

public class EgloosBlogDownloaderTest {

    @Test
    public void 유튜브embed주소변환() throws Exception {
        EgloosBlogDownloader downloader = new EgloosBlogDownloader();

        //downloader.getPost(new Blog(), "http://shed.egloos.com/3937430");
        //downloader.getPost(new Blog(), "http://shed.egloos.com/3869386");
        downloader.getPost(new Blog(), "http://shed.egloos.com/1189402");
    }

}
