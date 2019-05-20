package org.dskim.egloosExodus.model;

import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

@Data
public class Post {
	String id;		// 고유 아이디
	String title;
	String date;	// 2003/10/16 00:16
	String featuredImage = "";
	String description = "";
	String bodyText;
	String bodyHtml;
	String category;
	String tags = "";
	DateTimeFormatter readFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm");
	DateTimeFormatter printFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
	List<String[]> attachments;	// 첨부파일 정보 {"상대 경로", "절대 경로"}

	public String getUtcDate() {
		return printFormatter.print(readFormatter.parseDateTime(date));
	}
}
