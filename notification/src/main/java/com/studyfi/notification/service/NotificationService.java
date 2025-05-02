package com.studyfi.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyfi.notification.dto.NotificationDTO;
import com.studyfi.notification.model.Notification;
import com.studyfi.notification.repo.NotificationRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;import java.time.LocalDateTime;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.transaction.annotation.Propagation;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public NotificationService(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        this.modelMapper.createTypeMap(Notification.class, NotificationDTO.class)
                .addMapping(Notification::isRead, NotificationDTO::setRead).addMapping(Notification::getTimestamp, NotificationDTO::setTimestamp);
        this.modelMapper.createTypeMap(NotificationDTO.class, Notification.class)
                .addMapping(NotificationDTO::isRead, Notification::setRead) ;
    }

    public List<NotificationDTO> getAllNotifications(){
        List<Notification> notificationList = notificationRepo.findAll();
        return modelMapper.map(notificationList, new TypeToken<List<NotificationDTO>>(){}.getType());
    }

    public List<NotificationDTO> getLatestUserNotifications(Integer userId) {
        List<Notification> notifications = notificationRepo.findTop10ByUserIdOrderByTimestampDesc(userId);
        return convertToDTOs(notifications);
    }
    private List<NotificationDTO> convertToDTOs(List<Notification> notifications) {
        return modelMapper.map(notifications, new TypeToken<List<NotificationDTO>>() {}.getType());
    }

    public NotificationDTO sendNotification(NotificationDTO notificationDTO, Integer groupId, String groupName) {
        try {
            Notification notification = new Notification();
            notification.setMessage(notificationDTO.getMessage());
            notification.setUserId(notificationDTO.getUserId());
            notification.setRead(notificationDTO.isRead());
            notification.setTimestamp(LocalDateTime.now());
            notification.setGroupId(groupId);
            notification.setGroupName(groupName);


            Notification savedNotification = notificationRepo.save(notification);
            return modelMapper.map(savedNotification, NotificationDTO.class);
        } catch (Exception e) {
            System.out.println("Error saving notification");
            return null;
        }
    }
    public void markAllAsRead(Integer userId) {
        List<Notification> unreadNotifications = notificationRepo.findByUserIdAndIsReadFalse(userId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepo.saveAll(unreadNotifications);
    }

    public List<NotificationDTO> getUserNotifications(Integer userId) {
        List<Notification> notificationList = notificationRepo.findByUserId(userId);

        return modelMapper.map(notificationList, new TypeToken<List<NotificationDTO>>() {
        }.getType());
    }

    public void sendNotificationToGroup(String message, List<Integer> groupIds)  {
        for (Integer groupId : groupIds) {
            System.out.println("Entering sendNotificationToGroup for groupId: " + groupId);

            // Get users by group
            List<Integer> userIds;
            try {
                String url = String.format("http://apigateway/api/v1/groups/%s/userids", groupId);
                Mono<List<Integer>> response = webClientBuilder.build().get()
                        .uri(url)
                        .exchangeToMono(clientResponse -> {
                            if (clientResponse.statusCode().isError()) {
                                System.out.println("ERROR: Users by group");
                                return Mono.just(List.of());
                            } else {
                                return clientResponse.bodyToMono(new ParameterizedTypeReference<List<Integer>>() {
                                });
                            }
                        });
                userIds = response.block();
                System.out.println("Users by group: " + userIds);
            } catch (Exception e) {
                System.out.println("Error in getUsersByGroup method: " + e);
                userIds = List.of();
            }

            if (userIds != null) {
                for (Integer userId : userIds) {
                    NotificationDTO notificationDTO = new NotificationDTO();
                    String groupName = "";
                    try {
                        String urlGroupName = String.format("http://apigateway/api/v1/groups/%s", groupId);
                        groupName = webClientBuilder.build().get().uri(urlGroupName)
                                .exchangeToMono(response -> {
                                    if (response.statusCode().equals(HttpStatus.OK)) {
                                        return response.bodyToMono(JsonNode.class);
                                    } else {
                                        return Mono.empty();
                                    }
                                }).map(jsonNode -> jsonNode.get("name").asText())
                                .block();
                    } catch (Exception e) {
                        System.out.println("Error in Group Name : " + e);
                    }

                    message = message.replace("[group name]", groupName);
                    notificationDTO.setMessage(message);
                    notificationDTO.setUserId(userId);
                    notificationDTO.setRead(false);
                    try {
                        sendNotification(notificationDTO,groupId,groupName);
                    } catch (Exception e) {
                        System.err.println("Error sending notification to user: " + userId + e.getMessage());
                    }
                }
            }
        }
    }

    public void removeNotificationsByGroupIdAndUserId(Integer groupId, Integer userId) {
        notificationRepo.deleteByGroupIdAndUserId(groupId, userId);
    }

}
