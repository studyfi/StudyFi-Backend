package com.studyfi.contentandnews.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Integer postId;
    private Integer groupId;
    private String content;
    private LocalDateTime timestamp;
    private UserDTO user;
    private Integer likeCount;
    private Integer commentCount;
}
