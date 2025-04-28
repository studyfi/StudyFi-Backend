package com.studyfi.notification.service;

import com.studyfi.notification.dto.NotificationDTO;
import com.studyfi.notification.model.Notification;
import com.studyfi.notification.repo.NotificationRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.ArrayList;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private WebClient webClient;

    public NotificationService(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        this.modelMapper.createTypeMap(Notification.class, NotificationDTO.class)
                .addMapping(Notification::isRead, NotificationDTO::setRead);
        this.modelMapper.createTypeMap(NotificationDTO.class, Notification.class)
                .addMapping(NotificationDTO::isRead, Notification::setRead);
    }

    public List<NotificationDTO> getAllNotifications(){
        List<Notification> notificationList = notificationRepo.findAll();
        return modelMapper.map(notificationList, new TypeToken<List<NotificationDTO>>(){}.getType());
    }

    public NotificationDTO sendNotification(NotificationDTO notificationDTO) {
        try{
            Notification notification = modelMapper.map(notificationDTO, Notification.class);
            Notification savedNotification = notificationRepo.save(notification);
            return modelMapper.map(savedNotification, NotificationDTO.class);
        } catch (Exception e){
            System.out.println("Error saving notification");
            return null;
        }
    }
    public List<NotificationDTO> getUserNotifications(Integer userId) {
        List<Notification> notificationList = notificationRepo.findByUserId(userId);

        return modelMapper.map(notificationList, new TypeToken<List<NotificationDTO>>() {
        }.getType());
    }

    public void sendNotificationToGroup(String message, List<Integer> groupIds) {
        for (Integer groupId : groupIds) {
            try {
                System.out.println("Entering sendNotificationToGroup for groupId: " + groupId);

                // Get users by group
                List<Integer> userIds;
                try {
                    String url = String.format("http://localhost:8082/groups/%s/users", groupId);
                    Mono<List<Integer>> response = webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Integer>>() {});
                    userIds = response.block();
                    System.out.println("Users by group: " + userIds);
                } catch (Exception e) {
                    System.out.println("Error in getUsersByGroup method: " + e);
                    userIds = List.of();
                }

                //Send a notification for each user
                if (userIds == null) {
                    userIds = new ArrayList<>();
                }
                if (userIds != null) {
                    for (Integer userId : userIds) {
                        try {
                            NotificationDTO notificationDTO = new NotificationDTO();
                            notificationDTO.setMessage(message);
                            notificationDTO.setUserId(userId);
                            notificationDTO.setRead(false);
                            sendNotification(notificationDTO);
                        } catch (Exception e) {
                            System.err.println("Error sending notification to user: " + userId);
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in sendNotificationToGroup for groupId: " + groupId);
                e.printStackTrace();
            }

        }
    }
}
