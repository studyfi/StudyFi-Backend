package com.studyfi.notification.dto;

public class NotificationDTO {
    private Integer id;
    private String message;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
