package com.jamaa.service_notifications.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamaa.service_notifications.events.*;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import java.time.LocalDateTime;

@Component
public class NotificationTestProducer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationTestProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public NotificationTestProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDepositNotification(String email, String message, LocalDateTime timestamp) {
        DepositEvent event = new DepositEvent();
        event.setEmail(email);
        event.setAmount(1000.0);
        event.setDepositMethod("Carte");
        event.setReferenceNumber("REF123");
        event.setBankName("Jamaa Bank");
        event.setAccountNumber("ACC123");
        event.setTransactionId(java.util.UUID.randomUUID().toString());
        event.setAccountId("ACC-" + java.util.UUID.randomUUID().toString());
        event.setUserId("USER-" + java.util.UUID.randomUUID().toString());
        event.setTimestamp(timestamp);
        event.setStatus("SUCCESS");
        
        rabbitTemplate.convertAndSend("deposit.notification.queue", event);
        logger.info("Événement de dépôt envoyé pour l'utilisateur: {}", email);
    }

    public void sendWithdrawalNotification(String email, String message, LocalDateTime timestamp) {
        WithdrawalEvent event = new WithdrawalEvent();
        event.setEmail(email);
        event.setAmount(500.0);
        event.setWithdrawalMethod("Carte");
        event.setDestinationAccount("ACC456");
        event.setBankName("Jamaa Bank");
        event.setAccountNumber("ACC123");
        event.setTransactionId(java.util.UUID.randomUUID().toString());
        event.setAccountId("ACC-" + java.util.UUID.randomUUID().toString());
        event.setUserId("USER-" + java.util.UUID.randomUUID().toString());
        event.setTimestamp(timestamp);
        event.setStatus("SUCCESS");
        
        rabbitTemplate.convertAndSend("withdrawal.notification.queue", event);
        logger.info("Événement de retrait envoyé pour l'utilisateur: {}", email);
    }

    public void sendTransferNotification(String email, String message, LocalDateTime timestamp) {
        TransferEvent event = new TransferEvent();
        event.setEmail(email);
        event.setAmount(500.0);
        event.setDestinationBank("Jamaa Bank");
        event.setDestinationAccount("ACC456");
        event.setSourceBank("Jamaa Bank");
        event.setSourceAccount("ACC123");
        event.setBeneficiaryName("John Doe");
        event.setTransferReason("Paiement facture");
        event.setTransactionId(java.util.UUID.randomUUID().toString());
        event.setAccountId("ACC-" + java.util.UUID.randomUUID().toString());
        event.setUserId("USER-" + java.util.UUID.randomUUID().toString());
        event.setTimestamp(timestamp);
        event.setStatus("SUCCESS");
        
        rabbitTemplate.convertAndSend("transfer.notification.queue", event);
        logger.info("Événement de transfert envoyé pour l'utilisateur: {}", email);
    }

    public void sendAuthNotification(String email, String message, NotificationType type, LocalDateTime timestamp) {
        AuthEvent event = new AuthEvent();
        event.setEmail(email);
        event.setAuthType("CONFIRMATION_INSCRIPTION");
        event.setDeviceInfo("Chrome/Windows");
        event.setLocation("Paris");
        event.setSuccess(true);
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setUserId("USER-" + java.util.UUID.randomUUID().toString());
        event.setTimestamp(timestamp);
        event.setServiceName("AUTH_SERVICE");
        
        rabbitTemplate.convertAndSend("auth.notification.queue", event);
        logger.info("Événement d'authentification envoyé pour l'utilisateur: {}", email);
    }

    public void sendBankNotification(String email, String message, LocalDateTime timestamp) {
        BankEvent event = new BankEvent();
        event.setEmail(email);
        event.setBankName("Jamaa Bank");
        event.setAccountNumber("ACC123");
        event.setBankCode("JMB");
        event.setBranchCode("PAR001");
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setUserId("USER-" + java.util.UUID.randomUUID().toString());
        event.setTimestamp(timestamp);
        event.setServiceName("BANK_SERVICE");
        
        rabbitTemplate.convertAndSend("bank.notification.queue", event);
        logger.info("Événement bancaire envoyé pour l'utilisateur: {}", email);
    }

    public void sendAccountNotification(String email, String message, LocalDateTime timestamp) {
        AccountEvent event = new AccountEvent();
        event.setEmail(email);
        event.setAccountType("Compte courant");
        event.setAccountStatus("ACTIVE");
        event.setCurrency("EUR");
        event.setBalance(1000.0);
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setUserId("USER-" + java.util.UUID.randomUUID().toString());
        event.setTimestamp(timestamp);
        event.setServiceName("BANK_SERVICE");
        
        rabbitTemplate.convertAndSend("account.notification.queue", event);
        logger.info("Événement de compte envoyé pour l'utilisateur: {}", email);
    }
} 