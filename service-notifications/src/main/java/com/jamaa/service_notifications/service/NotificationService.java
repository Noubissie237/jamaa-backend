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
    
    public Notification envoyerNotification(Notification notification) {
        // Si c'est une notification en application, on ne fait que la sauvegarder
        if (notification.getCanal() == Notification.CanalNotification.IN_APP) {
            return notificationRepository.save(notification);
        }
        // Sinon, on laisse le consommateur gérer l'envoi par email
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    public List<Notification> getUnreadNotificationsByUser(String userId) {
        return notificationRepository.findByUserIdAndLuFalse(userId);
    }

    public List<Notification> getNotificationsByTypeAndUser(String userId, Notification.NotificationType type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    public List<Notification> getNotificationsByServiceAndUser(String userId, Notification.ServiceEmetteur service) {
        return notificationRepository.findByUserIdAndServiceEmetteur(userId, service);
    }
    
    public Notification marquerCommeLue(Long id) {
        return notificationRepository.findById(id)
            .map(notification -> {
                notification.setLu(true);
                return notificationRepository.save(notification);
            })
            .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
    }
    
    public int marquerToutesCommeLues(String userId) {
        List<Notification> notificationsNonLues = notificationRepository.findByUserIdAndLuFalse(userId);
        notificationsNonLues.forEach(notification -> notification.setLu(true));
        notificationRepository.saveAll(notificationsNonLues);
        return notificationsNonLues.size();
    }
} 