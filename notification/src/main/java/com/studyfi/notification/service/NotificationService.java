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

    public void sendNotificationToGroup(String message, Integer groupId, Integer excludeUserId) {
        if (groupId == null) {
            System.out.println("Group ID is null, cannot send notification");
            return;
        }
        System.out.println("Entering sendNotificationToGroup for groupId: " + groupId);

        // Get users by group
        List<Integer> userIds;
        try {
            String url = String.format("http://apigateway/api/v1/groups/%s/userids", groupId);
            Mono<List<Integer>> response = webClientBuilder.build().get()
                    .uri(url)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().isError()) {
                            System.out.println("ERROR: Users by group for groupId: " + groupId);
                            return Mono.just(List.of());
                        } else {
                            return clientResponse.bodyToMono(new ParameterizedTypeReference<List<Integer>>() {});
                        }
                    });
            userIds = response.block();
            System.out.println("Users in group " + groupId + ": " + userIds);
        } catch (Exception e) {
            System.out.println("Error in getUsersByGroup method: " + e.getMessage());
            userIds = List.of();
        }

        if (userIds != null && !userIds.isEmpty()) {
            // Get group name
            String groupName = "";
            try {
                String urlGroupName = String.format("http://apigateway/api/v1/groups/%s", groupId);
                groupName = webClientBuilder.build().get()
                        .uri(urlGroupName)
                        .exchangeToMono(response -> {
                            if (response.statusCode().equals(HttpStatus.OK)) {
                                return response.bodyToMono(JsonNode.class);
                            } else {
                                return Mono.empty();
                            }
                        })
                        .map(jsonNode -> jsonNode.get("name").asText())
                        .blockOptional()
                        .orElse("Unknown Group");
            } catch (Exception e) {
                System.out.println("Error fetching group name: " + e.getMessage());
                groupName = "Unknown Group";
            }

            // Replace placeholder in message
            message = message.replace("[group name]", groupName);

            // Send notifications to users, excluding excludeUserId
            for (Integer userId : userIds) {
                if (excludeUserId != null && userId.equals(excludeUserId)) {
                    continue; // Skip the post's author
                }
                NotificationDTO notificationDTO = new NotificationDTO();
                notificationDTO.setMessage(message);
                notificationDTO.setUserId(userId);
                notificationDTO.setRead(false);
                try {
                    sendNotification(notificationDTO, groupId, groupName);
                } catch (Exception e) {
                    System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("No users found for groupId: " + groupId);
        }
    }

    public void sendNotificationToUsers(String message, List<Integer> userIds, Integer groupId) {
        if (userIds == null || userIds.isEmpty()) {
            System.out.println("No users to notify");
            return;
        }
        System.out.println("Sending notifications to users: " + userIds);

        // Get group name
        String groupName = "";
        try {
            String urlGroupName = String.format("http://apigateway/api/v1/groups/%s", groupId);
            groupName = webClientBuilder.build().get()
                    .uri(urlGroupName)
                    .exchangeToMono(response -> {
                        if (response.statusCode().equals(HttpStatus.OK)) {
                            return response.bodyToMono(JsonNode.class);
                        } else {
                            return Mono.empty();
                        }
                    })
                    .map(jsonNode -> jsonNode.get("name").asText())
                    .blockOptional()
                    .orElse("Unknown Group");
        } catch (Exception e) {
            System.out.println("Error fetching group name: " + e.getMessage());
            groupName = "Unknown Group";
        }

        // Replace placeholder in message
        message = message.replace("[group name]", groupName);

        // Send notifications to specified users
        for (Integer userId : userIds) {
            NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setMessage(message);
            notificationDTO.setUserId(userId);
            notificationDTO.setRead(false);
            try {
                sendNotification(notificationDTO, groupId, groupName);
            } catch (Exception e) {
                System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
            }
        }
    }

    public void removeNotificationsByGroupIdAndUserId(Integer groupId, Integer userId) {
        notificationRepo.deleteByGroupIdAndUserId(groupId, userId);
    }

}
