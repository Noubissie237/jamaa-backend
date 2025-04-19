// package com.jamaa.service_notifications;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.TestPropertySource;
// import com.jamaa.service_notifications.producer.NotificationTestProducer;
// import com.jamaa.service_notifications.model.Notification.NotificationType;
// import com.jamaa.service_notifications.service.NotificationService;
// import org.springframework.test.annotation.DirtiesContext;
// import static org.junit.jupiter.api.Assertions.*;
// import java.util.List;
// import com.jamaa.service_notifications.model.Notification;
// import org.springframework.test.context.ActiveProfiles;

// @SpringBootTest
// @TestPropertySource(properties = {
//     "spring.rabbitmq.listener.simple.default-requeue-rejected=false",
//     "spring.rabbitmq.listener.simple.retry.enabled=false"
// })
// @DirtiesContext
// @ActiveProfiles("test")
// public class NotificationTest {

//     @Autowired
//     private NotificationTestProducer notificationTestProducer;

//     @Autowired
//     private NotificationService notificationService;

//     @Test
//     public void testSendAllNotifications() throws InterruptedException {
//         String email = "sorelletamen8@gmail.com";

//         // Test notification de dépôt
//         notificationTestProducer.sendDepositNotification(
//             email,
//             "Votre dépôt de 1000€ a été effectué avec succès"
//         );

//         // Test notification de retrait
//         notificationTestProducer.sendWithdrawalNotification(
//             email,
//             "Votre retrait de 500€ a été effectué avec succès"
//         );

//         // Test notification de transfert
//         String transferMessage = "Test notification flow";
//         notificationTestProducer.sendTransferNotification(
//             email,
//             transferMessage
//         );

//         // Test notification d'inscription
//         notificationTestProducer.sendAuthNotification(
//             email,
//             "Bienvenue sur Jamaa! Votre compte a été créé avec succès",
//             NotificationType.CONFIRMATION_INSCRIPTION
//         );

//         // Test notification de réinitialisation de mot de passe
//         notificationTestProducer.sendAuthNotification(
//             email,
//             "Votre mot de passe a été réinitialisé avec succès",
//             NotificationType.MOT_DE_PASSE_REINITIALISE
//         );

//         // Test notification de souscription bancaire
//         notificationTestProducer.sendBankNotification(
//             email,
//             "Félicitations! Vous êtes maintenant client de la banque X"
//         );

//         // Test notification de transaction
//         notificationTestProducer.sendTransactionNotification(
//             email,
//             "Votre transaction a été effectuée avec succès"
//         );

//         // Attendre que les messages soient traités
//         Thread.sleep(2000);

//         // Vérifier que les notifications ont été sauvegardées
//         List<Notification> notifications = notificationService.getNotificationsByUser(email);
//         assertFalse(notifications.isEmpty(), "Des notifications devraient être présentes");

//         // Vérifier spécifiquement la notification de transfert
//         List<Notification> transferNotifications = notificationService.getNotificationsByTypeAndUser(
//             email, NotificationType.CONFIRMATION_TRANSFERT);
//         assertFalse(transferNotifications.isEmpty(), "Une notification de transfert devrait être présente");
//         assertEquals(transferMessage, transferNotifications.get(0).getMessage(),
//             "Le message de transfert devrait correspondre");
//     }

//     @Test
//     public void testSendMultipleNotificationsForSameUser() throws InterruptedException {
//         String email = "user@example.com";

//         // Envoi de plusieurs notifications de dépôt
//         for (int i = 1; i <= 3; i++) {
//             notificationTestProducer.sendDepositNotification(
//                 email,
//                 String.format("Dépôt #%d de 100€ effectué avec succès", i)
//             );
//         }

//         // Envoi de plusieurs notifications de transfert
//         for (int i = 1; i <= 2; i++) {
//             notificationTestProducer.sendTransferNotification(
//                 email,
//                 String.format("Transfert #%d de 50€ vers le compte Y effectué", i)
//             );
//         }

//         // Attendre que les messages soient traités
//         Thread.sleep(2000);

//         // Vérifier les notifications de dépôt
//         List<Notification> depositNotifications = notificationService.getNotificationsByTypeAndUser(
//             email, NotificationType.CONFIRMATION_DEPOT);
//         assertEquals(3, depositNotifications.size(), "Il devrait y avoir 3 notifications de dépôt");

//         // Vérifier les notifications de transfert
//         List<Notification> transferNotifications = notificationService.getNotificationsByTypeAndUser(
//             email, NotificationType.CONFIRMATION_TRANSFERT);
//         assertEquals(2, transferNotifications.size(), "Il devrait y avoir 2 notifications de transfert");
//     }
// } 