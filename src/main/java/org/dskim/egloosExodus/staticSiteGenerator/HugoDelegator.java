package org.dskim.egloosExodus.staticSiteGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dskim.egloosExodus.model.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.Map;

@Component
public class HugoDelegator implements StaticSiteGeneratorDelegator {
	private static final Logger logger = LogManager.getLogger(HugoDelegator.class);

	Yaml yaml = new Yaml();

	@Value(("${blog.rootDir}"))
	String rootDir;
	String baseDir;
	String postTemplate = "+++\n" +
			  "title = \"{title}\"\n" +
			  "date = \"{date}\"\n" +
			  "categories = [{categories}]\n" +
			  "tags = [{tags}]\n" +
			  "featured_image = \"{featured_image}\"\n" +
			  "description = \"{description}\"\n" +
			  "+++\n" +
			  "{body}";

	@Override
	public void init(String baseDir, String blogName, String themeName) throws Exception {
		logger.debug("baseDir={}", baseDir);
		this.baseDir = baseDir;

		// theme파일
		File zippedTheme = (new ClassPathResource(themeName + ".tgz")).getFile();

		callCmd(new String[]{"rm", "-rf", rootDir + File.separator + baseDir}, null);
		//callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir}, null);
		//설정파일을 json형식을 사용
		callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir, "-f", "yml"}, null);

		//기본 테마 압축해제
		//callCmd(new String[]{"tar", "-zxvf", rootDir + File.separator + "ananke.tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes/ananke", "--strip-components=1"}, null);
		callCmd(new String[]{"tar", "-zxvf", zippedTheme.getAbsolutePath(), "-C", rootDir + File.separator + baseDir + File.separator + "themes"}, null);

		// config에 테마 추가
		Map<String, Object> obj = yaml.load((new ClassPathResource("hugo.config.yml")).getInputStream());
		obj.put("title", blogName);
		obj.put("theme", themeName);
		yaml.dump(obj, new FileWriter(rootDir + File.separator + baseDir + File.separator + "config.yml"));
		/*
		JSONObject hugoConfig = (JSONObject)JSONValue.parse(FileUtils.readFileToString(new File(rootDir + File.separator + baseDir + File.separator + "config.json"), "utf8"));
		hugoConfig.put("theme", "ananke");
		FileUtils.writeStringToFile(new File(rootDir + File.separator + baseDir + File.separator + "config.json"), hugoConfig.toJSONString(), "utf8");
		*/
	}

	@Override
	public String saveImage(String[] imageUrl) throws IOException {
		logger.debug("imageUrl={} / {}", imageUrl[0], imageUrl[1]);
		try{
			FileUtils.copyURLToFile(new URL(imageUrl[1]), new File(rootDir + File.separator + baseDir + "/content" + imageUrl[0]),5000,5000);
		} catch(IOException e) {
			logger.error("첨부 다운로드 오류는 무시함", e);
		}
		return imageUrl[0];
	}

	/**
	 * hugo new "posts/aa/bb.md" -c /home/bluedskim/IdeaProjects/egloosExodus/blogRootDir/blogName/content
	 * @param post
	 */
	@Override
	public void createPost(Post post) throws Exception {
		post.setTitle(post.getTitle().replaceAll("/", "-"));
		post.setTitle(post.getTitle().replaceAll("\"", "'"));
		logger.debug("post={}", post);
		String outStr = callCmd(new String[]{"hugo", "new", "posts/" +
														  (post.getCategory() == null ? "" : post.getCategory() + "/") +
														  post.getTitle() + ".md"
								  , "-c"
								  , rootDir + File.separator + baseDir + File.separator + "content"
		}, null);
		outStr = outStr.replaceAll("created", "");
		outStr = outStr.trim();
		String tempPostTemplate = postTemplate.replace("{title}", post.getTitle());
		tempPostTemplate = tempPostTemplate.replace("{categories}", "\"" + post.getCategory() + "\"");
		tempPostTemplate = tempPostTemplate.replace("{date}", post.getUtcDate());
		tempPostTemplate = tempPostTemplate.replace("{tags}", post.getTags());
		tempPostTemplate = tempPostTemplate.replace("{featured_image}", post.getFeaturedImage());
		tempPostTemplate = tempPostTemplate.replace("{description}", post.getDescription());
		tempPostTemplate = tempPostTemplate.replace("{body}", post.getBodyHtml());

		FileUtils.writeStringToFile(new File(outStr), tempPostTemplate, "utf8");

		if(post.getAttachments() != null) {
			for (String[] attachment : post.getAttachments()) {
				saveImage(attachment);
			}
		}
	}

	@Override
	public void generateStaticFles() throws Exception {
		logger.debug("baseDir={}", baseDir);
		callCmd(new String[]{"hugo", "-s", rootDir + File.separator + baseDir}, null);
	}

	/**
	 * 시스템 명령어 호출하고 결과 반환
	 * @param commandAndArgs 명령어(경로)와 명령의 아규먼트
	 * @param inStreamStr 명령에 전달해야 할 byte stream 이 있다면
	 * @return 호출 결과
	 * @throws Exception
	 */
	private String callCmd(String[] commandAndArgs, String inStreamStr) throws Exception {
		String outStr = null;
		ProcessBuilder builder = new ProcessBuilder();
		builder.command(commandAndArgs);
		builder.redirectErrorStream(true);
		Process pr = builder.start();

		//Process pr = rt.exec("docker exec keen_wing pandoc");
		InputStream stdout = pr.getInputStream ();
		InputStream stderr = pr.getErrorStream ();
		if(inStreamStr != null) {
			OutputStream stdin = pr.getOutputStream();
			stdin.write(inStreamStr.getBytes());
			stdin.flush();
			stdin.close();
		}

		pr.waitFor();

		outStr = IOUtils.toString(stdout);
		logger.debug("exitValue={}, outStr={}", pr.exitValue(), outStr);
		//logger.debug("stderr={}", IOUtils.toString(stderr));

		stderr.close();
		stdout.close();

		pr.destroy();
		if (pr.isAlive()) {
			pr.destroyForcibly();
		}

		return outStr;
	}
}
