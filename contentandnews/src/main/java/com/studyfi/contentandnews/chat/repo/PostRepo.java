package com.studyfi.contentandnews.chat.repo;

import com.studyfi.contentandnews.chat.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepo extends JpaRepository<Post, Integer> {
    //List<Post> findByGroupId(Integer groupId);
    Page<Post> findByGroupId(Integer groupId, Pageable pageable);
}
