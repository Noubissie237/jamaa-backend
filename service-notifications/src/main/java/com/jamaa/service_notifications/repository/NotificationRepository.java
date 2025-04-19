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
    List<Notification> findByEmail(String email);
    List<Notification> findByEmailAndLuFalse(String email);
    List<Notification> findByEmailAndType(String email, NotificationType type);
    List<Notification> findByEmailAndServiceEmetteur(String email, ServiceEmetteur service);

    @Transactional
    void deleteByEmail(String email);
} 