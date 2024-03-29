package org.dskim.egloosExodus.processor;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.model.Post;
import org.dskim.egloosExodus.repository.BlogRepository;
import org.dskim.egloosExodus.staticSiteGenerator.StaticSiteGeneratorDelegator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Data
public class EgloosBlogDownloader {
	private static final Logger logger = LogManager.getLogger(EgloosBlogDownloader.class);

	//int currentBlogNo = 1;
	//int currentBlogNo = 100;
	int currentBlogNo = 113;
	// int currentBlogNo = 10000;

	@Value("${blog.minSleepTime}")
	int minSleepTime;

	@Autowired
	BlogRepository blogRepo;

	public boolean isDownloading = false;

	/**
	 * 블로그 메인 주소를 받아서 전체 블로그를 다운로드 한다.
	 *
	 * @param siteGen
	 * @param blog
	 * @return
	 * @throws Exception
	 */
	public boolean downLoadBlog(StaticSiteGeneratorDelegator siteGen, Blog blog, Boolean isPreview) throws Exception {
		logger.info("downloading blogBaseUrl={}", blog.getBlogBaseUrl());

		int maxPostCount = blog.getMaxPostCount();

		boolean isSuccess = false;
		isDownloading = true;
		//this.blogBaseUrl = StringUtils.substringBeforeLast(blogBaseUrl, "/");
		//this.blogBaseUrl = "http://" + blog.getUserId() + ".egloos.com";

		String postUrl = getFirstPostUrl(blog);
		Post post = null;

		//blog.setBlogBaseUrl("http://" + blog.getUserId() + ".egloos.com");

		do {
			//long sleepTime = minSleepTime * 1000 * (1L + (long) (Math.random() * (3L - 1L)));
			long sleepTime = minSleepTime * 1000 + (long)(1000 * 2 * Math.random());
			int retryCount = 1;

			do {
				try {
					post = getPost(blog, postUrl);
				} catch(Exception e) {
					post = null;
					logger.error("exception! retryCount={}, sleepTime={}", retryCount, sleepTime, e);
					++retryCount;
					Thread.sleep(sleepTime);
				}
			} while (post == null && retryCount <= 5);

			siteGen.createPost(blog, post);
			postUrl = post.getPrevPostUrl();

			blog.setCurrentPostUrl(post.getUrl());
			if(!isPreview) blogRepo.save(blog);
			logger.debug("blog.getBlogBaseUrl()={}, blog.getCurrentPostUrl()={}", blog.getBlogBaseUrl(), blog.getCurrentPostUrl());
			//logger.debug("downloadQueueRepo.isClosed()={}", downloadQueueRepo.isClosed());

			logger.debug("sleeping... {}, postUrl={}", sleepTime, postUrl);
			Thread.sleep(sleepTime);

		} while (postUrl != null && --maxPostCount > 0);

		currentBlogNo = 1;
		isDownloading = false;

		logger.debug("downLoadBlog done isSuccess={}", isSuccess);
		return isSuccess;
	}

	/**
	 * 해당 블로그의 첫번째 글 주소를 가져온다.
	 * @param blog
	 * @return
	 * @throws IOException
	 */
	private String getFirstPostUrl(Blog blog) throws Exception {
		String firstPostUrl = null;

		Document document = Jsoup.connect(blog.getBlogBaseUrl()).get();
		logger.debug("document.title()={}", document.title());

		/*
		Element link = document.select("body > div.body > div > div.header > h1 > a").get(0);
		logger.debug("blogName={}", link.text());
		*/

		//logger.debug("등록된 포스트가 없습니다?={}", document.select("div:contains(등록된 포스트가 없습니다.)"));

		// 더 이상 블로그가 없다면
		if(document.select("div:contains(등록된 포스트가 없습니다.)").size() > 0) {
			logger.debug("등록된 포스트가 없습니다.");
			return null;
		}

		Element postAnchor = document.selectFirst("h2.entry-title > a");
		firstPostUrl = "http://" + blog.getUserId() + ".egloos.com/" + postAnchor.attr("href");
		logger.debug("firstPostUrl={}", firstPostUrl);

		return firstPostUrl;
	}

	public Post getPost(Blog blog, String postUrl) throws Exception {
		if(postUrl == null) return null;
		Post post = new Post();
		post.setUrl(postUrl);

		Document document = Jsoup.connect(postUrl).get();
		logger.debug("postUrl={}, document.title()={}", postUrl, document.title());

		// 더 이상 블로그가 없다면
		/*	// 더 이상 블로그가 없다면 이 메소드가 호출되지 않았을 것이므로 무의미한 코딩임.
		logger.debug("document.select(\"div:contains(등록된 포스트가 없습니다.)\").size()={}", document.select("div:contains(등록된 포스트가 없습니다.)").size());
		if(document.select("div:contains(등록된 포스트가 없습니다.)").size() > 0) {
			logger.debug("등록된 포스트가 없습니다.");
			return null;
		}
		*/

		Element blogPost = document.selectFirst("div.post_view");
		logger.debug("blogPost.html().length()={}", blogPost.html().length());

		//'신고' 삭제
		Element 신고 = blogPost.selectFirst("span:matchesOwn(신고)");
		if(신고 != null) {
			신고.parent().parent().remove();
		}

		Elements embedTags = blogPost.select("embed");
		logger.debug("embedTags.size()={}, embedTags={}", embedTags.size(), embedTags);

		Elements iframeTags = blogPost.select("iframe");
		embedTags.addAll(iframeTags);
		for (Element embedTag : embedTags) {
			if(embedTag.attr("src").contains("youtube.com") == true ) {
				embedTag.attr("src", embedTag.attr("src").replace("http://", "https://"));
			}
		}
		logger.debug("secured embedTags.size()={}, embedTags={}", embedTags.size(), embedTags);

		//logger.debug("blogPost={}", blogPost);
		Element postTitleArea = blogPost.select("div.post_title_area").first();
		//logger.debug("entry-title={}", postTitleArea);
		Element title = blogPost.select("a").first();
		logger.debug("title={}", title.text());
		post.setTitle(title.text());
		post.setId(title.attr("name"));

		Element category = blogPost.selectFirst("span.post_title_category");
		if(category == null) {
			// http://eggry.egloos.com/1572381
			category = blogPost.selectFirst("li.post_info_category");
		}
		post.setCategory(category.text());

		Element date = blogPost.select("abbr").first();
		logger.debug("date={}", date.text());
		post.setDate(date.text());
		//Element commentCount = postTitleArea.select("span.txt").first();

		//logger.debug("덧글수={}", new StringTokenizer(commentCount.text(), ":").);
		if(postTitleArea.select("ul > li.post_info_cmtcount") != null
				&& postTitleArea.select("ul > li.post_info_cmtcount").size() > 0) {
			Element commentCount = postTitleArea.select("ul > li.post_info_cmtcount").first();
			logger.debug("덧글수=[{}]", commentCount.text().substring(commentCount.text().lastIndexOf(": ")+1).trim());
		} else {
			logger.debug("덧글수 없음");
		}

		Elements images = blogPost.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
		logger.debug("이미지 개수=[{}]", images == null ? "null" : images.size());
		for (Element image : images) {
			/*
			System.out.println("\nsrc : " + image.attr("src"));
			System.out.println("height : " + image.attr("height"));
			System.out.println("width : " + image.attr("width"));
			System.out.println("alt : " + image.attr("alt"));
			*/
			// 이런 이미지들은 제외 http://md.egloos.com/img/icon/ico_badreport.png
			if(image.attr("src").contains("/md") == false
					&& image.attr("src").contains("/profile") == false ) {
				//logger.debug(" \t img src={}", image.attr("src"));
				if(post.getAttachments() == null) {
					post.setAttachments(new ArrayList());
				}
				String imageIndex = Integer.toString(post.getAttachments().size()+1);
				String tempImagePath = "/attachment/" + post.getId() + "_" + imageIndex + "." + StringUtils.substringAfterLast(image.attr("src"), ".");
				post.getAttachments().add(new String[]{tempImagePath, image.attr("src")});
				image.attr("src", tempImagePath);
			}
		}

		Element hentry = blogPost.selectFirst("div.hentry");
		// 여기서 이런 증상 발생~!!! http://luckcrow.egloos.com/2146639
		if(hentry == null) {
			hentry = blogPost.clone();
			post.setDirty(true);
			logger.debug("dirty! url={}", post.getUrl());
			hentry.html("<h3>EgloosExodus : Html parsing 실패!</h3>\n\n" + hentry.html());

			//hentry = postTitleArea.nextElementSibling().child(0);	// 엉뚱한 놈 -.-

			/* jerry도 마찬가지 -.-
			Jerry doc = jerry(blogPost.html());
			logger.debug("doc.$(\"div.post_title_area\").html()={}", doc.$("div.post_title_area").html());
			logger.debug("doc.$(\"div.post_title_area\").html().siblings().first()={}", doc.$("div.post_title_area").siblings().first().html());

			logger.debug("doc.$(\"div.hentry\").html()={}", doc.$("div.hentry").html());
			hentry = new Element("div");
			hentry.html(doc.$("div.hentry").html());
			*/
		}

		post.setBodyText(hentry.text());
		post.setBodyHtml(hentry.html());
		Element comment = blogPost.select("ul.comment_list").first();
		if(comment != null) {
			post.setBodyHtml(post.getBodyHtml() + "\n\n<ul>" + comment.html() + "</ul>");
		}
		//logger.debug("post.getBodyText()={}", post.getBodyText());
		//logger.debug("post.getBodyHtml()={}", post.getBodyHtml());

		Element tagListContainer = blogPost.select("div.post_taglist").first();
		if(tagListContainer != null) {
			Elements tagAnchors = tagListContainer.select("a");
			for (Element tagAnchor : tagAnchors) {
				logger.debug("tagAnchor={}", tagAnchor.text());
				if("".equals(post.getTags())) {
					post.setTags("\"" + tagAnchor.text() + "\"");
				} else {
					post.setTags(post.getTags() + ",\"" + tagAnchor.text() + "\"");
				}
			}
		}

		Element prevPostAnchor = document.selectFirst("div.next > a");
		if(prevPostAnchor != null) {
			post.setPrevPostUrl("http://" + blog.getUserId() + ".egloos.com/" + prevPostAnchor.attr("href"));
		} else {
			logger.debug("마지막 postUrl={}", postUrl);
		}

		logger.debug("post.getUtcDate()={}, post.getTitle()={}", post.getUtcDate(), post.getTitle());
		return post;
	}

	/**
	 * @deprecated
	 * 해당 페이지의 글들의 내용을 읽어온다(이글루 블로그는 한 페이지에 여러개의 글이 있음)
	 * 마지막 블로그라면 null 반환
	 *
	 * @param blogPostUrl
	 * @return
	 */
	public List<Post> getBlogContent(String blogPostUrl) throws Exception {
		List<Post> postList = null;
		boolean isLastPost = false;

		Document document = Jsoup.connect(blogPostUrl).get();
		logger.debug("document.title()={}", document.title());

		/*
		Element link = document.select("body > div.body > div > div.header > h1 > a").get(0);
		logger.debug("blogName={}", link.text());
		*/

		//logger.debug("등록된 포스트가 없습니다?={}", document.select("div:contains(등록된 포스트가 없습니다.)"));

		// 더 이상 블로그가 없다면
		if(document.select("div:contains(등록된 포스트가 없습니다.)").size() > 0) {
			logger.debug("등록된 포스트가 없습니다.");
			return postList;
		}

		Elements blogsOfThePage = document.select("div.post_view");

		if(blogsOfThePage.size() > 0) {
			postList = new ArrayList<>();

			for (Element blogPost : blogsOfThePage) {
				//'신고' 삭제
				blogPost.selectFirst("span:matchesOwn(신고)").parent().parent().remove();

				Post post = new Post();
				//logger.debug("blogPost={}", blogPost);
				Element postTitleArea = blogPost.select("div.post_title_area").first();
				//logger.debug("entry-title={}", postTitleArea);
				Element title = blogPost.select("a").first();
				logger.debug("title={}", title.text());
				post.setTitle(title.text());
				post.setId(title.attr("name"));

				Element category = blogPost.selectFirst("span.post_title_category");
				post.setCategory(category.text().replace("/", "-"));	// 카테고리명에 path 문자있으면 - 로 변경해준다.

				Element date = blogPost.select("abbr").first();
				logger.debug("date={}", date.text());
				post.setDate(date.text());
				//Element commentCount = postTitleArea.select("span.txt").first();

				//logger.debug("덧글수={}", new StringTokenizer(commentCount.text(), ":").);
				if(postTitleArea.select("ul > li.post_info_cmtcount") != null
					&& postTitleArea.select("ul > li.post_info_cmtcount").size() > 0) {
					Element commentCount = postTitleArea.select("ul > li.post_info_cmtcount").first();
					logger.debug("덧글수=[{}]", commentCount.text().substring(commentCount.text().lastIndexOf(": ")+1).trim());
				} else {
					logger.debug("덧글수 없음");
				}

				Elements images = blogPost.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
				for (Element image : images) {
					/*
					System.out.println("\nsrc : " + image.attr("src"));
					System.out.println("height : " + image.attr("height"));
					System.out.println("width : " + image.attr("width"));
					System.out.println("alt : " + image.attr("alt"));
					*/
					// 이런 이미지들은 제외 http://md.egloos.com/img/icon/ico_badreport.png
					if(image.attr("src").contains("/md") == false
						&& image.attr("src").contains("/profile") == false ) {
						logger.debug(" \t img src={}", image.attr("src"));
						if(post.getAttachments() == null) {
							post.setAttachments(new ArrayList());
						}
						String imageIndex = Integer.toString(post.getAttachments().size()+1);
						String tempImagePath = "/attachment/" + post.getId() + "_" + imageIndex + "." + StringUtils.substringAfterLast(image.attr("src"), ".");
						post.getAttachments().add(new String[]{tempImagePath, image.attr("src")});
						image.attr("src", tempImagePath);
					}
				}

				Element hentry = blogPost.select("div.hentry").first();
				post.setBodyText(hentry.text());
				post.setBodyHtml(hentry.html());
				Element comment = blogPost.select("ul.comment_list").first();
				if(comment != null) {
					post.setBodyHtml(post.getBodyHtml() + "\n\n<ul>" + comment.html() + "</ul>");
				}
				logger.debug("post.getBodyText()={}", post.getBodyText());
				logger.debug("post.getBodyHtml()={}", post.getBodyHtml());

				Element tagListContainer = blogPost.select("div.post_taglist").first();
				if(tagListContainer != null) {
					Elements tagAnchors = tagListContainer.select("a");
					for (Element tagAnchor : tagAnchors) {
						logger.debug("tagAnchor={}", tagAnchor.text());
						if("".equals(post.getTags())) {
							post.setTags("\"" + tagAnchor.text() + "\"");
						} else {
							post.setTags(post.getTags() + ",\"" + tagAnchor.text() + "\"");
						}
					}
				}

				//logger.debug("markdownmarkdownmarkdownmarkdown\n{}\nmarkdownmarkdownmarkdownmarkdown", htmlToMd(hentry.html()));

				postList.add(post);
			}
		}

		return postList;
	}

	/**
	 * @deprecated
	 * 잘 동작하지 않더라. 그래서 md로 변환하지 않고 html 을 그대로 삽입할 것임
	 *
	 * @param html
	 * @return
	 * @throws Exception
	 */
	private String htmlToMd(String html) throws Exception {
		String mdStr = null;
		//ProcessBuilder builder = new ProcessBuilder("/home/bluedskim/Utils/shell/echoFromStdIn.sh");
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("/usr/bin/pandoc", "-f", "html+raw_html", "-t", "markdown");
		builder.redirectErrorStream(true);
		Process pr = builder.start();

		//Process pr = rt.exec("docker exec keen_wing pandoc");
		OutputStream stdin = pr.getOutputStream ();
		InputStream stderr = pr.getErrorStream ();
		InputStream stdout = pr.getInputStream ();
		stdin.write(html.getBytes());
		stdin.flush();
		stdin.close();

		pr.waitFor();

		mdStr = IOUtils.toString(stdout);
		//logger.debug("exitValue={}, out={}", pr.exitValue(), mdStr);
		//logger.debug("stderr={}", IOUtils.toString(stderr));

		stderr.close();
		stdout.close();

		pr.destroy();
		if (pr.isAlive()) {
			pr.destroyForcibly();
		}

		return mdStr;
	}

	public Boolean checkOwner(Blog blog, String checkOwnerString) throws Exception {
		String firstPostUrl = null;

		Document document = Jsoup.connect(blog.getBlogBaseUrl()).get();
		logger.debug("checkOwnerString={}, document.title()={}", checkOwnerString, document.title());
		Element anyElement = document.selectFirst("h2.entry-title:contains(" + checkOwnerString + ")");

		return new Boolean(anyElement != null);
	}
}
