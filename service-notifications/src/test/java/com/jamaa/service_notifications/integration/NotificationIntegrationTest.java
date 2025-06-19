package com.jamaa.service_notifications.integration;

import com.jamaa.service_notifications.consumer.NotificationConsumer;
import com.jamaa.service_notifications.events.CustomerEvent;
import com.jamaa.service_notifications.events.DepositEvent;
import com.jamaa.service_notifications.events.TransferEvent;
import com.jamaa.service_notifications.events.WithdrawalEvent;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.service.EmailSender;
import com.jamaa.service_notifications.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'Intégration - Service de Notifications")
public class NotificationIntegrationTest {

    @SuppressWarnings("removal")
    @MockBean
    private NotificationService notificationService;

    @SuppressWarnings("removal")
    @MockBean
    private EmailSender emailSender;

    @Autowired
    private NotificationConsumer notificationConsumer;

    private List<Notification> savedNotifications = new ArrayList<>();
    private static final String TEST_EMAIL = "sorelletamen8@gmail.com";
    private static final String TEST_ACCOUNT = "ACC123456";
    private static final String TEST_FIRST_NAME = "Sorelle";
    private static final String TEST_LAST_NAME = "Tamen";

    @BeforeEach
    void setUp() {
        // Réinitialiser les mocks avant chaque test
        Mockito.reset(notificationService, emailSender);
        savedNotifications.clear();
        
        // Configurer le mock pour la sauvegarde des notifications
        when(notificationService.saveNotification(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId((long) (savedNotifications.size() + 1));
            notification.setDateEnvoi(LocalDateTime.now());
            savedNotifications.add(notification);
            return notification;
        });
        
        // Configurer le mock pour l'envoi d'emails (succès par défaut)
        try {
            doNothing().when(emailSender).sendNotification(any(String.class), any(EmailSender.NotificationType.class), any());
        } catch (Exception e) {
            fail("Erreur lors de la configuration du mock EmailSender: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @Timeout(value = 5) // 5 secondes max
    @DisplayName("Test de performance - Traitement rapide des notifications")
    void testPerformance() {
        // Ce test vérifie que le traitement d'une notification est rapide
        
        // Given
        CustomerEvent event = createCustomerEvent();
        
        // Configuration du mock pour la sauvegarde de la notification
        when(notificationService.saveNotification(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setDateEnvoi(LocalDateTime.now());
            savedNotifications.add(notification);
            return notification;
        });
        
        // When
        long startTime = System.currentTimeMillis();
        notificationConsumer.handleRegistrationNotification(event);
        long endTime = System.currentTimeMillis();
        
        // Then
        long executionTime = endTime - startTime;
        
        // Vérifier que le traitement est rapide (moins de 500ms par défaut, ajustez selon vos besoins)
        assertTrue(executionTime < 500, "Le traitement d'une notification ne devrait pas prendre plus de 500ms");
        
        // Vérifier que l'email a été envoyé
        try {
            verify(emailSender, times(1))
                .sendNotification(eq(TEST_EMAIL), eq(EmailSender.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE), any());
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'envoi de l'email: " + e.getMessage());
        }
        
        // Vérifier que la notification a été traitée
        assertEquals(1, savedNotifications.size(), 
                    "La notification doit avoir été traitée");
        
        System.out.println(String.format("✅ Test de performance réussi - notification traitée en %d ms", 
                                         executionTime));
    }

    @Test
    @Order(1)
    @DisplayName("Test d'inscription client - Flux complet")
    void testCustomerRegistrationFlow() {
        // Given
        CustomerEvent event = createCustomerEvent();
        
        // Configuration du mock pour la sauvegarde de la notification
        when(notificationService.saveNotification(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setDateEnvoi(LocalDateTime.now());
            savedNotifications.add(notification);
            return notification;
        });
        
        try {
            // When
            notificationConsumer.handleRegistrationNotification(event);
            
            // Then
            // Vérifier que la notification a été sauvegardée
            verify(notificationService, times(1)).saveNotification(any(Notification.class));
            
            // Vérifier que l'email a été envoyé
            verify(emailSender, times(1)).sendNotification(
                eq(TEST_EMAIL), 
                eq(EmailSender.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE),
                any()
            );
            
            // Vérifier les détails de la notification sauvegardée
            assertFalse(savedNotifications.isEmpty(), "Aucune notification n'a été sauvegardée");
            Notification savedNotification = savedNotifications.get(0);
            
            assertNotNull(savedNotification, "La notification sauvegardée ne doit pas être nulle");
            assertEquals(TEST_EMAIL, savedNotification.getEmail(), "L'email du destinataire ne correspond pas");
            assertEquals(Notification.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE, 
                        savedNotification.getType(), "Le type de notification ne correspond pas");
            assertTrue(savedNotification.getMessage().contains(TEST_FIRST_NAME), 
                     "Le message doit contenir le prénom du client");
            assertTrue(savedNotification.getMessage().contains(TEST_LAST_NAME), 
                     "Le message doit contenir le nom du client");
            assertTrue(savedNotification.getMessage().contains(TEST_ACCOUNT), 
                     "Le message doit contenir le numéro de compte");
            
            System.out.println("✅ Test d'inscription client réussi - Notification ID: " + savedNotification.getId());
        } catch (Exception e) {
            fail("Erreur lors du test d'inscription client: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test de dépôt - Flux complet")
    void testDepositFlow() {
        // Given
        DepositEvent event = createDepositEvent();
        
        // Configuration du mock pour la sauvegarde de la notification
        when(notificationService.saveNotification(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(2L);
            notification.setDateEnvoi(LocalDateTime.now());
            savedNotifications.add(notification);
            return notification;
        });
        
        try {
            // When
            notificationConsumer.handleDepositNotification(event);
            
            // Then
            // Vérifier que la notification a été sauvegardée
            verify(notificationService, times(1)).saveNotification(any(Notification.class));
            
            // Vérifier que l'email a été envoyé
            verify(emailSender, times(1)).sendNotification(
                eq(TEST_EMAIL), 
                eq(EmailSender.NotificationType.DEPOSIT),
                any()
            );
            
            // Vérifier les détails de la notification sauvegardée
            assertFalse(savedNotifications.isEmpty(), "Aucune notification n'a été sauvegardée");
            Notification savedNotification = savedNotifications.get(0);
            
            assertNotNull(savedNotification, "La notification sauvegardée ne doit pas être nulle");
            assertEquals(TEST_EMAIL, savedNotification.getEmail(), "L'email du destinataire ne correspond pas");
            assertEquals(Notification.NotificationType.CONFIRMATION_DEPOT, 
                        savedNotification.getType(), "Le type de notification ne correspond pas");
            assertTrue(savedNotification.getMessage().contains("50000.0"), 
                     "Le message doit contenir le montant du dépôt");
            assertTrue(savedNotification.getMessage().contains("VIREMENT"), 
                     "Le message doit contenir la méthode de dépôt");
            
            System.out.println("✅ Test de dépôt réussi - Notification ID: " + savedNotification.getId());
        } catch (Exception e) {
            fail("Erreur lors du test de dépôt: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Test de transfert - Flux complet")
    void testTransferFlow() {
        // Given
        TransferEvent event = createTransferEvent();

        // When
        try {
            notificationConsumer.handleTransferNotification(event);
        } catch (Exception e) {
            fail("L'appel à handleTransferNotification a échoué: " + e.getMessage());
        }

        // Then
        verifyNotificationSaved(1);
        
        try {
            verifyEmailSent(TEST_EMAIL, EmailSender.NotificationType.TRANSFER);
            
            Notification notification = findNotificationByType(EmailSender.NotificationType.TRANSFER);
            assertNotNull(notification, "Notification de transfert non trouvée");
            assertNotificationDetails(notification, EmailSender.NotificationType.TRANSFER, TEST_EMAIL);
            assertEquals("Confirmation de transfert", notification.getTitle());
            
            // Vérifier les détails du transfert
            assertTrue(notification.getMessage().contains("20000"), 
                "Le message doit contenir le montant du transfert");
            assertTrue(notification.getMessage().contains("Destinataire Test"), 
                "Le message doit contenir le nom du bénéficiaire");
            
            System.out.println("✅ Test de transfert réussi - Notification ID: " + notification.getId());
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'envoi d'email: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test de retrait - Flux complet")
    void testWithdrawalFlow() {
        // Given
        WithdrawalEvent event = createWithdrawalEvent();

        // When
        try {
            notificationConsumer.handleWithdrawalNotification(event);
        } catch (Exception e) {
            fail("L'appel à handleWithdrawalNotification a échoué: " + e.getMessage());
        }

        // Then
        verifyNotificationSaved(1);
        
        try {
            // Vérifier que la notification a été sauvegardée avec les bonnes informations
            ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationService).saveNotification(notificationCaptor.capture());
            
            Notification notification = notificationCaptor.getValue();
            assertNotNull(notification, "La notification ne doit pas être nulle");
            assertEquals(EmailSender.NotificationType.WITHDRAWAL.name(), notification.getType().name());
            assertEquals(TEST_EMAIL, notification.getEmail());
            assertEquals("Confirmation de retrait", notification.getTitle());
            
            // Vérifier le contenu du message
            String message = notification.getMessage();
            assertNotNull(message, "Le message ne doit pas être nul");
            assertTrue(message.contains("10000"), "Le message doit contenir le montant du retrait");
            assertTrue(message.contains("GUICHET"), "Le message doit contenir la méthode de retrait");
            
            // Vérifier que l'email a bien été envoyé
            verify(emailSender, times(1)).sendNotification(
                eq(TEST_EMAIL),
                eq(EmailSender.NotificationType.WITHDRAWAL),
                any()
            );
            
            System.out.println("✅ Test de retrait réussi - Notification ID: " + notification.getId());
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'envoi d'email: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Test de gestion d'erreur - Échec d'envoi d'email")
    void testEmailSendingFailure() {
        // Given
        CustomerEvent event = createCustomerEvent();
        
        // Configuration du mock pour la sauvegarde de la notification
        when(notificationService.saveNotification(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(3L);
            notification.setDateEnvoi(LocalDateTime.now());
            savedNotifications.add(notification);
            return notification;
        });
        
        // Configuration pour simuler un échec d'envoi d'email
        try {
            doThrow(new MessagingException("Erreur d'envoi d'email simulée"))
                .when(emailSender)
                .sendNotification(eq(TEST_EMAIL), eq(EmailSender.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE), any());
        } catch (Exception e) {
            fail("Échec de la configuration du mock EmailSender: " + e.getMessage());
        }

        // When/Then - Vérifier que l'exception est correctement gérée
        assertDoesNotThrow(() -> {
            try {
                notificationConsumer.handleRegistrationNotification(event);
            } catch (Exception e) {
                fail("La méthode ne devrait pas propager l'exception: " + e.getMessage());
            }
        }, "L'échec d'envoi d'email ne devrait pas faire échouer la méthode");

        // Vérifier que la notification a bien été enregistrée malgré l'échec d'envoi
        verify(notificationService, times(1)).saveNotification(any(Notification.class));
        
        // Vérifier que la tentative d'envoi d'email a bien été effectuée
        try {
            verify(emailSender, times(1)).sendNotification(
                eq(TEST_EMAIL),
                eq(EmailSender.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE),
                any()
            );
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'envoi d'email: " + e.getMessage());
        }
        
        System.out.println("✅ Test d'échec d'envoi d'email réussi");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test avec événement null")
    void testNullEvent() {
        // When/Then - Vérifier qu'une exception est levée pour un événement null
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            // Appeler directement la méthode handleRegistrationNotification avec null
            notificationConsumer.handleRegistrationNotification(null);
        }, "Devrait lever une exception pour un événement null");
        
        // Vérifier le message d'erreur
        assertTrue(exception.getMessage() != null && 
                 (exception.getMessage().contains("L'événement ne peut pas être null") || 
                  exception.getMessage().contains("L'email du client est requis") ||
                  exception.getMessage().contains("Le client ne peut pas être null")),
                 "Le message d'erreur doit indiquer que l'événement ne peut pas être null");
        
        // Vérifier qu'aucune notification n'est traitée
        verify(notificationService, never()).saveNotification(any(Notification.class));
        
        try {
            verify(emailSender, never()).sendNotification(anyString(), any(EmailSender.NotificationType.class), any());
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'absence d'envoi d'email: " + e.getMessage());
        }
        
        System.out.println("✅ Test d'événement null réussi");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test avec événement invalide - email manquant")
    void testInvalidEventMissingEmail() {
        // Given - Événement avec email manquant
        CustomerEvent invalidEvent = new CustomerEvent();
        invalidEvent.setFirstName(TEST_FIRST_NAME);
        invalidEvent.setLastName(TEST_LAST_NAME);
        invalidEvent.setAccountNumber(TEST_ACCOUNT);
        // Email manquant intentionnellement

        // When/Then - Vérifier qu'une exception est levée
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            // Appeler directement la méthode handleRegistrationNotification avec un événement invalide
            notificationConsumer.handleRegistrationNotification(invalidEvent);
        }, "Devrait lever une exception pour un événement avec email manquant");
        
        // Vérifier le message d'erreur
        assertTrue(exception.getMessage() != null && 
                 (exception.getMessage().contains("L'email du client est requis") ||
                  exception.getMessage().contains("L'email est obligatoire") ||
                  exception.getMessage().contains("Email is required")),
                 "Le message d'erreur doit indiquer que l'email est requis. Message d'erreur: " + 
                 (exception.getMessage() != null ? exception.getMessage() : "null"));
        
        // Vérifier qu'aucune notification n'est traitée
        verify(notificationService, never()).saveNotification(any(Notification.class));
        
        try {
            verify(emailSender, never()).sendNotification(anyString(), any(EmailSender.NotificationType.class), any());
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'absence d'envoi d'email: " + e.getMessage());
        }
        
        System.out.println("✅ Test d'événement invalide réussi");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test de performance avec plusieurs inscriptions")
    @Timeout(5)
    void testRegistrationPerformance() throws Exception {
        // Given
        int notificationCount = 50;
        List<CustomerEvent> events = new ArrayList<>();
        for (int i = 0; i < notificationCount; i++) {
            CustomerEvent event = new CustomerEvent();
            event.setEmail("user" + i + "@test.com");
            event.setFirstName("User" + i);
            event.setLastName("Test" + i);
            event.setAccountNumber("ACC" + (1000 + i));
            events.add(event);
        }

        // When
        long startTime = System.currentTimeMillis();
        for (CustomerEvent event : events) {
            notificationConsumer.handleRegistrationNotification(event);
        }
        long endTime = System.currentTimeMillis();

        // Then
        verify(notificationService, times(notificationCount)).saveNotification(any(Notification.class));
        verify(emailSender, times(notificationCount)).sendNotification(
            anyString(),
            any(EmailSender.NotificationType.class),
            anyMap()
        );
        
        long executionTime = endTime - startTime;
        System.out.println("⏱️ Temps d'exécution pour " + notificationCount + " inscriptions: " + executionTime + "ms");
        assertTrue(executionTime < 3000, "Le traitement de " + notificationCount + " inscriptions ne devrait pas prendre plus de 3 secondes");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test de flux mixte - Plusieurs types de notifications")
    public void testMixedNotificationFlow() {
        // Given
        CustomerEvent customerEvent = createCustomerEvent();
        DepositEvent depositEvent = createDepositEvent();
        TransferEvent transferEvent = createTransferEvent();
        WithdrawalEvent withdrawalEvent = createWithdrawalEvent();

        // When
        try {
            notificationConsumer.handleRegistrationNotification(customerEvent);
            notificationConsumer.handleDepositNotification(depositEvent);
            notificationConsumer.handleTransferNotification(transferEvent);
            notificationConsumer.handleWithdrawalNotification(withdrawalEvent);
        } catch (Exception e) {
            fail("Erreur lors de l'exécution du flux mixte: " + e.getMessage());
        }

        // Then
        verify(notificationService, times(4)).saveNotification(any(Notification.class));
        assertEquals(4, savedNotifications.size(), "4 notifications devraient être enregistrées");
        
        // Vérifier que chaque type de notification est présent
        assertTrue(savedNotifications.stream()
            .anyMatch(n -> n.getType() == Notification.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE), 
            "Doit contenir une notification de confirmation de souscription");
            
        assertTrue(savedNotifications.stream()
            .anyMatch(n -> n.getType() == Notification.NotificationType.CONFIRMATION_DEPOT), 
            "Doit contenir une notification de confirmation de dépôt");
            
        assertTrue(savedNotifications.stream()
            .anyMatch(n -> n.getType() == Notification.NotificationType.CONFIRMATION_TRANSFERT), 
            "Doit contenir une notification de confirmation de transfert");
            
        assertTrue(savedNotifications.stream()
            .anyMatch(n -> n.getType() == Notification.NotificationType.CONFIRMATION_RETRAIT), 
            "Doit contenir une notification de confirmation de retrait");
            
        // Vérifier que les emails ont été envoyés avec les bons paramètres
        try {
            verify(emailSender).sendNotification(
                eq(TEST_EMAIL),
                eq(EmailSender.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE),
                any()
            );
            
            verify(emailSender).sendNotification(
                eq(TEST_EMAIL),
                eq(EmailSender.NotificationType.DEPOSIT),
                any()
            );
            
            verify(emailSender).sendNotification(
                eq(TEST_EMAIL),
                eq(EmailSender.NotificationType.TRANSFER),
                any()
            );
            
            verify(emailSender).sendNotification(
                eq(TEST_EMAIL),
                eq(EmailSender.NotificationType.WITHDRAWAL),
                any()
            );
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'envoi d'emails: " + e.getMessage());
        }
        
        System.out.println("✅ Test de flux mixte réussi");
    }
    
    // =============== MÉTHODES UTILITAIRES ===============
    
    private void verifyNotificationSaved(int expectedCount) {
        verify(notificationService, times(expectedCount)).saveNotification(any(Notification.class));
    }
    
    private void verifyEmailSent(String email, EmailSender.NotificationType type) {
        try {
            verify(emailSender).sendNotification(
                eq(email),
                eq(type),
                any()
            );
        } catch (Exception e) {
            fail("Erreur lors de la vérification de l'envoi d'email: " + e.getMessage());
        }
    }
    
    private Notification findNotificationByType(EmailSender.NotificationType type) {
        return savedNotifications.stream()
            .filter(n -> n.getType().name().equals(type.name()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Aucune notification de type " + type + " trouvée"));
    }
    
    private void assertNotificationDetails(Notification notification, 
                                         EmailSender.NotificationType expectedType, 
                                         String expectedEmail) {
        assertNotNull(notification, "La notification ne doit pas être nulle");
        assertEquals(expectedType.name(), notification.getType().name(), "Le type de notification ne correspond pas");
        assertEquals(expectedEmail, notification.getEmail(), "L'email du destinataire ne correspond pas");
        assertNotNull(notification.getId(), "L'ID de la notification doit être défini");
        assertNotNull(notification.getDateEnvoi(), "La date d'envoi doit être définie");
        assertNotNull(notification.getMessage(), "Le message ne doit pas être nul");
        assertFalse(notification.getMessage().isEmpty(), "Le message ne doit pas être vide");
    }
    
    private CustomerEvent createCustomerEvent() {
        CustomerEvent event = new CustomerEvent();
        event.setEmail(TEST_EMAIL);
        event.setFirstName(TEST_FIRST_NAME);
        event.setLastName(TEST_LAST_NAME);
        event.setAccountNumber(TEST_ACCOUNT);
        return event;
    }
    
    private DepositEvent createDepositEvent() {
        DepositEvent event = new DepositEvent();
        event.setEmail(TEST_EMAIL);
        event.setAmount(50000.00);
        event.setTransactionId("DEP" + System.currentTimeMillis());
        event.setAccountId(TEST_ACCOUNT);
        event.setStatus("COMPLETED");
        event.setTimestamp(LocalDateTime.now());
        event.setDepositMethod("VIREMENT");
        event.setReferenceNumber("REF" + System.currentTimeMillis());
        event.setBankName("Jamaa Bank");
        return event;
    }
    
    private TransferEvent createTransferEvent() {
        TransferEvent event = new TransferEvent();
        event.setEmail(TEST_EMAIL);
        event.setAmount(20000.00);
        event.setTransactionId("TRF" + System.currentTimeMillis());
        event.setAccountId(TEST_ACCOUNT);
        event.setStatus("COMPLETED");
        event.setTimestamp(LocalDateTime.now());
        event.setDestinationAccount("ACC789012");
        event.setBeneficiaryName("Destinataire Test");
        event.setDestinationBank("JMBK");
        event.setSourceAccount(TEST_ACCOUNT);
        return event;
    }
    
    private WithdrawalEvent createWithdrawalEvent() {
        WithdrawalEvent event = new WithdrawalEvent();
        event.setEmail(TEST_EMAIL);
        event.setAmount(10000.00);
        event.setTransactionId("WDR" + System.currentTimeMillis());
        event.setAccountId(TEST_ACCOUNT);
        event.setStatus("COMPLETED");
        event.setTimestamp(LocalDateTime.now());
        event.setWithdrawalMethod("GUICHET");
        event.setDestinationAccount(TEST_ACCOUNT);
        event.setBankName("Agence Centrale");
        return event;
    }
}