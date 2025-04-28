package com.studyfi.notification.dto;

public class NotificationDTO {
    private Integer id;
    private String message;
    private boolean read;
    private Integer userId;

    // Constructors
    public NotificationDTO() {
    }

    public NotificationDTO(String message, Integer userId) {
        this.message = message;
        this.userId = userId;
    }

    public NotificationDTO(Integer id, String message, Integer userId) {
        this.id = id;
        this.message = message;
        this.read = false;
        this.userId = userId;
    }
    public NotificationDTO(Integer id, String message, boolean read, Integer userId) {
        this.id = id;
        this.message = message;
        this.userId = userId;
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
}
