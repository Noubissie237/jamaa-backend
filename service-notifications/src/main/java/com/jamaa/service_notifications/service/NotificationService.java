package com.jamaa.service_notifications.service;

import org.springframework.stereotype.Service;

import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUser(String email) {
        return notificationRepository.findByEmail(email);
    }

    public List<Notification> getNotificationsByTypeAndUser(String email, Notification.NotificationType type) {
        return notificationRepository.findByEmailAndType(email, type);
    }

    public List<Notification> getNotificationsByServiceAndUser(String email, Notification.ServiceEmetteur service) {
        return notificationRepository.findByEmailAndServiceEmetteur(email, service);
    }
} 