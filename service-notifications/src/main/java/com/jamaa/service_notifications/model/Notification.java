package com.jamaa.service_notifications.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String email;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private ServiceEmetteur serviceEmetteur;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public ServiceEmetteur getServiceEmetteur() {
        return serviceEmetteur;
    }

    public void setServiceEmetteur(ServiceEmetteur serviceEmetteur) {
        this.serviceEmetteur = serviceEmetteur;
    }

    @PrePersist
    public void prePersist() {
        this.dateEnvoi = LocalDateTime.now();
        this.lu = false;
    }

    public enum NotificationType {
        CONFIRMATION_DEPOT,
        CONFIRMATION_RETRAIT,
        CONFIRMATION_TRANSFERT,
        CONFIRMATION_INSCRIPTION,
        MOT_DE_PASSE_REINITIALISE,
        CONFIRMATION_SOUSCRIPTION_BANQUE,
        TRANSACTION_REUSSIE,
        AUTHENTIFICATION
    }

    public enum ServiceEmetteur {
        DEPOSIT_SERVICE,
        WITHDRAWAL_SERVICE,
        TRANSFER_SERVICE,
        AUTH_SERVICE,
        BANK_SERVICE,
        TRANSACTION_SERVICE
    }
} 
