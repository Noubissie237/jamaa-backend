package com.jamaa.service_notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import com.jamaa.service_notifications.model.Notification.ServiceEmetteur;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndType(String userId, NotificationType type);
    List<Notification> findByUserIdAndServiceEmetteur(String userId, ServiceEmetteur service);
    
    List<Notification> findByUserIdAndLuFalse(String userId);

    @Transactional
    void deleteByUserId(String userId);
} 