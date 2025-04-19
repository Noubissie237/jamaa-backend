package com.jamaa.service_notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;

import com.jamaa.service_notifications.producer.NotificationProducer;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import com.jamaa.service_notifications.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.List;

import com.jamaa.service_notifications.producer.NotificationTestProducer;

@SpringBootTest
@AutoConfigureGraphQlTester
@TestPropertySource(properties = {
    "spring.rabbitmq.listener.simple.default-requeue-rejected=false",
    "spring.rabbitmq.listener.simple.retry.enabled=false"
})
@DirtiesContext
public class GraphQLNotificationTest {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLNotificationTest.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private NotificationTestProducer notificationTestProducer;

    @Autowired
    private NotificationRepository notificationRepository;

    private void waitForNotificationProcessing() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void logNotifications(String email) {
        List<Notification> notifications = notificationRepository.findByEmail(email);
        logger.info("=== Notifications dans la base de données ===");
        logger.info("Nombre total: {}", notifications.size());
        notifications.forEach(n -> logger.info("Notification: {}", n));
    }

    @Test
    public void testQueryNotificationsByUser() throws InterruptedException {
        String email = "sorelletamen8@gmail.com";
        LocalDateTime now = LocalDateTime.now();

        // Nettoyer la base de données
        notificationRepository.deleteAll();
        logger.info("Base de données nettoyée");

        // Envoyer une notification simple
        notificationTestProducer.sendDepositNotification(
            email,
            "Dépôt de 1000.00 EUR",
            now
        );
        logger.info("Notification de dépôt envoyée");

        waitForNotificationProcessing();
        logNotifications(email);

        // Requête GraphQL simple
        String query = """
            query {
                notificationsByUser(email: "%s") {
                    id
                    title
                    message
                    email
                }
            }
        """.formatted(email);

        logger.info("Exécution de la requête GraphQL: {}", query);

        // Vérification simple
        graphQlTester.document(query)
            .execute()
            .path("notificationsByUser")
            .entityList(Notification.class)
            .hasSize(1)
            .satisfies(notifications -> {
                Notification notification = notifications.get(0);
                logger.info("Notification reçue: {}", notification);
                assert notification.getEmail().equals(email);
                assert notification.getTitle().equals("Confirmation de dépôt");
            });
    }

    @Test
    public void testQueryUnreadNotifications() throws InterruptedException {
        String email = "sorelletamen8@gmail.com";
        LocalDateTime now = LocalDateTime.now();

        // Nettoyer la base de données
        notificationRepository.deleteAll();
        logger.info("Base de données nettoyée");

        // Envoyer une notification
        notificationTestProducer.sendDepositNotification(
            email,
            "Dépôt de 1000.00 EUR",
            now
        );
        logger.info("Notification de dépôt envoyée");

        waitForNotificationProcessing();
        logNotifications(email);

        // Requête GraphQL simple
        String query = """
            query {
                unreadNotifications(email: "%s") {
                    id
                    title
                    message
                    email
                    lu
                }
            }
        """.formatted(email);

        logger.info("Exécution de la requête GraphQL: {}", query);

        // Vérification simple
        graphQlTester.document(query)
            .execute()
            .path("unreadNotifications")
            .entityList(Notification.class)
            .hasSize(1)
            .satisfies(notifications -> {
                Notification notification = notifications.get(0);
                logger.info("Notification non lue reçue: {}", notification);
                assert notification.getEmail().equals(email);
                assert !notification.isLu();
            });
    }
} 