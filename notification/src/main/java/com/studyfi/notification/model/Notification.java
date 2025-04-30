package com.studyfi.notification.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    private Boolean isRead = false;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "group_name")
    private String groupName;


    // Constructors

    public Notification() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public Notification(String message, Integer userId,Integer groupId, String groupName) {
        this.message = message;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Notification(Integer id, String message, Integer userId, Integer groupId,String groupName) {
        this.id = id;
        this.message = message;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }public Notification(Integer id, String message, Boolean isRead, Integer userId, LocalDateTime timestamp, Integer groupId, String groupName) {
        this.id = id;
        this.groupId = groupId;
        this.message = message;
        this.userId = userId;
        this.timestamp = timestamp;
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
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
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
