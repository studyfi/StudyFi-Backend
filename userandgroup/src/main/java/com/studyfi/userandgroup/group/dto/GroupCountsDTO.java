package com.studyfi.userandgroup.group.dto;

public class GroupCountsDTO {
    private Integer contentCount;
    private Integer newsCount;
    private Integer userCount;

    public GroupCountsDTO() {
    }

    public GroupCountsDTO(Integer contentCount, Integer newsCount, Integer userCount) {
        this.contentCount = contentCount;
        this.newsCount = newsCount;
        this.userCount = userCount;
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
}