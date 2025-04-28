package com.studyfi.notification.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications") // Optional: if you want a specific table name
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer id;

    @Column(name = "message")
    private String message;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "is_read")
    private Boolean read = false;


    // Constructors

    public Notification() {
    }

    public Notification(String message, Integer userId) {
        this.message = message;
        this.userId = userId;
    }

    public Notification(Integer id, String message, Integer userId) {
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

    public Boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
