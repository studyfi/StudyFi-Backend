package com.studyfi.contentandnews.controller;

import com.studyfi.contentandnews.dto.ContentDTO;
import com.studyfi.contentandnews.model.Content;
import com.studyfi.contentandnews.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    // Endpoint to upload new content with file
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ContentDTO uploadContent(@RequestParam String title,
                                    @RequestParam String contentText,
                                    @RequestParam String author,
                                    @RequestParam("groupIds[]") List<Integer> groupIds, // Handle multiple groupIds[]
                                    @RequestParam MultipartFile file) throws IOException {
        Content content = contentService.uploadContent(title, contentText, author, groupIds, file);

        // Convert Content to ContentDTO before sending the response
        return convertToDTO(content);
    }

    // Endpoint to get content for a specific group
    @GetMapping("/group/{groupId}")
    public List<ContentDTO> getContentForGroup(@PathVariable Integer groupId) {
        List<Content> contentList = contentService.getContentForGroup(groupId);

        // Convert the list of Content to ContentDTO before returning the response
        return contentList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // New endpoint to get the number of contents for a specific group
    @GetMapping("/group/{groupId}/count")
    public Integer getCountForGroup(@PathVariable Integer groupId) {
        return contentService.getContentForGroup(groupId).size();
    }

    // Method to convert Content to ContentDTO
    private ContentDTO convertToDTO(Content content) {
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setId(content.getId());
        contentDTO.setTitle(content.getTitle());
        contentDTO.setContent(content.getContent());
        contentDTO.setAuthor(content.getAuthor());
        contentDTO.setGroupIds(content.getGroupIds());
        contentDTO.setFileURL(content.getFileURL());
        return contentDTO;
    }
}
