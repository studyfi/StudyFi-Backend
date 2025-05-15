package com.studyfi.contentandnews.chat.repo;

import com.studyfi.contentandnews.chat.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepo extends JpaRepository<Like, Integer> {
    int countByPostId(Integer postId);
    Optional<Like> findByUserIdAndPostId(Integer userId, Integer postId);
    List<Like> findByPostId(Integer postId);
}
