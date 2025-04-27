package com.studyfi.contentandnews.service;

import com.studyfi.contentandnews.model.Content;
import com.studyfi.contentandnews.repo.ContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class ContentService {

    @Autowired
    private ContentRepo contentRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private void validateGroups(List<Integer> groupIds) {
        try {
            webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost")
                            .port(8082)
                            .path("/groups/validate")
                            .queryParam("groupIds", groupIds.toArray())
                            .build())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Invalid group");
        }
    }

    // Method to upload new content with file
    public Content uploadContent(String title, String contentText, String author, List<Integer> groupIds, MultipartFile file) throws IOException {
        // Validate file size before upload
        validateGroups(groupIds);

        validateFileSize(file);

        // Proceed with file upload and content creation
        String fileUrl = cloudinaryService.uploadFile(file);
        Content content = new Content();
        content.setTitle(title);
        content.setContent(contentText);
        content.setAuthor(author);
        content.setGroupIds(groupIds);
        content.setCreatedAt(new Date());
        content.setFileURL(fileUrl);  // Store the file URL in the content object

        Content savedContent = contentRepo.save(content);

        // Send notification
        System.out.println("sendNotificationToGroup is called with message: New content: " + title + " and groupIds: " + groupIds);
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8081/notifications/addnotification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("message", "New content: " + title, "groupIds", groupIds))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Log the error, but don't throw it to avoid breaking content creation
        }
        return savedContent;
    }

    public void validateFileSize(MultipartFile file) throws IllegalArgumentException {
        // Check if the file size exceeds 10 MB (10 * 1024 * 1024 bytes)
        long MAX_FILE_SIZE = 10 * 1024 * 1024;

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the limit of 10 MB.");
        }
    }


    // Get all content for a particular group
    public List<Content> getContentForGroup(Integer groupId) {
        return contentRepo.findByGroupIds(groupId);
    }
}
