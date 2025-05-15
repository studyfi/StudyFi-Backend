package com.studyfi.contentandnews.chat.repo;

import com.studyfi.contentandnews.chat.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment, Integer> {
    int countByPostId(Integer postId);
    List<Comment> findByPostId(Integer postId);
}
