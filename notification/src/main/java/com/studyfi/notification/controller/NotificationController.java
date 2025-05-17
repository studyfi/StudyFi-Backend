package com.studyfi.notification.controller;

import com.studyfi.notification.dto.NotificationDTO;
import com.studyfi.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/getnotifications")
    public List<NotificationDTO> getNotifications() {
        return notificationService.getAllNotifications();
    }

    @PostMapping("/addnotification")
    public void addNotification(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        Integer groupId = (Integer) payload.get("groupId");
        Integer excludeUserId = (Integer) payload.get("excludeUserId");

        if (message == null || groupId == null) {
            throw new IllegalArgumentException("Message and groupId are required");
        }

        notificationService.sendNotificationToGroup(message, groupId, excludeUserId);
    }

    @PostMapping("/addnotification/users")
    public void addNotificationForUsers(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        List<Integer> userIds = (List<Integer>) payload.get("userIds");
        Integer groupId = (Integer) payload.get("groupId");

        if (message == null || userIds == null || groupId == null) {
            throw new IllegalArgumentException("Message, userIds, and groupId are required");
        }

        notificationService.sendNotificationToUsers(message, userIds, groupId);
    }

    @GetMapping("/getnotifications/{userId}")
    public Map<String, List<NotificationDTO>> getUserNotifications(@PathVariable Integer userId) {
        List<NotificationDTO> allNotifications = notificationService.getUserNotifications(userId);
        List<NotificationDTO> readNotifications = allNotifications.stream().filter(NotificationDTO::isRead).toList();
        List<NotificationDTO> unreadNotifications = allNotifications.stream().filter(notificationDTO -> !notificationDTO.isRead()).toList();
        Map<String, List<NotificationDTO>> result = new HashMap<>();
        result.put("readNotifications", readNotifications);
        result.put("unreadNotifications", unreadNotifications);
        return result;
    }

    @GetMapping("/getnotifications/{userId}/latest")
    public List<NotificationDTO> getLatestUserNotifications(@PathVariable Integer userId) {
        return notificationService.getLatestUserNotifications(userId);
    }

    @PostMapping("/markallasread/{userId}")
    public Map<String, String> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        Map<String, String> result = new HashMap<>();
        result.put("message", "All notifications marked as read");
        return result;
    }

    @DeleteMapping("/remove/{groupId}/user/{userId}")
    public ResponseEntity<?> removeNotificationsByGroupIdAndUserId(
            @PathVariable Integer groupId,
            @PathVariable Integer userId) {
        notificationService.removeNotificationsByGroupIdAndUserId(groupId, userId);
        return ResponseEntity.ok().build();
    }
}