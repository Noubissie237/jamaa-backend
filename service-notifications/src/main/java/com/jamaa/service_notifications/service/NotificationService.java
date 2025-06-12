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

    public List<Notification> getNotificationsByUser(String email) {
        return notificationRepository.findByEmail(email);
    }
    
    public List<Notification> getUnreadNotificationsByUser(String email) {
        return notificationRepository.findByEmailAndLuFalse(email);
    }

    public List<Notification> getNotificationsByTypeAndUser(String email, Notification.NotificationType type) {
        return notificationRepository.findByEmailAndType(email, type);
    }

    public List<Notification> getNotificationsByServiceAndUser(String email, Notification.ServiceEmetteur service) {
        return notificationRepository.findByEmailAndServiceEmetteur(email, service);
    }
    
    public Notification marquerCommeLue(Long id) {
        return notificationRepository.findById(id)
            .map(notification -> {
                notification.setLu(true);
                return notificationRepository.save(notification);
            })
            .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
    }
    
    public int marquerToutesCommeLues(String email) {
        List<Notification> notificationsNonLues = notificationRepository.findByEmailAndLuFalse(email);
        notificationsNonLues.forEach(notification -> notification.setLu(true));
        notificationRepository.saveAll(notificationsNonLues);
        return notificationsNonLues.size();
    }
} 