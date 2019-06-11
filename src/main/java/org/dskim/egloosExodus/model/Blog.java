package org.dskim.egloosExodus.model;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class Blog {
	DateTime downloadStartDate = new DateTime();
	String serviceName; // ex) 네이버 블로그, 이글루스, 티스토리

	String userId;	// 해당 서비스의 사용자 아이디(baseDir의 최하위 폴더명)
	String blogName;
	String email;
	String blogBaseUrl;
	Post currentPost;
	int currentPostNumber = 0;
	//List<Post> postList;

	public Blog(String userId) {
		this.userId = userId;
	}
}
