package org.dskim.egloosExodus.model;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
public class Blog {
	Date downloadStartDate = new Date();
	String serviceName; // ex) 네이버 블로그, 이글루스, 티스토리
	String blogName;
	String email;
	String blogBaseUrl;
	List<Post> postList;
}
