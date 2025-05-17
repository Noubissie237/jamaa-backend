package com.jamaa.service_notifications.consumer;

import com.jamaa.service_notifications.events.*;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import com.jamaa.service_notifications.model.Notification.ServiceEmetteur;
import com.jamaa.service_notifications.service.NotificationService;
import com.jamaa.service_notifications.service.EmailSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;
    private final EmailSender emailService;

    public NotificationConsumer(NotificationService notificationService, EmailSender emailService) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @RabbitListener(queues = "deposit.notification.queue")
    public void handleDepositNotification(DepositEvent event) {
        logger.info("=== Nouvelle notification de dépôt reçue ===");
        logger.info("Email: {}", event.getEmail());
        logger.info("Montant: {}", event.getAmount());
        
        try {
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("amount", event.getAmount());
            data.put("depositMethod", event.getDepositMethod());
            data.put("referenceNumber", event.getReferenceNumber());
            data.put("bankName", event.getBankName());
            data.put("accountNumber", event.getAccountNumber());
            
            // Message pour la base de données (version simplifiée)
            String message = String.format(
                "Dépôt de %.2f € effectué avec succès via %s (Réf: %s)",
                event.getAmount(), event.getDepositMethod(), event.getReferenceNumber()
            );

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de dépôt");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_DEPOT);
            notification.setServiceEmetteur(ServiceEmetteur.DEPOSIT_SERVICE);

            logger.info("Sauvegarde de la notification dans la base de données...");
            Notification savedNotification = notificationService.saveNotification(notification);
            logger.info("Notification sauvegardée avec l'ID: {}", savedNotification.getId());

            // Envoi de l'email avec le template
            logger.info("Envoi de l'email...");
            emailService.sendNotification(
                event.getEmail(), 
                EmailSender.NotificationType.DEPOSIT,
                data
            );
            
            logger.info("Email envoyé avec succès");
            logger.info("=== Fin du traitement de la notification de dépôt ===");
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "withdrawal.notification.queue")
    public void handleWithdrawalNotification(WithdrawalEvent event) {
        try {
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("amount", event.getAmount());
            data.put("withdrawalMethod", event.getWithdrawalMethod());
            data.put("destinationAccount", event.getDestinationAccount());
            data.put("bankName", event.getBankName());
            data.put("accountNumber", event.getAccountNumber());
            
            // Message pour la base de données
            String message = String.format(
                "Retrait de %.2f € effectué avec succès via %s",
                event.getAmount(), event.getWithdrawalMethod()
            );

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de retrait");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_RETRAIT);
            notification.setServiceEmetteur(ServiceEmetteur.WITHDRAWAL_SERVICE);

            notificationService.saveNotification(notification);
            
            // Envoi de l'email avec le template
            emailService.sendNotification(
                event.getEmail(), 
                EmailSender.NotificationType.WITHDRAWAL,
                data
            );
            
            logger.info("Notification de retrait traitée pour la transaction: {}", event.getTransactionId());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de la notification de retrait: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "transfer.notification.queue")
    public void handleTransferNotification(TransferEvent event) {
        try {
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("amount", event.getAmount());
            data.put("sourceAccount", event.getSourceAccount());
            data.put("destinationAccount", event.getDestinationAccount());
            data.put("destinationBank", event.getDestinationBank());
            data.put("beneficiaryName", event.getBeneficiaryName());
            data.put("transferReason", event.getTransferReason());
            
            // Message pour la base de données
            String message = String.format(
                "Transfert de %.2f € effectué vers %s (%s)",
                event.getAmount(), event.getBeneficiaryName(), event.getDestinationBank()
            );

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de transfert");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_TRANSFERT);
            notification.setServiceEmetteur(ServiceEmetteur.TRANSFER_SERVICE);

            notificationService.saveNotification(notification);
            
            // Envoi de l'email avec le template
            emailService.sendNotification(
                event.getEmail(), 
                EmailSender.NotificationType.TRANSFER,
                data
            );
            
            logger.info("Notification de transfert traitée pour la transaction: {}", event.getTransactionId());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de la notification de transfert: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "account.notification.queue")
    public void handleAccountNotification(AccountEvent event) {
        try {
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("accountStatus", event.getAccountStatus());
            data.put("accountType", event.getAccountType());
            data.put("currency", event.getCurrency());
            data.put("balance", event.getBalance());
            
            // Message pour la base de données
            String message = String.format(
                "Compte %s %s. Solde initial: %.2f %s",
                event.getAccountType(), event.getAccountStatus(), 
                event.getBalance(), event.getCurrency()
            );

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Notification de compte");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE);
            notification.setServiceEmetteur(ServiceEmetteur.BANK_SERVICE);

            notificationService.saveNotification(notification);
            
            // Envoi de l'email avec le template
            emailService.sendNotification(
                event.getEmail(), 
                EmailSender.NotificationType.ACCOUNT,
                data
            );
            
            logger.info("Notification de compte traitée pour l'utilisateur: {}", event.getUserId());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de la notification de compte: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "auth.notification.queue")
    public void handleAuthNotification(AuthEvent event) {
        try {
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("authType", event.getAuthType().toLowerCase());
            data.put("deviceInfo", event.getDeviceInfo());
            data.put("location", event.getLocation());
            
            // Message pour la base de données
            String message = String.format(
                "%s effectuée depuis %s à %s",
                event.getAuthType(), event.getDeviceInfo(), event.getLocation()
            );

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Notification d'authentification");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_INSCRIPTION);
            notification.setServiceEmetteur(ServiceEmetteur.AUTH_SERVICE);

            notificationService.saveNotification(notification);
            
            // Envoi de l'email avec le template
            emailService.sendNotification(
                event.getEmail(), 
                EmailSender.NotificationType.AUTHENTICATION,
                data
            );
            
            logger.info("Notification d'authentification traitée pour l'utilisateur: {}", event.getUserId());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de la notification d'authentification: {}", e.getMessage());
        }
    }
    
    // Ajout de deux nouveaux listeners pour les notifications supplémentaires
    
    @RabbitListener(queues = "suspicious.activity.queue")
    public void handleSuspiciousActivityNotification(SuspiciousActivityEvent event) {
        try {
            // Utilisation de la méthode spécialisée
            emailService.sendSuspiciousActivityAlert(
                event.getEmail(),
                event.getActivityType(),
                event.getLocation(),
                event.getDeviceInfo(),
                event.getActivityTime()
            );
            
            // Création et sauvegarde de la notification
            String message = String.format(
                "Activité suspecte détectée: %s depuis %s à %s",
                event.getActivityType(), event.getDeviceInfo(), event.getLocation()
            );
            
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Alerte de sécurité");
            notification.setMessage(message);
            notification.setType(NotificationType.ALERTE_SECURITE);
            notification.setServiceEmetteur(ServiceEmetteur.SECURITY_SERVICE);

            notificationService.saveNotification(notification);
            
            logger.info("Notification d'activité suspecte envoyée: {}", event.getActivityId());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de l'alerte d'activité suspecte: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "insufficient.funds.queue")
    public void handleInsufficientFundsNotification(InsufficientFundsEvent event) {
        try {
            // Utilisation de la méthode spécialisée
            emailService.sendInsufficientFundsAlert(
                event.getEmail(),
                event.getAccountNumber(),
                event.getCurrentBalance(),
                event.getRequiredAmount(),
                event.getTransactionType()
            );
            
    //         // Création et sauvegarde de la notification
            String message = String.format(
                "Solde insuffisant pour %s. Solde actuel: %.2f €, Montant requis: %.2f €",
                event.getTransactionType(), event.getCurrentBalance(), event.getRequiredAmount()
            );
            
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Alerte de solde insuffisant");
            notification.setMessage(message);
            notification.setType(NotificationType.ALERTE_SOLDE);
            notification.setServiceEmetteur(ServiceEmetteur.TRANSACTION_SERVICE);

            notificationService.saveNotification(notification);
            
            logger.info("Notification de solde insuffisant envoyée pour le compte: {}", event.getAccountNumber());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de l'alerte de solde insuffisant: {}", e.getMessage());
        }
    }

}