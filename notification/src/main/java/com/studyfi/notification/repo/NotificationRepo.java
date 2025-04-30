package com.studyfi.notification.repo;

import com.studyfi.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserId(Integer userId);
    List<Notification> findTop10ByUserIdOrderByTimestampDesc(Integer userId);
    List<Notification> findByUserIdAndIsReadFalse(Integer userId);
}
