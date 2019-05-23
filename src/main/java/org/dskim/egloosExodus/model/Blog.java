package org.dskim.egloosExodus.model;

import lombok.Data;

import java.util.List;

@Data
public class Blog {
	String serviceName; // ex) 네이버 블로그, 이글루스, 티스토리
	String blogName;
	String blogBaseUrl;
	List<Post> postList;
}
