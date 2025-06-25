package com.jamaa.service_notifications.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
@Getter
@Setter
public class Notification implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String email;
    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private ServiceEmetteur serviceEmetteur;
    
    @Column(nullable = false)
    private boolean lu = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotification canal = CanalNotification.EMAIL; // Par défaut, on envoie par email

    @PrePersist
    public void prePersist() {
        this.dateEnvoi = LocalDateTime.now();
    }

    public enum NotificationType {
        CONFIRMATION_DEPOT,
        CONFIRMATION_RETRAIT,
        CONFIRMATION_TRANSFERT,
        CONFIRMATION_INSCRIPTION,
        MOT_DE_PASSE_REINITIALISE,
        CONFIRMATION_SOUSCRIPTION_BANQUE,
        TRANSACTION_REUSSIE,
        AUTHENTIFICATION,
        ALERTE_SOLDE,
        CARD_CREATE,
        CONFIRMATION_RECHARGE,
        RECHARGE,
        SUPPRESSION_COMPTE,
        ACCOUNT_DELETION,
        ERREUR_CREATION_COMPTE,
        ACCOUNT_CREATION_ERROR,
    }

    public enum ServiceEmetteur {
        DEPOSIT_SERVICE,
        WITHDRAWAL_SERVICE,
        TRANSFER_SERVICE,
        AUTH_SERVICE,
        BANK_SERVICE,
        TRANSACTION_SERVICE,
        RECHARGE_SERVICE
        
    }
    
    public enum CanalNotification {
        IN_APP,  // Notification dans l'application
        EMAIL    // Notification par email
    }
} 
