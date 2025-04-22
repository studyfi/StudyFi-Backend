package com.studyfi.contentandnews.service;

import com.studyfi.contentandnews.model.News;
import com.studyfi.contentandnews.repo.NewsRepo;
import com.studyfi.contentandnews.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class NewsService {

    @Autowired
    private NewsRepo newsRepo;

    @Autowired
    private CloudinaryService cloudinaryService; // Service to handle file upload

    // Method to post news
    public News postNews(String headline, String contentText, String author, List<Integer> groupIds, MultipartFile imageFile) throws IOException {
        News news = new News();
        news.setHeadline(headline);
        news.setContent(contentText);
        news.setAuthor(author);
        news.setGroupIds(groupIds);
        news.setCreatedAt(new Date());

        // If an image file is provided, upload it and set the image URL
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(imageFile); // Upload to Cloudinary and get the URL
            news.setImageUrl(imageUrl); // Set the image URL in the news entity
        }

        // Save the news entry to the database
        return newsRepo.save(news);
    }

    // Get all news for a particular group
    public List<News> getNewsForGroup(Integer groupId) {
        return newsRepo.findByGroupIds(groupId);
    }
}
