package com.studyfi.notification.service;

import com.studyfi.notification.dto.NotificationDTO;
import com.studyfi.notification.model.Notification;
import com.studyfi.notification.repo.NotificationRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class NotificationService {
    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private ModelMapper modelMapper;

    public List<NotificationDTO> getAllNotifications(){
        List<Notification> notificationList = notificationRepo.findAll();
        return modelMapper.map(notificationList, new TypeToken<List<NotificationDTO>>(){}.getType());
    }

    public NotificationDTO saveNotification(NotificationDTO notificationDTO){
        notificationRepo.save(modelMapper.map(notificationDTO, Notification.class));
        return notificationDTO;
    }
}
