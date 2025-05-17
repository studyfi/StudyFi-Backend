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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                            .host("apigateway")
                            .path("/api/v1/groups/validate")
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
        content.setFileURL(fileUrl);

        Content savedContent = contentRepo.save(content);

        // Send notification for each group
        for (Integer groupId : groupIds) {
            try {
                System.out.println("Sending notification to group " + groupId + " with message: New content: " + title);
                Map<String, Object> notificationPayload = new HashMap<>();
                notificationPayload.put("message", "New content: " + title + ", by " + author);
                notificationPayload.put("groupId", groupId);
                // Note: excludeUserId is not included since author is a string, not a user ID
                // If you can map author to a user ID, add it here as notificationPayload.put("excludeUserId", authorUserId);

                webClientBuilder.build()
                        .post()
                        .uri("http://apigateway/api/v1/notifications/addnotification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(notificationPayload)
                        .retrieve()
                        .onStatus(status -> status.isError(),
                                response -> response.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Failed to send notification: " + body)))
                        .bodyToMono(Void.class)
                        .block();
            } catch (Exception e) {
                System.err.println("Error sending notification for group " + groupId + ": " + e.getMessage());
            }
        }

        return savedContent;
    }

    public void validateFileSize(MultipartFile file) throws IllegalArgumentException {
        long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the limit of 10 MB.");
        }
    }

    // Get all content for a particular group
    public List<Content> getContentForGroup(Integer groupId) {
        return contentRepo.findByGroupIds(groupId);
    }
}