package com.studyfi.notification.controller;

import com.studyfi.notification.dto.NotificationDTO;
import com.studyfi.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "api/v1")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/getnotifications")
    public List<NotificationDTO> getNotifications(){
        return notificationService.getAllNotifications();
    }

    @PostMapping("/addnotification")
    public NotificationDTO saveUser(@RequestBody NotificationDTO notificationDTO){
        return notificationService.saveNotification(notificationDTO);
    }
}
