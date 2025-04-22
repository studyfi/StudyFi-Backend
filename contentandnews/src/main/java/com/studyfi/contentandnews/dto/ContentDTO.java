package com.studyfi.contentandnews.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContentDTO {

    private Integer id;
    private String title;
    private String content;   // Description or any additional information
    private String author;    // Author of the content (user who uploaded)
    private List<Integer> groupIds; // List of Group IDs where the content is shared
    private String fileURL; // File URL (from Cloudinary file storage)

}
