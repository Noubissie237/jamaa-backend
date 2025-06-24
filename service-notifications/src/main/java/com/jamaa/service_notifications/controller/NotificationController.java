package com.jamaa.service_notifications.controller;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.service.NotificationService;


import java.util.List;

@Controller
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @QueryMapping
    public List<Notification> notificationsByUser(@Argument String userId) {
        return notificationService.getNotificationsByUser(userId);
    }

    @QueryMapping
    public List<Notification> notificationsByType(@Argument String userId, @Argument Notification.NotificationType type) {
        return notificationService.getNotificationsByTypeAndUser(userId, type);
    }

    @QueryMapping
    public List<Notification> notificationsByService(@Argument String userId, @Argument Notification.ServiceEmetteur service) {
        return notificationService.getNotificationsByServiceAndUser(userId, service);
    }
    
    @QueryMapping
    public List<Notification> unreadNotifications(@Argument String userId) {
        return notificationService.getUnreadNotificationsByUser(userId);
    }
    
    @MutationMapping
    public Boolean marquerCommeLue(@Argument Long id) {
        notificationService.marquerCommeLue(id);
        return true;
    }
    
    @MutationMapping
    public Integer marquerToutesCommeLues(@Argument String userId) {
        return notificationService.marquerToutesCommeLues(userId);
    }
} 