package org.dskim.egloosExodus.model;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.List;

@Data
@Document(collection = "blogs", schemaVersion = "1.0")
public class Blog {
	DateTime downloadStartDate = new DateTime();
	String serviceName; // ex) 네이버 블로그, 이글루스, 티스토리

	@Id String userId;	// 해당 서비스의 사용자 아이디(baseDir의 최하위 폴더명)
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
