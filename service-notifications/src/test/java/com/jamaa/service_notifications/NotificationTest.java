package com.jamaa.service_notifications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import com.jamaa.service_notifications.consumer.NotificationConsumer;
import com.jamaa.service_notifications.events.DepositEvent;
import com.jamaa.service_notifications.events.WithdrawalEvent;
import com.jamaa.service_notifications.events.TransferEvent;
import com.jamaa.service_notifications.events.AuthEvent;
import com.jamaa.service_notifications.events.CustomerEvent;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.service.EmailSender;
import com.jamaa.service_notifications.service.NotificationService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
public class NotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        // Configurer le mock pour retourner une notification avec ID
        when(notificationService.saveNotification(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L); // Simuler l'ID généré par la base de données
                return n;
            });
    }

    @Test
    void testHandleDepositNotification() throws Exception {
        // Given
        DepositEvent event = new DepositEvent();
        event.setEmail("sorelletamen8@gmail.com");
        event.setAmount(100.0);
        event.setDepositMethod("CARD");
        event.setReferenceNumber("REF123");
        event.setAccountNumber("ACCT123");
        event.setBankName("Test Bank");

        // When
        notificationConsumer.handleDepositNotification(event);

        // Then
        verify(notificationService).saveNotification(any(Notification.class));
        // Vérifier que sendNotification n'est pas appelé car le canal est IN_APP par défaut
        verify(emailSender, org.mockito.Mockito.never()).sendNotification(
            any(String.class), 
            any(EmailSender.NotificationType.class), 
            any(Map.class)
        );
    }

    @Test
    void testHandleWithdrawalNotification() throws Exception {
        // Given
        WithdrawalEvent event = new WithdrawalEvent();
        event.setEmail("sorelletamen8@gmail.com");
        event.setAmount(500.0);
        event.setWithdrawalMethod("ATM");
        event.setAccountNumber("ACCT123");
        event.setBankName("Test Bank");

        // When
        notificationConsumer.handleWithdrawalNotification(event);

        // Then
        verify(notificationService).saveNotification(any(Notification.class));
        // Vérifier que sendNotification n'est pas appelé car le canal est IN_APP par défaut
        verify(emailSender, org.mockito.Mockito.never()).sendNotification(
            any(String.class), 
            any(EmailSender.NotificationType.class), 
            any(Map.class)
        );
    }

    @Test
    void testHandleTransferNotification() throws Exception {
        // Given
        TransferEvent event = new TransferEvent();
        event.setEmail("sorelletamen8@gmail.com");
        event.setAmount(300.0);
        event.setSourceAccount("SENDER123");
        event.setDestinationAccount("RECIPIENT456");
        event.setBeneficiaryName("Bénéficiaire Test");

        // When
        notificationConsumer.handleTransferNotification(event);

        // Then
        verify(notificationService).saveNotification(any(Notification.class));
        // Vérifier que sendNotification n'est pas appelé car le canal est IN_APP par défaut
        verify(emailSender, org.mockito.Mockito.never()).sendNotification(
            any(String.class), 
            any(EmailSender.NotificationType.class), 
            any(Map.class)
        );
    }

    @Test
    void testHandleAuthNotification() throws Exception {
        // Given
        AuthEvent event = new AuthEvent();
        event.setEmail("sorelletamen8@gmail.com");
        event.setAuthType("LOGIN");
        event.setTimestamp(java.time.LocalDateTime.now());
        event.setDeviceInfo("Chrome on Windows");
        event.setLocation("Paris, France");
        event.setSuccess(true);

        // When
        notificationConsumer.handleAuthNotification(event);

        // Then
        verify(notificationService).saveNotification(any(Notification.class));
        // Vérifier que sendNotification n'est pas appelé car le canal est IN_APP par défaut
        verify(emailSender, org.mockito.Mockito.never()).sendNotification(
            any(String.class), 
            any(EmailSender.NotificationType.class), 
            any(Map.class)
        );
    }

    @Test
    void testHandleAccountCreationSuccess() throws Exception {
        // Given
        CustomerEvent event = new CustomerEvent();
        event.setEmail("sorelletamen8@gmail.com");
        event.setFirstName("Jean");
        event.setLastName("Dupont");
        event.setAccountNumber("ACCT123");

        // When
        notificationConsumer.handleAccountCreationSuccess(event);

        // Then
        verify(notificationService).saveNotification(any(Notification.class));
        verify(emailSender).sendNotification(
            any(String.class), 
            any(EmailSender.NotificationType.class), 
            any(Map.class)
        );
    }
}
