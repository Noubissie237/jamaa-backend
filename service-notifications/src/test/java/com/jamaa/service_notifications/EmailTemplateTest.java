package com.jamaa.service_notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;

import com.jamaa.service_notifications.consumer.NotificationConsumer;
import com.jamaa.service_notifications.events.DepositEvent;
import com.jamaa.service_notifications.events.TransferEvent;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import com.jamaa.service_notifications.repository.NotificationRepository;
import com.jamaa.service_notifications.service.EmailSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.mail.MessagingException;

@SpringBootTest
@AutoConfigureGraphQlTester
@TestPropertySource(locations = "classpath:application-test.properties")
@TestPropertySource(properties = {
    "spring.rabbitmq.listener.simple.default-requeue-rejected=false",
    "spring.rabbitmq.listener.simple.retry.enabled=false"
})
@DirtiesContext
public class EmailTemplateTest {

    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateTest.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationConsumer notificationConsumer;
    
    @Autowired
    private EmailSender emailSender;

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
    public void testDepositEmailTemplate() throws MessagingException {
        // Création d'un événement de dépôt
        DepositEvent depositEvent = new DepositEvent();
        depositEvent.setEmail("sorelletamen8@gmail.com"); // Utilisez une adresse réelle pour tester
        depositEvent.setAmount(1000.0);
        depositEvent.setDepositMethod("Virement bancaire");
        depositEvent.setReferenceNumber("REF123456789");
        depositEvent.setBankName("Jamaa Bank");
        depositEvent.setAccountNumber("123456789012");
        depositEvent.setTransactionId("TX-12345");
        
        // Nettoyer la base de données avant le test
        notificationRepository.deleteAll();
        
        try {
            // Traitement de l'événement
            notificationConsumer.handleDepositNotification(depositEvent);
            
            // Attente pour s'assurer que le traitement est terminé
            waitForNotificationProcessing();
            
            // Vérification que la notification a été sauvegardée
            logNotifications(depositEvent.getEmail());
            List<Notification> notifications = notificationRepository.findByEmail(depositEvent.getEmail());
            assert !notifications.isEmpty() : "Aucune notification n'a été sauvegardée";
            assert notifications.get(0).getType() == NotificationType.CONFIRMATION_DEPOT : "Mauvais type de notification";
            
            logger.info("Test de template d'email de dépôt réussi - vérifiez votre boîte de réception");
            
            // Test GraphQL
            String query = """
                query {
                    notificationsByUser(email: "%s") {
                        id
                        title
                        message
                        email
                    }
                }
            """.formatted(depositEvent.getEmail());
            
            graphQlTester.document(query)
                .execute()
                .path("notificationsByUser")
                .entityList(Notification.class)
                .hasSize(1);
                
        } catch (Exception e) {
            logger.error("Erreur lors du test de dépôt: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Test
    public void testTransferEmailTemplate() throws MessagingException {
        // Création d'un événement de transfert
        TransferEvent transferEvent = new TransferEvent();
        transferEvent.setEmail("sorelletamen8@gmail.com"); // Utilisez une adresse réelle pour tester
        transferEvent.setAmount(500.0);
        transferEvent.setSourceAccount("123456789012");
        transferEvent.setDestinationAccount("987654321098");
        transferEvent.setDestinationBank("Autre Banque");
        transferEvent.setBeneficiaryName("John Doe");
        transferEvent.setTransferReason("Remboursement");
        transferEvent.setTransactionId("TX-67890");
        
        // Nettoyer la base de données avant le test
        notificationRepository.deleteAll();
        
        try {
            // Traitement de l'événement
            notificationConsumer.handleTransferNotification(transferEvent);
            
            // Attente pour s'assurer que le traitement est terminé
            waitForNotificationProcessing();
            
            // Vérification que la notification a été sauvegardée
            logNotifications(transferEvent.getEmail());
            List<Notification> notifications = notificationRepository.findByEmail(transferEvent.getEmail());
            assert !notifications.isEmpty() : "Aucune notification n'a été sauvegardée";
            assert notifications.get(0).getType() == NotificationType.CONFIRMATION_TRANSFERT : "Mauvais type de notification";
            
            logger.info("Test de template d'email de transfert réussi - vérifiez votre boîte de réception");
        } catch (Exception e) {
            logger.error("Erreur lors du test de transfert: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Test
    public void testEmailSenderDirectly() throws Exception {
        // Préparation des données
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 1500.0);
        data.put("depositMethod", "Carte bancaire");
        data.put("referenceNumber", "REF987654321");
        data.put("bankName", "Jamaa Bank");
        data.put("accountNumber", "987654321098");
        
        try {
            // Envoi direct de la notification
            emailSender.sendNotification(
                "sorelletamen8@gmail.com", // Utilisez une adresse réelle pour tester
                EmailSender.NotificationType.DEPOSIT,
                data
            );
            
            logger.info("Test d'envoi direct d'email réussi - vérifiez votre boîte de réception");
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi direct d'email: {}", e.getMessage(), e);
            throw e;
        }
    }
}