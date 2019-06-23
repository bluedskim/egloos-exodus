package org.dskim.egloosExodus.repository;

import org.dskim.egloosExodus.model.Blog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BlogRepository extends PagingAndSortingRepository<Blog, Integer> {
    public List<Blog> findAllByIsDownloaded(boolean isDownloaded, Pageable pageable);
    public Blog findByUserIdAndServiceName(String userId, String serviceName);
    Long countByUserIdAndServiceName(String userId, String serviceName);
}
