package org.dskim.egloosExodus.model;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Data
@Entity
public class Blog {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Integer id;

	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	DateTime downloadStartDate ;

	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	DateTime downloadEndDate ;

	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	DateTime regDate ;

	String serviceName; // ex) 네이버 블로그, 이글루스, 티스토리

	String userId;	// 해당 서비스의 사용자 아이디(baseDir의 최하위 폴더명)
	String blogName;
	String email;
	String blogBaseUrl;
	@Transient
	Post currentPost;
	String currentPostUrl;
	int currentPostNumber = 0;
	boolean isDownloaded = false;
	// 압축파일 사이즈 mb
	long fileSize;
	//List<Post> postList;

	public Blog() {
		super();
		regDate = new DateTime();
	}

	public Blog(String userId) {
		this();
		this.userId = userId;
	}
}
