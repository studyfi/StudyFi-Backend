package com.studyfi.contentandnews.chat.dto;

import lombok.Data;

@Data
public class LikeDTO {
    private Integer likeId;
    private Integer userId;
    private Integer postId;
}
