package com.jamaa.service_notifications.consumer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.jamaa.service_notifications.events.AccountEvent;
import com.jamaa.service_notifications.events.AuthEvent;
import com.jamaa.service_notifications.events.CustomerEvent;
import com.jamaa.service_notifications.events.DepositEvent;
import com.jamaa.service_notifications.events.InsufficientFundsEvent;
import com.jamaa.service_notifications.events.RechargeEvent;
import com.jamaa.service_notifications.events.TransferEvent;
import com.jamaa.service_notifications.events.WithdrawalEvent;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import com.jamaa.service_notifications.model.Notification.ServiceEmetteur;
import com.jamaa.service_notifications.service.EmailSender;
import com.jamaa.service_notifications.service.NotificationService;

import jakarta.mail.MessagingException;

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
                    event.getAmount(), event.getDepositMethod(), event.getReferenceNumber());

            // Création de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de dépôt");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_DEPOT);
            notification.setServiceEmetteur(ServiceEmetteur.DEPOSIT_SERVICE);
            
            // Définir le canal (pourrait aussi venir de l'événement)
            notification.setCanal(Notification.CanalNotification.IN_APP);

            // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            
            logger.info("=== Fin du traitement de la notification de dépôt ===");
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification de dépôt: {}", e.getMessage());
        }
    }

    /**
     * Traite une notification en fonction de son canal
     * - Pour les notifications IN_APP : sauvegarde simplement la notification
     * - Pour les notifications EMAIL : sauvegarde et envoie un email
     */
    private void traiterNotification(Notification notification) {
        logger.info("Traitement d'une notification de type: {}", notification.getType());
        logger.info("Canal: {}", notification.getCanal());
        
        try {
            // Sauvegarder la notification
            Notification savedNotification = notificationService.saveNotification(notification);
            logger.info("Notification sauvegardée avec l'ID: {}", savedNotification.getId());
            
            // Si c'est une notification par email, on l'envoie
            if (notification.getCanal() == Notification.CanalNotification.EMAIL) {
                try {
                    // Préparer les données pour le template
                    Map<String, Object> data = new HashMap<>();
                    data.put("message", notification.getMessage());
                    // Vous pouvez ajouter d'autres données spécifiques ici si nécessaire
                    
                    // Mapper le type de notification au type d'email
                    EmailSender.NotificationType emailType = mapToEmailType(notification.getType());
                    
                    
                    // Envoyer l'email
                    logger.info("Envoi de l'email...");
                    emailService.sendNotification(
                        notification.getEmail(),
                        emailType,
                        data
                    );
                    logger.info("Email envoyé avec succès");
                } catch (MessagingException | IOException e) {
                    logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification: {}", e.getMessage());
        }
    }
    
    /**
     * Mappe un type de notification vers un type d'email
     */
    private EmailSender.NotificationType mapToEmailType(Notification.NotificationType type) {
        // Mappage des types de notification vers les types d'email
        switch (type) {
            case CONFIRMATION_DEPOT:
                return EmailSender.NotificationType.DEPOSIT;
            case CONFIRMATION_RETRAIT:
                return EmailSender.NotificationType.WITHDRAWAL;
            case CONFIRMATION_TRANSFERT:
                return EmailSender.NotificationType.TRANSFER;
            case CONFIRMATION_INSCRIPTION:
            case CONFIRMATION_SOUSCRIPTION_BANQUE:
                return EmailSender.NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE;
            case MOT_DE_PASSE_REINITIALISE:
                return EmailSender.NotificationType.PASSWORD_CHANGE;
            case RECHARGE:
                return EmailSender.NotificationType.RECHARGE;
            case SUPPRESSION_COMPTE:
            case ACCOUNT_DELETION:
                return EmailSender.NotificationType.ACCOUNT_DELETION;
            case ERREUR_CREATION_COMPTE:
            case ACCOUNT_CREATION_ERROR:
                return EmailSender.NotificationType.ACCOUNT_CREATION_ERROR;
            case ALERTE_SOLDE:
                return EmailSender.NotificationType.INSUFFICIENT_FUNDS;
            case AUTHENTIFICATION:
                return EmailSender.NotificationType.AUTHENTICATION;
            default:
                return EmailSender.NotificationType.ACCOUNT; // Type par défaut
        }
    }

    @RabbitListener(queues = "withdrawal.notification.queue")
    public void handleWithdrawalNotification(WithdrawalEvent event) {
        try {
            // Message pour la base de données
            String message = String.format(
                    "Retrait de %.2f € effectué avec succès via %s",
                    event.getAmount(), event.getWithdrawalMethod());

            // Création de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de retrait");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_RETRAIT);
            notification.setServiceEmetteur(ServiceEmetteur.WITHDRAWAL_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            
            logger.info("Notification de retrait traitée pour: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification de retrait: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "transfer.notification.queue")
    public void handleTransferNotification(TransferEvent event) {
        try {
            // Message pour la base de données
            String message = String.format(
                    "Transfert de %.2f € vers %s (Compte: %s) effectué avec succès",
                    event.getAmount(), event.getBeneficiaryName(), event.getDestinationAccount());

            // Création de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de transfert");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_TRANSFERT);
            notification.setServiceEmetteur(ServiceEmetteur.TRANSFER_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            
            logger.info("Notification de transfert traitée pour: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification de transfert: {}", e.getMessage());
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
                    event.getBalance(), event.getCurrency());

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Notification de compte");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_INSCRIPTION);
            notification.setServiceEmetteur(ServiceEmetteur.BANK_SERVICE);

            notificationService.saveNotification(notification);

            // Envoi de l'email avec le template
            emailService.sendNotification(
                    event.getEmail(),
                    EmailSender.NotificationType.ACCOUNT,
                    data);

            logger.info("Notification de compte traitée pour l'utilisateur: {}", event.getUserId());
        } catch (MessagingException | IOException e) {
            logger.error("Erreur lors de l'envoi de la notification de compte: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "recharge.notification.queue")
    public void handleRechargeNotification(RechargeEvent event) {
        logger.info("=== Nouvelle notification de recharge reçue ===");
        logger.info("Email: {}", event.getEmail());
        logger.info("Montant: {}", event.getAmount());

        try {
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("amount", event.getAmount());
            data.put("rechargeMethod", event.getRechargeMethod());
            data.put("referenceNumber", event.getReferenceNumber());
            data.put("phoneNumber", event.getPhoneNumber());
            data.put("operatorName", event.getOperatorName());
            data.put("accountNumber", event.getAccountNumber());

            // Message pour la base de données (version simplifiée)
            String message = String.format(
                    "Recharge de %.2f FCFA effectuée avec succès pour le numéro %s (%s) - Réf: %s",
                    event.getAmount(), event.getPhoneNumber(), event.getOperatorName(), event.getReferenceNumber());

            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Confirmation de recharge");
            notification.setMessage(message);
            notification.setType(NotificationType.CONFIRMATION_RECHARGE);
            notification.setServiceEmetteur(ServiceEmetteur.RECHARGE_SERVICE);

            logger.info("Sauvegarde de la notification dans la base de données...");
            Notification savedNotification = notificationService.saveNotification(notification);
            logger.info("Notification sauvegardée avec l'ID: {}", savedNotification.getId());

            // Envoi de l'email avec le template
            logger.info("Envoi de l'email...");
            emailService.sendNotification(
                    event.getEmail(),
                    EmailSender.NotificationType.RECHARGE,
                    data);

            logger.info("Email envoyé avec succès");
            logger.info("=== Fin du traitement de la notification de recharge ===");
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "auth.notification.queue")
    public void handleAuthNotification(AuthEvent event) {
        try {
            // Message pour la base de données
            String message = String.format("Action d'authentification: %s", event.getAuthType());
            
            // Création de la notification
            Notification notification = new Notification();
            notification.setEmail(event.getEmail());
            notification.setTitle("Notification d'authentification");
            notification.setMessage(message);
            notification.setType(NotificationType.AUTHENTIFICATION);
            notification.setServiceEmetteur(ServiceEmetteur.AUTH_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            
            logger.info("Notification d'authentification traitée pour: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification d'authentification: {}", e.getMessage());
        }
    }

    // Ajout de deux nouveaux listeners pour les notifications supplémentaires

    @RabbitListener(queues = "insufficient.funds.queue")
    public void handleInsufficientFundsNotification(InsufficientFundsEvent event) {
        try {
            // Utilisation de la méthode spécialisée
            emailService.sendInsufficientFundsAlert(
                    event.getEmail(),
                    event.getAccountNumber(),
                    event.getCurrentBalance(),
                    event.getRequiredAmount(),
                    event.getTransactionType());

            // // Création et sauvegarde de la notification
            String message = String.format(
                    "Solde insuffisant pour %s. Solde actuel: %.2f €, Montant requis: %.2f €",
                    event.getTransactionType(), event.getCurrentBalance(), event.getRequiredAmount());

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

    @RabbitListener(queues = "accountCreateQueue")
    public void handleRegistrationNotification(CustomerEvent event) {
        logger.info("=== Notification de création de compte reçue ===");
        logger.info("Email: {}", event.getEmail());

        try {
            // Vérifier s'il y a une erreur
            if (event.getErrorMessage() != null && !event.getErrorMessage().isEmpty()) {
                logger.info("=== Traitement d'une erreur de création de compte ===");
                handleAccountCreationError(event);
            } else {
                logger.info("=== Traitement d'un succès de création de compte ===");
                handleAccountCreationSuccess(event);
            }

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("Erreur d'entrée/sortie: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue: {}", e.getMessage());
        }
    }

    /**
     * Méthode pour gérer les créations de compte réussies
     * @throws Exception 
     */
   public void handleAccountCreationSuccess(CustomerEvent event) throws Exception {
       try {
           // Validation des données
           if (event.getEmail() == null || event.getEmail().isEmpty()) {
               throw new IllegalArgumentException("L'email du client est requis");
           }
   
           // Préparation des données
           String fullName = (event.getFirstName() != null ? event.getFirstName() + " " : "") + 
                            (event.getLastName() != null ? event.getLastName() : "").trim();
           
           Map<String, Object> data = new HashMap<>();
           data.put("email", event.getEmail());
           data.put("firstName", event.getFirstName());
           data.put("lastName", event.getLastName());
           data.put("fullName", fullName);
           data.put("accountNumber", event.getAccountNumber());
           data.put("registrationDate", LocalDate.now());
           // À remplacer par
          //data.put("verificationToken", event.getVerificationToken());
           data.put("verificationToken", "lXZxIoGzJvnl/Eh9AvMnkjptA3nIMSK6DmFJgWd3Pc8=");
           data.put("year", LocalDate.now().getYear());
   
           // Création de la notification
           Notification notification = new Notification();
           notification.setEmail(event.getEmail());
           notification.setTitle("Confirmation d'inscription");
           notification.setMessage(String.format(
               "Client %s enregistré avec succès - Compte: %s",
               event.getEmail(), 
               event.getAccountNumber() != null ? event.getAccountNumber() : "N/A"
           ));
           notification.setType(NotificationType.CONFIRMATION_SOUSCRIPTION_BANQUE);
           notification.setServiceEmetteur(ServiceEmetteur.AUTH_SERVICE);
           notification.setCanal(Notification.CanalNotification.EMAIL); // Définition du canal
  
           EmailSender.NotificationType emailType = mapToEmailType(notification.getType());
                    
           // Envoyer l'email
           logger.info("Envoi de l'email...");
           emailService.sendNotification(
               notification.getEmail(),
               emailType,
               data
           );
   
           logger.info("Traitement de la création de compte réussi pour: {}", event.getEmail());
       } catch (Exception e) {
           logger.error("Erreur lors du traitement de la création de compte pour {}: {}", 
                      event.getEmail(), e.getMessage(), e);
           throw e; // À adapter selon la stratégie de gestion d'erreur
       }
   }
    /**
     * Méthode pour gérer les erreurs de création de compte
     */
    public void handleAccountCreationError(CustomerEvent event) throws MessagingException, IOException {
        logger.info("Traitement de l'erreur de création de compte pour: {}", event.getEmail());
        logger.info("Erreur: {}", event.getErrorMessage());

        // Préparation des données pour le template d'ERREUR
        Map<String, Object> data = new HashMap<>();
        data.put("email", event.getEmail());
        data.put("firstName", event.getFirstName());
        data.put("lastName", event.getLastName());
        data.put("fullName", event.getFirstName() + " " + event.getLastName());
        data.put("attemptDate", LocalDate.now());
        data.put("errorMessage", event.getErrorMessage());
        data.put("year", LocalDate.now().getYear());

        // Message pour la base de données
        String message = String.format(
                "Échec de création de compte pour %s - %s ",
                event.getEmail(), event.getErrorMessage());

        // Création et sauvegarde de la notification d'ERREUR
        Notification notification = new Notification();
        notification.setEmail(event.getEmail());
        notification.setTitle("Informations à corriger");
        notification.setMessage(message);
        notification.setType(NotificationType.ERREUR_CREATION_COMPTE);
        notification.setServiceEmetteur(ServiceEmetteur.AUTH_SERVICE);

        logger.info("Sauvegarde de la notification d'erreur dans la base de données...");
        Notification savedNotification = notificationService.saveNotification(notification);
        logger.info("Notification d'erreur sauvegardée avec l'ID: {}", savedNotification.getId());

        // Envoi de l'email avec le template d'ERREUR
        logger.info("Envoi de l'email d'erreur...");
        emailService.sendNotification(
                event.getEmail(),
                EmailSender.NotificationType.ACCOUNT_CREATION_ERROR,
                data);

        logger.info("Email d'erreur envoyé avec succès pour: {}", event.getEmail());
        logger.info("=== Fin du traitement d'erreur ===");
    }

}