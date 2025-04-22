package com.studyfi.contentandnews.service;

import com.studyfi.contentandnews.model.Content;
import com.studyfi.contentandnews.repo.ContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class ContentService {

    @Autowired
    private ContentRepo contentRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Method to upload new content with file
    public Content uploadContent(String title, String contentText, String author, List<Integer> groupIds, MultipartFile file) throws IOException {
        // Validate file size before upload
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

        return contentRepo.save(content);
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
