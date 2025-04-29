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

import java.util.HashMap;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepo newsRepo;

    @Autowired
    private CloudinaryService cloudinaryService; // Service to handle file upload

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${userandgroup.base.url:http://userandgroup}")
    private String userandgroupBaseUrl;

    @Value("${notification.base.url:http://notification}")
    private String notificationBaseUrl;

    private void validateGroups(List<Integer> groupIds) {
        String groupIdsString = groupIds.stream().map(Object::toString).collect(Collectors.joining("&groupIds="));

        webClientBuilder.build().get()
                .uri(userandgroupBaseUrl + "/groups/validate?groupIds=" + groupIdsString)
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

        News savedNews = newsRepo.save(news);

        try{
            System.out.println("Sending notification to group with message: New news: " + headline + ", for groups: " + groupIds.stream().map(Object::toString).collect(Collectors.joining(", ")));
            Map<String, Object> notificationPayload = new HashMap<>();
            notificationPayload.put("message", "New news: " + headline);
            notificationPayload.put("groupIds", groupIds);

            webClientBuilder.build().post()
                    .uri(notificationBaseUrl + "/notifications/addnotification")
                    .bodyValue(notificationPayload)
                    .retrieve().bodyToMono(Void.class).block();
        }catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        // Save the news entry to the database
        return newsRepo.save(news);
    }

    // Get all news for a particular group
    public List<News> getNewsForGroup(Integer groupId) {
        return newsRepo.findByGroupIds(groupId);
    }
}
