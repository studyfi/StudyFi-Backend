package com.studyfi.contentandnews.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Integer commentId;
    private Integer postId;
    private String content;
    private LocalDateTime timestamp;
    private UserDTO user;
}
