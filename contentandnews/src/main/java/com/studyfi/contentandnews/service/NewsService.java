package com.studyfi.contentandnews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyfi.contentandnews.model.News;
import com.studyfi.contentandnews.repo.NewsRepo;
import com.studyfi.contentandnews.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepo newsRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${userandgroup.base.url:http://userandgroup}")
    private String userandgroupBaseUrl;

    @Value("${notification.base.url:http://notification}")
    private String notificationBaseUrl;

    private void validateGroups(List<Integer> groupIds) {
        String groupIdsString = groupIds.stream().map(Object::toString).collect(Collectors.joining("&groupIds="));

        webClientBuilder.build().get()
                .uri(userandgroupBaseUrl + "/api/v1/groups/validate?groupIds=" + groupIdsString)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new RuntimeException("Invalid groups")))
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error validating groups", e)))
                .block();
    }

    // Method to post news
    public News postNews(String headline, String contentText, String author, List<Integer> groupIds, MultipartFile imageFile) throws IOException {
        validateGroups(groupIds);

        News news = new News();
        news.setHeadline(headline);
        news.setContent(contentText);
        news.setAuthor(author);
        news.setGroupIds(groupIds);
        news.setCreatedAt(new Date());

        // If an image file is provided, upload it and set the image URL
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(imageFile);
            news.setImageUrl(imageUrl);
        }

        // Save the news entry to the database
        News savedNews = newsRepo.save(news);

        // Send notification for each group
        for (Integer groupId : groupIds) {
            try {
                System.out.println("Sending notification to group " + groupId + " with message: New news: " + headline);
                Map<String, Object> notificationPayload = new HashMap<>();
                notificationPayload.put("message", "New news: " + headline + ", by " + author);
                notificationPayload.put("groupId", groupId);
                // Note: excludeUserId is not included since author is a string, not a user ID
                // Later implementation - map author to a user ID, add it here as notificationPayload.put("excludeUserId", authorUserId);

                webClientBuilder.build().post()
                        .uri(notificationBaseUrl + "/api/v1/notifications/addnotification")
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

        return savedNews;
    }

    // Get all news for a particular group
    public List<News> getNewsForGroup(Integer groupId) {
        return newsRepo.findByGroupIds(groupId);
    }
}