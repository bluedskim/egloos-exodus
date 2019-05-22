package org.dskim.egloosExodus.staticSiteGenerator;

import org.dskim.egloosExodus.model.Post;

import java.io.IOException;

public interface StaticSiteGeneratorDelegator {
	void init(String baseDir, String blogName, String themeName) throws Exception;
	String saveImage(String[] imageUrl) throws IOException;

	/**
	 *
	 * @param post
	 */
	void createPost(Post post) throws Exception;
	void generateStaticFles() throws Exception;
}
