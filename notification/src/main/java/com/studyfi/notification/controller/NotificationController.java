package com.studyfi.notification.controller;

import com.studyfi.notification.dto.NotificationDTO;
import com.studyfi.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/getnotifications")
    public List<NotificationDTO> getNotifications(){
        return notificationService.getAllNotifications();
    }
    @PostMapping("/addnotification")
    public void addNotification(@RequestBody java.util.Map<String, Object> payload){
        notificationService.sendNotificationToGroup((String) payload.get("message"), (List<Integer>) payload.get("groupIds"));
    }

    @GetMapping("/getnotifications/{userId}")
    public java.util.Map<String, List<NotificationDTO>> getUserNotifications(@PathVariable Integer userId) {
        List<NotificationDTO> allNotifications = notificationService.getUserNotifications(userId);
        List<NotificationDTO> readNotifications = allNotifications.stream().filter(NotificationDTO::isRead).toList();
        List<NotificationDTO> unreadNotifications = allNotifications.stream().filter(notificationDTO -> !notificationDTO.isRead()).toList();
        java.util.Map<String, List<NotificationDTO>> result = new java.util.HashMap<>();
        result.put("readNotifications", readNotifications);
        result.put("unreadNotifications", unreadNotifications);
        return result;
    }
}
