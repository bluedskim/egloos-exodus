package org.dskim.egloosExodus.springDataJsondb.jsondbRepo;

import org.dskim.egloosExodus.model.Blog;
import org.mambofish.spring.data.jsondb.repository.JsonDBRepository;

public interface BlogRepo extends JsonDBRepository<Blog, String> {
}
