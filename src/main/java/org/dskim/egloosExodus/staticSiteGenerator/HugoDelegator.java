package org.dskim.egloosExodus.staticSiteGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
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

/**
 * Hugo 를 이용한 정적 사이트 생성
 */
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

	@Value(("${hugo.resourcesDir}"))
	String hugoResourcesDir;

	/**
	 * 블로그별로 호출해야 함
	 *
	 * @param blogName 블로그 명
	 * @param themeName 사용할 테마명
	 * @throws Exception
	 */
	@Override
	public void init(String blogName, String themeName) throws Exception {
		logger.debug("blogName={}", blogName);
		this.baseDir = blogName;

		callCmd(new String[]{"rm", "-rf", rootDir + File.separator + baseDir}, null);
		//callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir}, null);
		callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir, "-f", "yml"}, null);

		//기본 테마 압축해제
		//callCmd(new String[]{"tar", "-zxvf", rootDir + File.separator + "ananke.tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes/ananke", "--strip-components=1"}, null);
		callCmd(new String[]{"tar", "-zxvf", hugoResourcesDir + File.separator + themeName + ".tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes"}, null);

		// config에 테마 추가
		Map<String, Object> obj = yaml.load(new FileReader(hugoResourcesDir + File.separator + "hugo.config.yml"));
		obj.put("title", blogName);
		obj.put("theme", themeName);
		yaml.dump(obj, new FileWriter(rootDir + File.separator + baseDir + File.separator + "config.yml"));
		/*
		JSONObject hugoConfig = (JSONObject)JSONValue.parse(FileUtils.readFileToString(new File(rootDir + File.separator + baseDir + File.separator + "config.json"), "utf8"));
		hugoConfig.put("theme", "ananke");
		FileUtils.writeStringToFile(new File(rootDir + File.separator + baseDir + File.separator + "config.json"), hugoConfig.toJSONString(), "utf8");
		*/

		//static 에 필요 파일들 복사
		FileUtils.copyFile(new File(hugoResourcesDir + File.separator + "hugo.custom.css")
				, new File(rootDir + File.separator + baseDir + File.separator + "static" + File.separator + "custom.css"));
	}

	/**
	 * 웹경로를 가지고 있는 각종 파일 다운로드
	 *
	 * (이 메소드는 유틸리티 어딘가로 옮겨야 할 듯)
	 * @param resourceUrl
	 * @return
	 * @throws IOException
	 */
	@Override
	public String saveResourceFromUrl(String[] resourceUrl) throws IOException {
		logger.debug("resourceUrl={} / {}", resourceUrl[0], resourceUrl[1]);
		try{
			FileUtils.copyURLToFile(new URL(resourceUrl[1]), new File(rootDir + File.separator + baseDir + "/content" + resourceUrl[0]),5000,5000);
		} catch(IOException e) {
			logger.error("첨부 다운로드 오류는 무시함", e);
		}
		return resourceUrl[0];
	}

	/**
	 * site generator 흘 호출하여 새로운 post 생성
	 * hugo new "posts/aa/bb.md" -c /home/bluedskim/IdeaProjects/egloosExodus/blogRootDir/blogName/content
	 * @param post
	 */
	@Override
	public void createPost(Post post) throws Exception {
		post.setTitle(post.getTitle().replaceAll("/", "-"));
		post.setTitle(post.getTitle().replaceAll("\"", "'"));
		logger.debug("post.getTitle()={}", post.getTitle());
		String folderPath = rootDir + File.separator + baseDir + "/content/posts" + (StringUtils.isEmpty(post.getCategory()) ? "" : "/" + post.getCategory());
		File containingFolder = new File(folderPath);
		logger.debug("folderPath={}, exists={}", containingFolder.getAbsolutePath(), containingFolder.exists());

		int postCountWithSameTilte = 0;
		if(containingFolder.exists()) {
			/*
			//find ~/imsi/content -maxdepth 1 -name "*aaa*" | wc -l
			// 동작 안함 -.-
			//String findPostCountWithSameTilte = callCmd(new String[]{"find", folderPath, "-maxdepth", "1", "-name", "'" + post.getTitle() + "*'", "|", "wc", "-l"}, null);
			String countCommand = "find " + folderPath + " -maxdepth 1 -name '" + post.getTitle() + "*' | wc -l";
			logger.debug("countCommand={}", countCommand);
			String findPostCountWithSameTilte = callCmd(new String[]{countCommand}, null);
			logger.debug("중복된 파일명={}, postCountWithSameTilte={}", post.getTitle(), findPostCountWithSameTilte);
			int postCountWithSameTilte = Integer.parseInt(findPostCountWithSameTilte.trim());
			*/

			postCountWithSameTilte = FileUtils.listFiles(containingFolder, new WildcardFileFilter(post.getTitle()+"*"), null).size();
			logger.debug("postCountWithSameTilte={}", postCountWithSameTilte);
		}

		String outStr = callCmd(new String[]{"hugo", "new", "posts/" +
														  (post.getCategory() == null ? "" : post.getCategory() + "/") +
														  post.getTitle() + (postCountWithSameTilte > 0 ? "_" + postCountWithSameTilte : "") + ".md"
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
				saveResourceFromUrl(attachment);
			}
		}
	}

	/**
	 * 정적 사이트 생성
	 * @return Static 엔진 호출 결과
	 * @throws Exception
	 */
	@Override
	public String generateStaticFles() throws Exception {
		logger.debug("baseDir={}", baseDir);
		String generateStaticFlesRtn = callCmd(new String[]{"hugo", "-s", rootDir + File.separator + baseDir}, null);
		logger.debug("generateStaticFlesRtn={}", generateStaticFlesRtn);
		return generateStaticFlesRtn;
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
