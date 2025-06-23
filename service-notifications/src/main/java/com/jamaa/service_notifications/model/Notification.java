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
    // private String email;
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
        // Notifications générales
        CONFIRMATION_DEPOT,
        CONFIRMATION_RETRAIT,
        CONFIRMATION_TRANSFERT,
        CONFIRMATION_INSCRIPTION,
        MOT_DE_PASSE_REINITIALISE,
        CONFIRMATION_CREATION_COMPTE_JAMAA,
        CONFIRMATION_RECHARGE,
        RECHARGE,
        SUPPRESSION_COMPTE,
        ACCOUNT_DELETION,
        ERREUR_CREATION_COMPTE,
        ACCOUNT_CREATION_ERROR,
        
        // Notifications pour les cartes
        CONFIRMATION_CREATION_CARTE,
        MISE_A_JOUR_CARTE,
        ACTIVATION_CARTE,
        BLOCAGE_CARTE,
        SUPPRESSION_CARTE,
        ERREUR_CARTE,
    }

    public enum ServiceEmetteur {
        WITHDRAWAL_SERVICE,
        TRANSFER_SERVICE,
        BANK_SERVICE,
        RECHARGE_SERVICE,
        CARD_SERVICE,
        ACCOUNT
    }
    
    public enum CanalNotification {
        IN_APP,  // Notification dans l'application
        EMAIL    // Notification par email
    }
} 
