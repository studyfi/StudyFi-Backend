package com.studyfi.contentandnews.controller;

import com.studyfi.contentandnews.dto.NewsDTO;
import com.studyfi.contentandnews.model.News;
import com.studyfi.contentandnews.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    // Endpoint to post news with optional image
    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsDTO postNews(@RequestParam String headline,
                            @RequestParam String contentText,
                            @RequestParam String author,
                            @RequestParam("groupIds[]") List<Integer> groupIds,
                            @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        // Post news, passing the image file if it exists
        News news = newsService.postNews(headline, contentText, author, groupIds, imageFile);

        // Convert News to NewsDTO before sending the response
        return convertToDTO(news);
    }

    // Endpoint to get news for a specific group
    @GetMapping("/group/{groupId}")
    public List<NewsDTO> getNewsForGroup(@PathVariable Integer groupId) {
        List<News> newsList = newsService.getNewsForGroup(groupId);

        // Convert the list of News to NewsDTO before returning the response
        return newsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Method to convert News to NewsDTO
    private NewsDTO convertToDTO(News news) {
        NewsDTO newsDTO = new NewsDTO();
        newsDTO.setId(news.getId());
        newsDTO.setHeadline(news.getHeadline());
        newsDTO.setContent(news.getContent());
        newsDTO.setAuthor(news.getAuthor());
        newsDTO.setGroupIds(news.getGroupIds());
        newsDTO.setImageUrl(news.getImageUrl()); // Set image URL in the DTO
        return newsDTO;
    }
}
