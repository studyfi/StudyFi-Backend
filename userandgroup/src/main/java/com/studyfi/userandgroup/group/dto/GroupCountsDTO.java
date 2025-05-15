package com.studyfi.userandgroup.group.dto;

public class GroupCountsDTO {
    private Integer contentCount;
    private Integer newsCount;
    private Integer userCount;
    private Integer chatCount;

    public GroupCountsDTO() {
    }

    public GroupCountsDTO(Integer contentCount, Integer newsCount, Integer userCount, Integer chatCount) {
        this.contentCount = contentCount;
        this.newsCount = newsCount;
        this.userCount = userCount;
        this.chatCount = chatCount;
    }

    public Integer getContentCount() {
        return contentCount;
    }

    public Integer getNewsCount() {
        return newsCount;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public Integer getChatCount() {
        return chatCount;
    }
}