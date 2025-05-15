package com.studyfi.contentandnews.chat.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "postId"}, name = "uq_user_post_like")
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeId;
    private Integer userId;
    private Integer postId;
}
