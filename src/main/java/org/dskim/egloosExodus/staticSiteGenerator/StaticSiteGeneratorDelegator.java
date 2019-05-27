package org.dskim.egloosExodus.staticSiteGenerator;

import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.model.Post;

import java.io.IOException;

public interface StaticSiteGeneratorDelegator {
	void init(Blog blog, String themeName) throws Exception;
	String saveResourceFromUrl(String[] imageUrl) throws IOException;

	/**
	 *
	 * @param post
	 */
	void createPost(Post post) throws Exception;
	String generateStaticFles() throws Exception;
}
