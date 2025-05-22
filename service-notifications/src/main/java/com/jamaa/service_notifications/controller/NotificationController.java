package com.jamaa.service_notifications.controller;

import org.springframework.graphql.data.method.annotation.Argument;
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
    public List<Notification> notificationsByUser(@Argument String email) {
        return notificationService.getNotificationsByUser(email);
    }

    @QueryMapping
    public List<Notification> notificationsByType(@Argument String email, @Argument Notification.NotificationType type) {
        return notificationService.getNotificationsByTypeAndUser(email, type);
    }

    @QueryMapping
    public List<Notification> notificationsByService(@Argument String email, @Argument Notification.ServiceEmetteur service) {
        return notificationService.getNotificationsByServiceAndUser(email, service);
    }

    
} 