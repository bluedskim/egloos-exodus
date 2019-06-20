package org.dskim.egloosExodus.tomlDemo;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import jodd.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Toml4jDemo {
    private static Logger logger = LoggerFactory.getLogger(Toml4jDemo.class);

    public static void main(String[] args) throws IOException {
        TomlWriter tomlWriter = new TomlWriter.Builder()
                .indentValuesBy(2)
                .indentTablesBy(2)
                .padArrayDelimitersBy(2)
                .build();

        Toml toml = new Toml().read(new File("/home/bluedskim/IdeaProjects/egloosexodus/hugoResources/hugo-theme-bootstrap4-blog.toml"));
        logger.debug("original toml={}", tomlWriter.write(toml));
        /*
        toml = new Toml(toml).read(
                        "title = \"바뀐 제목\"" +
                        "title = \"바뀐 제목\"" +
                        "");
        */
        List<Map<String, Object>> sidebarList = toml.getList("menu.sidebar");
        logger.debug("sidebarList={}", sidebarList);
        Map<String, Object> prevBlog = sidebarList.get(1);
        prevBlog.put("name", "사용자 아이디");
        prevBlog.put("url", "사용자 아이디.eglos.com");

        Map<String, Object> newSideBar = new HashMap<String, Object>();
        newSideBar.put("name", "추가 링크");
        newSideBar.put("url", "추가 링크 주소");
        sidebarList.add(newSideBar);



        File modified = new File("/tmp/temp.toml");
        tomlWriter.write(toml, new File("/tmp/temp.toml"));
        logger.debug("modified=\n{}", FileUtil.readString(modified, "utf8"));
    }
}
