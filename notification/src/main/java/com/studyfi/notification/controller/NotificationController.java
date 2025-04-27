package com.studyfi.notification.controller;

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
    public List<com.studyfi.notification.dto.NotificationDTO> getNotifications(){
        return notificationService.getAllNotifications();
    }
    @PostMapping("/addnotification")
    public void addNotification(@RequestBody java.util.Map<String, Object> payload){
        notificationService.sendNotificationToGroup((String) payload.get("message"), (List<Integer>) payload.get("groupIds"));
    }
}
