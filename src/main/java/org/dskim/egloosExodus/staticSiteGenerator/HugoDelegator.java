package org.dskim.egloosExodus.staticSiteGenerator;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dskim.egloosExodus.model.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

@Component
public class HugoDelegator implements StaticSiteGeneratorDelegator {
	private static final Logger logger = LogManager.getLogger(HugoDelegator.class);

	@Value(("${blog.rootDir}"))
	String rootDir;
	String baseDir;
	String postTemplate = "+++\n" +
			  "title = \"{title}\"\n" +
			  "date = \"{date}\"\n" +
			  "tags = [{tags}]\n" +
			  "featured_image = \"{featured_image}\"\n" +
			  "description = \"{description}\"\n" +
			  "+++\n" +
			  "{body}";

	@Override
	public void init(String baseDir) throws Exception {
		logger.debug("baseDir={}", baseDir);
		this.baseDir = baseDir;
		callCmd(new String[]{"rm", "-rf", rootDir + File.separator + baseDir}, null);
		//callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir}, null);
		//설정파일을 json형식을 사용
		callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir, "-f", "json"}, null);

		//기본 테마 압축해제
		//callCmd(new String[]{"tar", "-zxvf", rootDir + File.separator + "ananke.tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes/ananke", "--strip-components=1"}, null);
		callCmd(new String[]{"tar", "-zxvf", rootDir + File.separator + "ananke.tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes"}, null);

		// config에 테마 추가
		JSONObject hugoConfig = (JSONObject)JSONValue.parse(FileUtils.readFileToString(new File(rootDir + File.separator + baseDir + File.separator + "config.json"), "utf8"));
		hugoConfig.put("theme", "ananke");
		FileUtils.writeStringToFile(new File(rootDir + File.separator + baseDir + File.separator + "config.json"), hugoConfig.toJSONString(), "utf8");
	}

	@Override
	public void saveImage(String[] imageUrl) throws IOException {
		logger.debug("imageUrl={} / {}", imageUrl[0], imageUrl[1]);
		try{
			FileUtils.copyURLToFile(new URL(imageUrl[1]), new File(rootDir + File.separator + baseDir + "/content" + imageUrl[0]),5000,5000);
		} catch(IOException e) {
			logger.error("첨부 다운로드 오류는 무시함", e);
		}
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
