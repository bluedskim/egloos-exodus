package org.dskim.egloosExodus.staticSiteGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.model.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
			  //"featured_image = \"{featured_image}\"\n" +
			  "description = \"{description}\"\n" +
			  "+++\n" +
			  "{body}";

	@Value(("${hugo.resourcesDir}"))
	String hugoResourcesDir;

	@Value(("${senderMailId}"))
	String senderMailId;

	@Value(("${senderMailpw}"))
	String senderMailpw;

	// 현재 다운로드 중 블로그
	Blog blog;

	/**
	 * 블로그별로 호출해야 함
	 *
	 * @param blog 블로그
	 * @param themeName 사용할 테마명
	 * @throws Exception
	 */
	@Override
	public void init(Blog blog, String themeName) throws Exception {
		logger.debug("blogName={}", blog.getBlogName());
		this.blog = blog;
		this.baseDir = blog.getBlogName();

		callCmd(new String[]{"rm", "-rf", rootDir + File.separator + baseDir}, null);
		//callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir}, null);
		callCmd(new String[]{"hugo", "new", "site", rootDir + File.separator + baseDir, "-f", "yml"}, null);

		//기본 테마 압축해제
		//callCmd(new String[]{"tar", "-zxvf", rootDir + File.separator + "ananke.tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes/ananke", "--strip-components=1"}, null);
		callCmd(new String[]{"tar", "-zxvf", hugoResourcesDir + File.separator + themeName + ".tgz", "-C", rootDir + File.separator + baseDir + File.separator + "themes"}, null);

		// config에 테마 추가
		Map<String, Object> obj = yaml.load(new FileReader(hugoResourcesDir + File.separator + "hugo.config.yml"));
		obj.put("title", blog.getBlogName());
		obj.put("theme", themeName);

		// ananke theme 에 featured 이미지 추가
		String egloosProfileImagePath = saveResourceFromUrl(getEgloosProfileImage(blog));
		logger.debug("egloosProfileImagePath={}", egloosProfileImagePath);
		if(egloosProfileImagePath != null) {
			Map<String, Object> params = (HashMap<String, Object>)obj.get("params");
			params.put("featured_image", egloosProfileImagePath);
		}

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

	private String[] getEgloosProfileImage(Blog blog) throws IOException {
		logger.debug("blog Url={}", blog.getBlogBaseUrl());
		String[] profileImg = null;

		Document document = Jsoup.connect(blog.getBlogBaseUrl()).get();
		logger.debug("document.title()={}, div.profile_image={}", document.title(), document.selectFirst("div.profile_image"));

		Element profileImgEl = document.selectFirst("div.profile_image > a > img");
		if(profileImgEl != null) {
			profileImg = new String[]{"/attachment/profileImg.jpg", profileImgEl.attr("src")};
		} else {
			logger.debug("profileImgEl 없음");
		}

		logger.debug("profileImg={}", profileImg);
		return profileImg;
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
		try{
			logger.debug("resourceUrl={} / {}", resourceUrl[0], resourceUrl[1]);
			FileUtils.copyURLToFile(new URL(resourceUrl[1]), new File(rootDir + File.separator + baseDir + "/content" + resourceUrl[0]),5000,5000);
		} catch(Exception e) {
			logger.error("리소스 다운로드 오류는 무시함", e);
		}
		return resourceUrl == null ? null : resourceUrl[0];
	}

	/**
	 * site generator 흘 호출하여 새로운 post 생성
	 * hugo new "posts/aa/bb.md" -c /home/bluedskim/IdeaProjects/egloosExodus/blogRootDir/blogName/content
	 * @param post
	 */
	@Override
	public void createPost(Post post) throws Exception {
		// 파일명에 / 는 - 로 변경, " 는 없앰
		String postFileName = post.getTitle().replace("/", "-").replace("\"", "");
		logger.debug("postFileName={}", postFileName);
		// 카테고리명에 / 를 - 로 변경
		post.setCategory(post.getCategory().replace("/", "-"));
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

			postCountWithSameTilte = FileUtils.listFiles(containingFolder, new WildcardFileFilter(postFileName+"*"), null).size();
			logger.debug("postCountWithSameTilte={}", postCountWithSameTilte);
		}

		String outStr = callCmd(new String[]{"hugo", "new", "posts/" +
														  (post.getCategory() == null ? "" : post.getCategory() + "/") +
															postFileName + (postCountWithSameTilte > 0 ? "_" + postCountWithSameTilte : "") + ".md"
								  , "-c"
								  , rootDir + File.separator + baseDir + File.separator + "content"
		}, null);
		outStr = outStr.replaceAll("created", "");
		outStr = outStr.trim();
		// 제목에 "와 역슬래시 escape,
		String tempPostTemplate = postTemplate.replace("{title}", post.getTitle().replace("\\", "\\\\").replace("\"", "\\\""));
		tempPostTemplate = tempPostTemplate.replace("{categories}", "\"" + post.getCategory() + "\"");
		tempPostTemplate = tempPostTemplate.replace("{date}", post.getUtcDate());
		tempPostTemplate = tempPostTemplate.replace("{tags}", post.getTags());
		if(!StringUtils.isEmpty(post.getFeaturedImage())) {
			tempPostTemplate = tempPostTemplate + ("{featured_image}" + post.getFeaturedImage());
		}
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
		// 압축하기
		//String zipFlesRtn = callCmd(new String[]{"zip", "-r", "\"" + rootDir + File.separator + baseDir + ".zip\"", rootDir + File.separator + baseDir}, null);
		//String zipFlesRtn = callCmd(new String[]{"zip", "-r", rootDir + File.separator + baseDir + ".zip", rootDir + File.separator + baseDir}, null);
		// 전체 경로로 압축 됨 -.-
		//String zipFlesRtn = callCmd(new String[]{"zip", "-b", rootDir, "-r", baseDir + ".zip", baseDir}, null);
		// tar -zcvf /home/bluedskim/IdeaProjects/egloosexodus/blogRootDir/하고\ 싶은\ 걸\ 하세요\ Do\ What\ You\ Want.tgz -C /home/bluedskim/IdeaProjects/egloosexodus/blogRootDir 하고\ 싶은\ 걸\ 하세요\ Do\ What\ You\ Want

		String[] zipCommand = new String[]{"tar", "-zcf", rootDir + File.separator + baseDir + ".tgz", "-C", rootDir, baseDir};
		logger.debug("zipCommand={}", String.join(" ", zipCommand));
		String zipFlesRtn = callCmd(zipCommand, null);

		logger.debug("zipFlesRtn={}", zipFlesRtn);
		// 메일 보내기
		sendDownloadCompleteAlarm(blog, senderMailId, senderMailpw);

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

	private void sendDownloadCompleteAlarm(Blog blog, String senderMailId, String senderMailpw) {
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true"); //TLS

		Session session = Session.getInstance(prop,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(senderMailId, senderMailpw);
					}
				});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderMailId + "@gmail.com"));
			message.setRecipients(
					Message.RecipientType.TO,
					InternetAddress.parse("egloos.exodus@gmail.com, " + blog.getEmail())
			);
			message.setSubject("Egloos Exodus : [" + blog.getBlogName() + "] 다운로드 완료");
			message.setContent("[" + blog.getBlogName() + "] 다운로드가 완료되었습니다 !!! 24시간 후 자동 삭제됩니다 !!!<br/>"
					+ "* 미리보기 : <a href=\"http://samba.iptime.org/ee/" + blog.getBlogName() + "/public\">http://samba.iptime.org/ee/" + blog.getBlogName() + "/public</a><br/>"
					+ "* 다운로드 : <a href=\"http://samba.iptime.org/ee/" + blog.getBlogName() + ".tgz\">http://samba.iptime.org/ee/" + blog.getBlogName() + ".tgz</a><br/>"
					+ "<hr/>"
					+ "<a href=\"http://samba.iptime.org:8081/ee\">Egloos Exodus</a>"
					, "text/html; charset=utf-8");

			logger.debug("메일 발송 중", message.getContent());
			Transport.send(message);

			logger.debug("메일 발송 완료");
		} catch (Exception e) {
			logger.error("메일 발송 실패!!! ", e);
		}
	}
}
