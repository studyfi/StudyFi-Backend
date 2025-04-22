package com.studyfi.contentandnews.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String headline;  // Title or headline of the news
    private String content;   // Body of the news (text or URL)
    private String author;    // Author of the news (user who posted)

    @ElementCollection
    private List<Integer> groupIds; // List of Group IDs where the news is shared

    private Date createdAt;

    private String imageUrl; // URL of the image associated with the news (optional)

    // Getters and Setters (Lombok will generate these automatically)
}
