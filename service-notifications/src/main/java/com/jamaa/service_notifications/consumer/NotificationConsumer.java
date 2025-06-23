package com.jamaa.service_notifications.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.jamaa.service_notifications.dto.CardDTO;
import com.jamaa.service_notifications.dto.UserInfoResponse;
import com.jamaa.service_notifications.events.AccountEvent;
import com.jamaa.service_notifications.events.CustomerEvent;
import com.jamaa.service_notifications.events.RechargeEvent;
import com.jamaa.service_notifications.events.TransfertEvent;
import com.jamaa.service_notifications.events.WithdrawalEvent;
import com.jamaa.service_notifications.model.Notification;
import com.jamaa.service_notifications.dto.AccountDTO;
import com.jamaa.service_notifications.dto.CardDTO;
import com.jamaa.service_notifications.model.Notification.NotificationType;
import com.jamaa.service_notifications.model.Notification.ServiceEmetteur;
import com.jamaa.service_notifications.service.EmailSender;
import com.jamaa.service_notifications.service.NotificationService;
import com.jamaa.service_notifications.utils.CardUtil;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.jamaa.service_notifications.utils.AccountUtil;

@Slf4j
@Component
public class NotificationConsumer {
    @Autowired
    private AccountUtil accountUtil;

    @Autowired
    private CardUtil cardUtil;

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;
    private final EmailSender emailService;

    public NotificationConsumer(NotificationService notificationService, EmailSender emailService) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }
    private String getLastFourDigits(String cardNumber) {
        return cardNumber != null && cardNumber.length() > 4 
            ? cardNumber.substring(cardNumber.length() - 4)
            : cardNumber;
    }

    /**
     * Traite une notification IN_APP
     * - Sauvegarde la notification pour affichage dans l'application
     * - Ne gère pas l'envoi d'emails
     */
    private void traiterNotification(Notification notification) {
        try {
            // Vérifier que c'est bien une notification IN_APP
            if (notification.getCanal() != Notification.CanalNotification.IN_APP) {
                logger.warn("Seules les notifications IN_APP sont traitées par cette méthode");
                return;
            }
            
            // Sauvegarder la notification pour l'application
            notificationService.saveNotification(notification);
            logger.info("Notification IN_APP enregistrée: {}", notification.getTitle());
            
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification IN_APP: {}", e.getMessage());
        }
    }
    
    /**
     * Mappe un type de notification vers un type d'email
     */
    private EmailSender.NotificationType mapToEmailType(Notification.NotificationType type) {
        // Mappage des types de notification vers les types d'email
        switch (type) {
            case CONFIRMATION_RETRAIT:
                return EmailSender.NotificationType.WITHDRAWAL;
            case CONFIRMATION_TRANSFERT:
                return EmailSender.NotificationType.TRANSFER;
            case CONFIRMATION_CREATION_COMPTE_JAMAA:
                return EmailSender.NotificationType.CONFIRMATION_CREATION_COMPTE_JAMAA;
            case MOT_DE_PASSE_REINITIALISE:
                return EmailSender.NotificationType.PASSWORD_CHANGE;
            case RECHARGE:
                return EmailSender.NotificationType.RECHARGE;
            case ACCOUNT_CREATION_ERROR:
                return EmailSender.NotificationType.ACCOUNT_CREATION_ERROR;
            case CONFIRMATION_CREATION_CARTE:
                return EmailSender.NotificationType.CARD_CREATED;
            case MISE_A_JOUR_CARTE:
                return EmailSender.NotificationType.CARD_UPDATED;
            case ACTIVATION_CARTE:
                return EmailSender.NotificationType.CARD_ACTIVATED;
            case BLOCAGE_CARTE:
                return EmailSender.NotificationType.CARD_BLOCKED;
            case SUPPRESSION_CARTE:
                return EmailSender.NotificationType.CARD_DELETED;
            case ERREUR_CARTE:
                return EmailSender.NotificationType.CARD_ERROR;
            default:
                return EmailSender.NotificationType.ACCOUNT; // Type par défaut
        }
    }

    @RabbitListener(queues = "notification.retrait.done")
    public void handleRetraitDone(WithdrawalEvent event) {
        try {
            logger.info("=== Notification de retrait reçue ===");
            logger.info("Account ID: {}, Card ID: {}, Montant: {}", 
                      event.getAccountId(), event.getCardId(), event.getAmount());

             // Récupération des détails de la carte avant suppression si possible
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            
            // logger.debug("Récupération des informations du compte {}", event.getAccountId());
            // AccountDTO account = accountUtil.getAccount(event.getAccountId());
            

            // Préparer les données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("amount", event.getAmount());
            data.put("operationType", "retrait");
            data.put("status", event.getStatus());
            data.put("transactionDate", event.getCreatedAt());
            
            if (cardDetails != null) {
                data.put("cardLastFour", cardDetails.getCardNumber() != null && cardDetails.getCardNumber().length() > 4 ? 
                    cardDetails.getCardNumber().substring(cardDetails.getCardNumber().length() - 4) : "••••");
                data.put("cardType", cardDetails.getCardType());
                data.put("holderName", cardDetails.getHolderName());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
            }

            // Création de la notification
            Notification notification = new Notification();
            // notification.setEmail(event.getAccountId() + "@jamaa.com");
            notification.setTitle("Confirmation de retrait");
            notification.setMessage(String.format("Retrait effectué avec succès\n\nMontant : %s FCFA\nDate : %s\nStatut : Complété", event.getAmount(), event.getCreatedAt()));
            UserInfoResponse userInfo = accountUtil.getUserInfoByAccountId(event.getAccountId());
            notification.setUserId(userInfo.getUserId());
            notification.setType(NotificationType.CONFIRMATION_RETRAIT);
            notification.setServiceEmetteur(ServiceEmetteur.WITHDRAWAL_SERVICE);
            
             // Définir le canal (pourrait aussi venir de l'événement)
            notification.setCanal(Notification.CanalNotification.IN_APP);
            // Sauvegarder la notification
            notificationService.saveNotification(notification);
             // Traiter la notification (sauvegarde + envoi si nécessaire)
             traiterNotification(notification);
           
            // Envoyer l'email avec le template
            logger.info("Envoi de l'email de confirmation de retrait...");
            emailService.sendNotification(
                userInfo.getUserEmail(),
                EmailSender.NotificationType.WITHDRAWAL,
                data
            );

            logger.info("Notification de retrait traitée pour le compte: {}", event.getAccountId());
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification de retrait: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "notification.transfer.done")
    public void handleTransferNotification(TransfertEvent event) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("idAccountSender", event.getIdAccountSender());
            data.put("idAccountReceiver", event.getIdAccountReceiver());
            data.put("amount", event.getAmount());
            data.put("status", event.getStatus());
            data.put("createdAt", event.getCreatedAt());
    
            String message = String.format(
                "Transfert effectué avec succès\n\nMontant : %.2f FCFA\nCompte émetteur : %d\nCompte bénéficiaire : %d\nDate : %s\nStatut : %s",
                event.getAmount(),
                event.getIdAccountSender(),
                event.getIdAccountReceiver(),
                event.getCreatedAt(),
                event.getStatus()
            );
    
            Notification notification = new Notification();
            notification.setTitle("Notification de Transfert");
            notification.setMessage(message);
            UserInfoResponse userInfo = accountUtil.getUserInfoByAccountId(event.getIdAccountSender());
            notification.setUserId(userInfo.getUserId());
            notification.setType(NotificationType.CONFIRMATION_TRANSFERT);
            notification.setServiceEmetteur(ServiceEmetteur.TRANSFER_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);
            notificationService.saveNotification(notification);
             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);

            // notification receveur

            Notification notification2 = new Notification();
            notification2.setTitle("Notification de Transfert");
            notification2.setMessage(message);
            UserInfoResponse userInfo2 = accountUtil.getUserInfoByAccountId(event.getIdAccountReceiver());
            notification2.setUserId(userInfo2.getUserId());
            notification2.setType(NotificationType.CONFIRMATION_TRANSFERT);
            notification2.setServiceEmetteur(ServiceEmetteur.TRANSFER_SERVICE);
            notification2.setCanal(Notification.CanalNotification.IN_APP);
            notificationService.saveNotification(notification2);
             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification2);

            // Envoi de l'email avec le template
                    
           // Envoyer l'email
           logger.info("Envoi de l'email...");
           emailService.sendNotification(
               userInfo2.getUserEmail(),
               EmailSender.NotificationType.TRANSFER,
               data
           );
    
            // emailService.sendNotification(..., data, ...); // à adapter selon ta logique d'email
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification de transfert: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "card.created.notification")
    public void handleCardCreated(AccountEvent event) {
        try {
            // Récupération des détails complets de la carte
          
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            
            // Champs de l'événement (toujours disponibles)
            data.put("cardNumber", event.getCardNumber());
            data.put("holderName", event.getHolderName());
            data.put("customerId", event.getCustomerId());
            data.put("cardType", event.getCardType());
            data.put("expiryDate", event.getExpiryDate());
            data.put("creditLimit", event.getCreditLimit());
            
            // Ajout des détails supplémentaires si disponibles
            if (cardDetails != null) {
                data.put("cardStatus", cardDetails.getStatus());
                data.put("createdAt", cardDetails.getCreatedAt());
                data.put("updatedAt", cardDetails.getUpdatedAt());
                data.put("availableCredit", cardDetails.getAvailableCredit());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
           
                // S'assurer que les champs essentiels sont bien renseignés
                if (event.getHolderName() == null && cardDetails.getHolderName() != null) {
                    data.put("holderName", cardDetails.getHolderName());
                }
                if (event.getExpiryDate() == null && cardDetails.getExpiryDate() != null) {
                    data.put("expiryDate", cardDetails.getExpiryDate());
                }
            }
            
            // Message pour la base de données
            String message = String.format("Nouvelle carte créée\n\nNuméro de carte : %s\nType de carte : %s\nTitulaire : %s\nLimite de crédit : %.2f FCFA\nDate d'expiration : %s\nDate de création : %s\nBanque : %s", 
                event.getCardNumber(),
                event.getCardType(),
                event.getHolderName(),
                event.getCreditLimit(),
                event.getExpiryDate(),
                LocalDateTime.now(),
                cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank"
            );
            
            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setTitle("Nouvelle carte créée");
            notification.setMessage(message);
            String userId = cardDetails.getCustomerId().toString();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.CONFIRMATION_CREATION_CARTE);
            notification.setServiceEmetteur(Notification.ServiceEmetteur.CARD_SERVICE);

            notification.setCanal(Notification.CanalNotification.IN_APP);
            notificationService.saveNotification(notification);
             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            
            
            // // Envoi de l'email avec le template
            // emailService.sendNotification(
            //         ,
            //         EmailSender.NotificationType.CARD_CREATED,
            //         data);

            logger.info("Notification de création de carte traitée pour l'utilisateur: {}", event.getCustomerId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de création de carte: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "card.updated.notification")
    public void handleCardUpdated(AccountEvent event) {
        try {
            // Récupération des détails complets de la carte
            
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            
            // Champs de l'événement
            data.put("cardNumber", event.getCardNumber());
            data.put("holderName", event.getHolderName());
            data.put("cardType", event.getCardType());
            data.put("creditLimit", event.getCreditLimit());
            
            // Ajout des détails supplémentaires si disponibles
            if (cardDetails != null) {
                data.put("expiryDate", cardDetails.getExpiryDate());
                data.put("cardStatus", cardDetails.getStatus());
                data.put("updatedAt", cardDetails.getUpdatedAt());
                data.put("availableCredit", cardDetails.getAvailableCredit());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
           
            }
            
            // Message pour la base de données
            String message = String.format("Mise à jour de carte\n\nNuméro de carte : %s\nType de carte : %s\nNouvelle limite de crédit : %.2f FCFA\nDate de mise à jour : %s\nStatut : %s", 
                event.getCardNumber(),
                event.getCardType(),
                event.getCreditLimit(),
                LocalDateTime.now(),
                cardDetails.getStatus()
            );
            
            // Création et sauvegarde de la notification
            Notification notification = new Notification();
             notification.setTitle("Mise à jour de carte");
            notification.setMessage(message);
            String userId = cardDetails.getCustomerId().toString();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.MISE_A_JOUR_CARTE);
            notification.setServiceEmetteur(Notification.ServiceEmetteur.CARD_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);
            notificationService.saveNotification(notification);             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);

            // // Envoi de l'email avec le template
            // emailService.sendNotification(
            //         event.getEmail(),
            //         EmailSender.NotificationType.CARD_UPDATED,
            //         data);
            logger.info("Notification de mise à jour de carte traitée pour la carte: {}", event.getCardNumber());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de mise à jour de carte: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "card.activated.notification")
    public void handleCardActivated(AccountEvent event) {
        try {
            // Récupération des détails complets de la carte
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            
            // Champs de l'événement (toujours disponibles)
            data.put("cardNumber", event.getCardNumber());
            data.put("cardType", event.getCardType());
            data.put("holderName", event.getHolderName());
            data.put("customerId", event.getCustomerId());
            data.put("activationDate", LocalDateTime.now().toString());
            
            // Ajout des détails supplémentaires si disponibles
            if (cardDetails != null) {
                data.put("expiryDate", cardDetails.getExpiryDate());
                data.put("creditLimit", cardDetails.getCreditLimit());
                data.put("availableCredit", cardDetails.getAvailableCredit());
                data.put("cardStatus", cardDetails.getStatus());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
           
                // S'assurer que les champs essentiels sont bien renseignés
                if (event.getHolderName() == null && cardDetails.getHolderName() != null) {
                    data.put("holderName", cardDetails.getHolderName());
                }
            }
            
            // Message pour la base de données
            String message = String.format("Carte activée avec succès\n\nNuméro de carte : %s\nType de carte : %s\nDate d'activation : %s\nStatut : Active\nLimite de crédit : %.2f FCFA\nDate d'expiration : %s", 
                event.getCardNumber(),
                event.getCardType(),
                LocalDateTime.now(),
                cardDetails.getCreditLimit(),
                cardDetails.getExpiryDate()
            );
            
            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setTitle("Carte activée");
            notification.setMessage(message);
            String userId = cardDetails.getCustomerId().toString();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.ACTIVATION_CARTE);
            notification.setServiceEmetteur(Notification.ServiceEmetteur.CARD_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            notificationService.saveNotification(notification);             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            // Envoi de l'email avec le template
            // emailService.sendNotification(
            //         event.getEmail(),
            //         EmailSender.NotificationType.CARD_ACTIVATED,
            //         data);

            logger.info("Notification d'activation de carte traitée pour la carte: {}", 
                       getLastFourDigits(event.getCardNumber()));
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'activation de la carte: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "card.blocked.notification")
    public void handleCardBlocked(AccountEvent event) {
        try {
            // Récupération des détails complets de la carte
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            
            
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            
            // Champs de l'événement (toujours disponibles)
            data.put("cardNumber", event.getCardNumber());
            data.put("cardType", event.getCardType());
            data.put("holderName", event.getHolderName());
            data.put("customerId", event.getCustomerId());
            data.put("blockingDate", LocalDateTime.now().toString());
            //data.put("blockingReason", event.getBlockingReason());
            
            // Ajout des détails supplémentaires si disponibles
            if (cardDetails != null) {
                data.put("expiryDate", cardDetails.getExpiryDate());
                data.put("creditLimit", cardDetails.getCreditLimit());
                data.put("availableCredit", cardDetails.getAvailableCredit());
                data.put("cardStatus", cardDetails.getStatus());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
           
                // S'assurer que les champs essentiels sont bien renseignés
                if (event.getHolderName() == null && cardDetails.getHolderName() != null) {
                    data.put("holderName", cardDetails.getHolderName());
                }
            }
            
            // Message pour la base de données
            String message = String.format("Carte bloquée\n\nNuméro de carte : %s\nType de carte : %s\nTitulaire : %s\nDate de blocage : %s\nStatut : Bloquée\nBanque : %s\n\nVeuillez contacter le service client pour plus d'informations.", 
                event.getCardNumber(),
                event.getCardType(),
                event.getHolderName(),
                LocalDateTime.now(),
                cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank"
            );
            
            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setTitle("Carte bloquée");
            notification.setMessage(message);
            String userId = cardDetails.getCustomerId().toString();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.BLOCAGE_CARTE);
            notification.setServiceEmetteur(Notification.ServiceEmetteur.CARD_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            notificationService.saveNotification(notification);             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            // Envoi de l'email avec le template
            // emailService.sendNotification(
            //         event.getEmail(),
            //         EmailSender.NotificationType.CARD_BLOCKED,
            //         data);

            logger.info("Notification de blocage de carte traitée pour la carte: {}", 
                      event.getCardNumber() != null ? 
                      "••••" + event.getCardNumber().substring(Math.max(0, event.getCardNumber().length() - 4)) : 
                      "[numéro non disponible]");
        } catch (Exception e) {
            logger.error("Erreur lors du traitement du blocage de la carte: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "card.error.notification")
    public void handleCardError(AccountEvent event) {
        try {
            // Récupération des détails complets de la carte
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            
            
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            
            // Champs de l'événement (toujours disponibles)
            data.put("cardNumber", event.getCardNumber());
            data.put("cardType", event.getCardType());
            data.put("holderName", event.getHolderName());
            data.put("customerId", event.getCustomerId());
           // data.put("errorMessage", event.getErrorMessage());
          //  data.put("errorCode", event.getErrorCode());
            data.put("timestamp", LocalDateTime.now().toString());
            
            // Ajout des détails supplémentaires si disponibles
            if (cardDetails != null) {
                data.put("expiryDate", cardDetails.getExpiryDate());
                data.put("cardStatus", cardDetails.getStatus());
                data.put("creditLimit", cardDetails.getCreditLimit());
                data.put("availableCredit", cardDetails.getAvailableCredit());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
           
                // S'assurer que les champs essentiels sont bien renseignés
                if (event.getHolderName() == null && cardDetails.getHolderName() != null) {
                    data.put("holderName", cardDetails.getHolderName());
                }
            }
            
            // Message pour la base de données
            String message = String.format("Erreur sur la carte\n\nNuméro de carte : %s\nType de carte : %s\nTitulaire : %s\nDate de l'erreur : %s\nStatut : %s\nBanque : %s\n\nVeuillez contacter le service client pour plus d'informations.", 
                event.getCardNumber(),
                event.getCardType(),
                event.getHolderName(),
                LocalDateTime.now(),
                cardDetails.getStatus(),
                cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank"
            );
            
            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setTitle("Erreur avec votre carte");
            notification.setMessage(message);
            String userId = cardDetails.getCustomerId().toString();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.ERREUR_CARTE);
            notification.setServiceEmetteur(Notification.ServiceEmetteur.CARD_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            notificationService.saveNotification(notification);             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            // Envoi de l'email avec le template
            // emailService.sendNotification(
            //         event.getEmail(),
            //         EmailSender.NotificationType.CARD_ERROR,
            //         data);

            logger.info("Notification d'erreur de carte traitée");
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'erreur de carte: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "card.deleted.notification")
    public void handleCardDeleted(AccountEvent event) {
        try {
            // Récupération des détails de la carte avant suppression si possible
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
            
            
            // Préparation des données pour le template
            Map<String, Object> data = new HashMap<>();
            
            // Champs de l'événement (toujours disponibles)
            data.put("cardNumber", event.getCardNumber());
            data.put("cardType", event.getCardType());
            data.put("holderName", event.getHolderName());
            data.put("customerId", event.getCustomerId());
            data.put("deletionDate", LocalDateTime.now().toString());
            
            // Ajout des détails supplémentaires si disponibles
            if (cardDetails != null) {
                data.put("expiryDate", cardDetails.getExpiryDate());
                data.put("lastStatus", cardDetails.getStatus());
                data.put("creditLimit", cardDetails.getCreditLimit());
                data.put("availableCredit", cardDetails.getAvailableCredit());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
           
                // S'assurer que les champs essentiels sont bien renseignés
                if (event.getHolderName() == null && cardDetails.getHolderName() != null) {
                    data.put("holderName", cardDetails.getHolderName());
                }
            }
            
            // Message pour la base de données
            String message = String.format("Carte supprimée\n\nNuméro de carte : %s\nType de carte : %s\nTitulaire : %s\nDate de suppression : %s\nBanque : %s\n\nVos transactions restent disponibles sur votre compte Jamaa Bank.", 
                event.getCardNumber(),
                event.getCardType(),
                event.getHolderName(),
                LocalDateTime.now(),
                cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank"
            );
            
            // Création et sauvegarde de la notification
            Notification notification = new Notification();
            notification.setTitle("Carte supprimée");
            notification.setMessage(message);
            String userId = cardDetails.getCustomerId().toString();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.SUPPRESSION_CARTE);
            notification.setServiceEmetteur(Notification.ServiceEmetteur.CARD_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            notificationService.saveNotification(notification);             // Traiter la notification (sauvegarde + envoi si nécessaire)
            traiterNotification(notification);
            // Envoi de l'email avec le template
            // emailService.sendNotification(
            //         event.getEmail(),
            //         EmailSender.NotificationType.CARD_DELETED,
            //         data);

            logger.info("Notification de suppression de carte traitée pour la carte: {}", event.getCardNumber());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de suppression de carte: {}", e.getMessage());
        }
    }
    
    @RabbitListener(queues = "notification.recharge.done")
    public void handleRechargeDone(RechargeEvent event) {
        try {
            logger.info("=== Notification de recharge reçue ===");
            logger.info("Account ID: {}, Card ID: {}, Montant: {}", 
                      event.getAccountId(), event.getCardId(), event.getAmount());

             // Récupération des détails de la carte avant suppression si possible
            logger.debug("Récupération des informations de la carte {}", event.getCardId());
            CardDTO cardDetails = cardUtil.getCard(event.getCardId());
             

            // Préparer les données pour le template
            Map<String, Object> data = new HashMap<>();
            data.put("amount", event.getAmount());
            data.put("operationType", "recharge");
            data.put("status", event.getStatus());
            data.put("transactionDate", event.getCreatedAt());
            
            if (cardDetails != null) {
                data.put("cardLastFour", cardDetails.getCardNumber() != null && cardDetails.getCardNumber().length() > 4 ? 
                    cardDetails.getCardNumber().substring(cardDetails.getCardNumber().length() - 4) : "••••");
                data.put("cardType", cardDetails.getCardType());
                data.put("holderName", cardDetails.getHolderName());
                data.put("bankName", cardDetails.getBankName() != null ? cardDetails.getBankName() : "Jamaa Bank");
            }

            // Création de la notification
            Notification notification = new Notification();
            notification.setTitle("Confirmation de recharge");
            notification.setMessage(String.format("Recharge effectuée avec succès\n\nMontant : %s FCFA\nDate : %s\nStatut : Complété", event.getAmount(), event.getCreatedAt()));
            UserInfoResponse userInfo = accountUtil.getUserInfoByAccountId(event.getAccountId());
        notification.setUserId(userInfo.getUserId());
            notification.setType(NotificationType.CONFIRMATION_RECHARGE);
            notification.setServiceEmetteur(ServiceEmetteur.RECHARGE_SERVICE);
            notification.setCanal(Notification.CanalNotification.IN_APP);

            // Sauvegarder la notification
            notificationService.saveNotification(notification);
             // Traiter la notification (sauvegarde + envoi si nécessaire)
             traiterNotification(notification);
            // Envoyer l'email
            logger.info("Envoi de l'email de confirmation de recharge...");
            emailService.sendNotification(
                userInfo.getUserEmail(),
                EmailSender.NotificationType.RECHARGE,
                data
            );

            logger.info("Notification de recharge traitée pour le compte: {}", event.getAccountId());
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification de recharge: {}", e.getMessage(), e);
        }
    }

    // Ajout de deux nouveaux listeners pour les notifications supplémentaires
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
           data.put("accountNumber", event.getAccountNumber());
           data.put("registrationDate", LocalDate.now());
           // À remplacer par
          //data.put("verificationToken", event.getVerificationToken());
           data.put("verificationToken", "lXZxIoGzJvnl/Eh9AvMnkjptA3nIMSK6DmFJgWd3Pc8=");
           data.put("year", LocalDate.now().getYear());
           UserInfoResponse userInfo = accountUtil.getUserInfoByAccountNumber(event.getAccountNumber());
        
           // Création de la notification
           Notification notification = new Notification();
           notification.setTitle("Confirmation d'inscription");
           notification.setMessage(String.format(
            "Cher(e) client(e), votre compte a été créé avec succès. " +
            "Un email de confirmation a été envoyé à %s",
            userInfo.getUserEmail()
        ));
            
            notification.setUserId(userInfo.getUserId());
           notification.setType(NotificationType.CONFIRMATION_CREATION_COMPTE_JAMAA);
           notification.setServiceEmetteur(ServiceEmetteur.ACCOUNT);
           notification.setCanal(Notification.CanalNotification.IN_APP);
           

           notificationService.saveNotification(notification);
           // Traitement de la notification (sauvegarde + envoi si nécessaire)
           traiterNotification(notification);
           // Envoi de l'email avec le template
           EmailSender.NotificationType emailType = mapToEmailType(notification.getType());
                    
           // Envoyer l'email
           logger.info("Envoi de l'email...");
           emailService.sendNotification(
               event.getEmail(),
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
        notification.setTitle("Informations à corriger");
        notification.setMessage(message);
        notification.setType(NotificationType.ERREUR_CREATION_COMPTE);
        notification.setServiceEmetteur(ServiceEmetteur.ACCOUNT);

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