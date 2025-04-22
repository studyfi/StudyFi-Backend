package com.studyfi.contentandnews.repo;

import com.studyfi.contentandnews.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NewsRepo extends JpaRepository<News, Integer> {

    // Custom query to fetch news by group IDs
    List<News> findByGroupIds(Integer groupId);
}
