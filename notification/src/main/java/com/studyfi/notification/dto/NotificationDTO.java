package com.studyfi.notification.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Integer id;
    private String message;
    private boolean read;
    private LocalDateTime timestamp;
    private Integer userId;
    private Integer groupId;
    private String groupName;

    // Constructors
    public NotificationDTO() {
    }

    public NotificationDTO(String message, Integer userId) {
        this.message = message;
        this.userId = userId;
    }

    public NotificationDTO(Integer id, String message, LocalDateTime timestamp,Integer userId) {
        this.id = id;
        this.message = message;
        this.read = false;
        this.timestamp = timestamp;
        this.userId = userId;
    }
    public NotificationDTO(Integer id, String message, boolean read, LocalDateTime timestamp, Integer userId) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.timestamp = timestamp;
        this.userId = userId;
    }
    public NotificationDTO(Integer id, String message, boolean read, Integer userId) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.userId = userId;
    }
    public NotificationDTO(Integer id, String message, boolean read, LocalDateTime timestamp, Integer userId,Integer groupId, String groupName) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.timestamp = timestamp;
        this.userId = userId;
        this.groupId = groupId;
        this.groupName= groupName;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
