package org.dskim.egloosExodus.tomlDemo;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NightConfigDemo {
    private static Logger logger = LoggerFactory.getLogger(NightConfigDemo.class);

    public static void main(String args[]) {
        // Simple builder:
        FileConfig conf = FileConfig.of("the/file/config.toml");

        // Advanced builder, default resource, autosave and much more (-> cf the wiki)
        CommentedFileConfig config = CommentedFileConfig.builder("/home/bluedskim/IdeaProjects/egloosexodus/blogRootDir/shed/config.toml")
                //.defaultResource("/home/bluedskim/IdeaProjects/egloosexodus/hugoResources/hugo-theme-bootstrap4-blog.toml")
                .autosave().build();
        config.load(); // This actually reads the config

        logger.debug("old title={}", (String)config.get("title"));
        config.set("title", "바뀐 title");
        logger.debug("new title={}", (String)config.get("title"));

        List<Config> navbar = new ArrayList<>();
        for(int i = 1 ; i <= 10 ; i++) {
            Config tempConfig = Config.inMemory();
            tempConfig.set("name", "카테고리" + i);
            tempConfig.set("url", "/categories/카테고리" + i);
            navbar.add(tempConfig);
        }
        config.set("menu.navbar", navbar);

        List<Config> sidebar = config.get("menu.sidebar"); // Generic return type!
        logger.debug("sidebar.size()={}, sidebar={}", sidebar.size(), sidebar);

        sidebar.get(1).set("url", "이전 블로그 url");
        config.set("menu.sidebar", sidebar);

        config.set("params.author", "egloos아이디");
        config.set("params.description", "egloos아이디" + "의 블로그");
        config.set("params.sidebar.about", "egloos아이디" + "의 " + "블로그 제목");

        /*
        List<String> names = config.get("users_list"); // Generic return type!
        long id = config.getLong("account.id"); // Compound path: key "id" in subconfig "account"
        int points = config.getIntOrElse("account.score", 0); // Default value

        config.set("account.score", points*2);

        String comment = config.getComment("user");
// NightConfig saves the config's comments (for TOML and HOCON)

// config.save(); not needed here thanks to autosave()
         */
        config.close(); // Close the FileConfig once you're done with it :)`
    }
}
