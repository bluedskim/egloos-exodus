package org.dskim.egloosExodus.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 이글루스에서 posting을 읽어온다.
 * @deprecated
 */
@Component
public class EgloosPostListReader {
	private static final Logger logger = LogManager.getLogger(EgloosPostListReader.class);

	@Autowired
	ConcurrentSkipListMap blogList;

	/**
	 * 게시물 목록을 가져온다
	 * 모든 링크들을 가져오고 해당 블로그의 글들만 필터링하자
	 */
	public ConcurrentSkipListMap getPostList(String url) throws IOException {
		logger.debug("url={}", url);

		Document document = Jsoup.connect(url).get();
		Elements links = document.select("a[href]");

		for (Element link : links) {
			logger.debug(" \t text={} link={}", link.text(), link.attr("href"));
			if (link.attr("href").indexOf("/page") == 0) {
				logger.debug(" \t \t bingo!");
				blogList.put(link.text(), link);
			}
		}

		return blogList;
	}
}
