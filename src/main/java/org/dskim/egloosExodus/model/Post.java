package org.dskim.egloosExodus.model;

import lombok.Data;
import lombok.ToString;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.List;

@Data
@ToString(exclude = "featuredImage, description, bodyText, bodyHtml, category, tags, prevPostUrl, postList, readFormatter, printFormatter")
public class Post implements Serializable {
	String id;		// 고유 아이디
	String title;
	String date;	// 2003/10/16 00:16
	String featuredImage = "";
	String description = "";
	String bodyText;
	String bodyHtml;
	String category;
	String tags = "";
	String url;
	String prevPostUrl;
	transient DateTimeFormatter readFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm");
	transient DateTimeFormatter printFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
	List<String[]> attachments;	// 첨부파일 정보 {"상대 경로", "절대 경로"}
	// 해당 html 더러워서 파싱 불가. 그래서 파싱 가능한 최소 범위를 가져옴.
	boolean dirty;

	public String getUtcDate() {
		return printFormatter.print(readFormatter.parseDateTime(date));
	}

	public String getFeaturedImage() {
		if(getAttachments() != null) {
			for (String[] attachment : getAttachments()) {
				String tempImagePath = attachment[0].toLowerCase();
				if(tempImagePath.indexOf("jpg") >= 0
					|| tempImagePath.indexOf("png") >= 0
					|| tempImagePath.indexOf("gif") >= 0) {
					featuredImage = attachment[0];
					break;
				}
			}
		}
		//return featuredImage;
		return "";	// 헤더에 이미지가 표시되지 않는 문제있어 일단 사용하지 않음
	}
}
