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

@Component
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;
    private final EmailSender mailservice;

    public NotificationConsumer(NotificationService notificationService, EmailSender mailservice) {
        this.notificationService = notificationService;
        this.mailservice = mailservice;
    }

    @RabbitListener(queues = "deposit.notification.queue")
    public void handleDepositNotification(DepositEvent event) {
        logger.info("=== Nouvelle notification de dépôt reçue ===");
        logger.info("Email: {}", event.getEmail());
        logger.info("Montant: {}", event.getAmount());
        
        try {
            String message = 
                "Bonjour,<br>" + 
                "Nous vous confirmons que votre dépôt de " + event.getAmount() + " € a été effectué avec succès. <br>" + 
                "Méthode : " + event.getDepositMethod() + "<br>" + 
                "Référence : " + event.getReferenceNumber() + "<br>" + 
                "Banque : " + event.getBankName() + "<br>" + 
                "Compte : " + event.getAccountNumber() + "<br>" + 
                "Cordialement, <br>L'équipe Jamaa";

            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de dépôt");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_DEPOT);
            notification.setServiceEmetteur(ServiceEmetteur.DEPOSIT_SERVICE);

            logger.info("Sauvegarde de la notification dans la base de données...");
            Notification savedNotification = notificationService.saveNotification(notification);
            logger.info("Notification sauvegardée avec l'ID: {}", savedNotification.getId());

            logger.info("Envoi de l'email...");
            mailservice.sendMail(event.getEmail(), "Confirmation de dépôt", message);
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
            String message = 
                "Bonjour,<br>" + 
                "Votre retrait de " + event.getAmount() + " € a été effectué avec succès.<br>" + 
                "Méthode : " + event.getWithdrawalMethod() + "<br>" + 
                "Compte destinataire : " + event.getDestinationAccount() + "<br>" + 
                "Banque : " + event.getBankName() + "<br>" + 
                "Compte : " + event.getAccountNumber() + "<br>" + 
                "Cordialement,<br>L'équipe Jamaa";

            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de retrait");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_RETRAIT);
            notification.setServiceEmetteur(ServiceEmetteur.WITHDRAWAL_SERVICE);

            notificationService.saveNotification(notification);
            mailservice.sendMail(event.getEmail(), "Confirmation de retrait", message);
            logger.info("Notification de retrait traitée pour la transaction: {}", event.getTransactionId());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification de retrait: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "transfer.notification.queue")
    public void handleTransferNotification(TransferEvent event) {
        try {
            String message = 
                "Bonjour,<br>" + 
                "Votre transfert de " + event.getAmount() + " € a été effectué avec succès.<br>" + 
                "Compte source : " + event.getSourceAccount() + "<br>" + 
                "Compte destinataire : " + event.getDestinationAccount() + "<br>" + 
                "Banque destinataire : " + event.getDestinationBank() + "<br>" + 
                "Bénéficiaire : " + event.getBeneficiaryName() + "<br>" + 
                "Motif : " + event.getTransferReason() + "<br>" + 
                "Cordialement,<br>L'équipe Jamaa";

            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de transfert");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_TRANSFERT);
            notification.setServiceEmetteur(ServiceEmetteur.TRANSFER_SERVICE);

            notificationService.saveNotification(notification);
            mailservice.sendMail(event.getEmail(), "Confirmation de transfert", message);
            logger.info("Notification de transfert traitée pour la transaction: {}", event.getTransactionId());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification de transfert: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "account.notification.queue")
    public void handleAccountNotification(AccountEvent event) {
        try {
            String message = 
                "Bonjour,<br>" + 
                "Votre compte a été " + event.getAccountStatus() + " avec succès.<br>" + 
                "Type de compte : " + event.getAccountType() + "<br>" + 
                "Devise : " + event.getCurrency() + "<br>" + 
                "Solde : " + event.getBalance() + " " + event.getCurrency() + "<br>" + 
                "Cordialement,<br>L'équipe Jamaa";

            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Notification de compte");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE);
            notification.setServiceEmetteur(ServiceEmetteur.BANK_SERVICE);

            notificationService.saveNotification(notification);
            mailservice.sendMail(event.getEmail(), "Notification de compte", message);
            logger.info("Notification de compte traitée pour l'utilisateur: {}", event.getUserId());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification de compte: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "auth.notification.queue")
    public void handleAuthNotification(AuthEvent event) {
        try {
            String message = 
                "Bonjour,<br>" + 
                "Votre " + event.getAuthType().toLowerCase() + " a été effectuée avec succès.<br>" + 
                "Appareil : " + event.getDeviceInfo() + "<br>" + 
                "Localisation : " + event.getLocation() + "<br>" + 
                "Cordialement,<br>L'équipe Jamaa";

            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Notification d'authentification");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_INSCRIPTION);
            notification.setServiceEmetteur(ServiceEmetteur.AUTH_SERVICE);

            notificationService.saveNotification(notification);
            mailservice.sendMail(event.getEmail(), "Notification d'authentification", message);
            logger.info("Notification d'authentification traitée pour l'utilisateur: {}", event.getUserId());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification d'authentification: {}", e.getMessage());
        }
    }
} 