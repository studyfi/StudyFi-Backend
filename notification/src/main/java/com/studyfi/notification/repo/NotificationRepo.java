package com.studyfi.notification.repo;

import com.studyfi.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserId(Integer userId);
}
