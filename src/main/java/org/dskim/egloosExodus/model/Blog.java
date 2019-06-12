package org.dskim.egloosExodus.model;

import lombok.Data;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.joda.time.DateTime;

@Data
public class Blog implements Mappable {
	DateTime downloadStartDate = new DateTime();
	String serviceName; // ex) 네이버 블로그, 이글루스, 티스토리

	@Id
	String userId;	// 해당 서비스의 사용자 아이디(baseDir의 최하위 폴더명)
	String blogName;
	String email;
	String blogBaseUrl;
	Post currentPost;
	String currentPostUrl;
	int currentPostNumber = 0;
	//List<Post> postList;

	public Blog() {
		super();
	}

	public Blog(String userId) {
		super();
		this.userId = userId;
	}

	@Override
	public Document write(NitriteMapper mapper) {
		Document document = new Document();
		document.put("userId", userId);
		document.put("blogName", blogName);
		document.put("email", email);
		document.put("blogBaseUrl", blogBaseUrl);
		document.put("currentPostUrl", currentPostUrl);
		//document.put("currentPost", currentPost);
		document.put("currentPostNumber", currentPostNumber);
		return document;
	}

	@Override
	public void read(NitriteMapper mapper, Document document) {
		if (document != null) {
			userId = (String)document.get("userId");
			blogName = (String)document.get("blogName");
			email = (String)document.get("email");
			blogBaseUrl = (String)document.get("blogBaseUrl");
			currentPostUrl = (String)document.get("currentPostUrl");
			//currentPost = (Post)document.get("currentPost");
			currentPostNumber = (Integer)document.get("currentPostNumber");
		}
	}
}
