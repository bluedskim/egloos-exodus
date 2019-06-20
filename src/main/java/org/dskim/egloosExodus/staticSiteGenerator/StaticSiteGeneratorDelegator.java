package org.dskim.egloosExodus.staticSiteGenerator;

import org.dskim.egloosExodus.model.Blog;
import org.dskim.egloosExodus.model.Post;

import java.io.IOException;

public interface StaticSiteGeneratorDelegator {
	void init(Blog blog) throws Exception;
	String saveResourceFromUrl(Blog blog, String[] imageUrl) throws IOException;

	/**
	 *
	 * @param post
	 */
	void createPost(Blog blog, Post post) throws Exception;
	String generateStaticFles(Blog blog) throws Exception;
}
