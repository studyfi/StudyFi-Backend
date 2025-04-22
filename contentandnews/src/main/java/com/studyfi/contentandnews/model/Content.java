package com.studyfi.contentandnews.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;   // Title of the content
    private String content; // Content text (optional, can be used for description)
    private String author;  // Author of the content (user who uploaded)

    @ElementCollection
    private List<Integer> groupIds; // List of Group IDs where the content is shared

    private Date createdAt;  // Timestamp when the content was created/posted

    private String fileURL; // URL to the file stored on Cloudinary or other file storage

    // Getters and Setters (lombok handles this automatically with @Data)
}
