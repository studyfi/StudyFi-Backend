package com.studyfi.contentandnews.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewsDTO {

    private Integer id;
    private String headline;  // Title or headline of the news
    private String content;   // Body of the news (text or URL)
    private String author;    // Author of the news (user who posted)
    private List<Integer> groupIds; // List of Group IDs where the news is shared
    private String imageUrl; // Optional image URL for the news article

}
