package com.studyfi.contentandnews.repo;

import com.studyfi.contentandnews.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentRepo extends JpaRepository<Content, Integer> {

    // Custom query to fetch content by group IDs
    List<Content> findByGroupIds(Integer groupId);
}
