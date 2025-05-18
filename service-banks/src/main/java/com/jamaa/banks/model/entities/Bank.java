package com.jamaa.banks.model.entities;

import com.jamaa.banks.model.enums.BankServiceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant une banque dans le système.
 * Cette classe est l'entité principale du service banks et contient toutes les informations
 * relatives à une banque, ses services et ses conditions.
 *
 * @see BankCondition
 * @see BankServiceType
 */
@Getter
@Setter
@ToString(exclude = "fees")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "banks", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "uk_bank_name")
})
public class Bank {
    /**
     * Identifiant unique de la banque.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom unique de la banque.
     * Ce champ est utilisé pour identifier la banque de manière unique dans le système.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description détaillée de la banque.
     * Stocké comme TEXT pour permettre des descriptions longues.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL vers le logo de la banque.
     */
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    /**
     * Numéro de téléphone du service client.
     * Format international recommandé.
     */
    @Column(name = "customer_service_phone", nullable = false, length = 15)
    private String customerServicePhone;

    /**
     * Email du service client.
     * Utilisé pour les communications officielles.
     */
    @Column(name = "customer_service_email", nullable = false, length = 255)
    private String customerServiceEmail;

    /**
     * Liste des services offerts par la banque.
     * Stockée comme une collection d'énumérations.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "bank_service_types",
        joinColumns = @JoinColumn(name = "bank_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 50)
    private Set<BankServiceType> services = new HashSet<>();

    /**
     * Liste des conditions bancaires associées à cette banque.
     * Relation bidirectionnelle avec cascade pour la gestion automatique des conditions.
     */


    /**
     * Liste des frais bancaires associés à cette banque.
     * Relation bidirectionnelle avec cascade pour la gestion automatique des frais.
     */
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BankFee> fees = new HashSet<>();

    /**
     * Solde minimum requis pour l'ouverture d'un compte.
     * La valeur par défaut est 0.0
     */
    @Column(name = "minimum_balance", nullable = false)
    private Double minimumBalance = 0.0;

    /**
     * Description du solde minimum requis pour l'ouverture d'un compte.
     */
    @Column(name = "minimum_balance_description", length = 500)
    private String minimumBalanceDescription;

    /**
     * Indique si la banque est active dans le système.
     * Une banque inactive ne peut pas accepter de nouvelles souscriptions.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Date et heure de création de l'enregistrement.
     * Automatiquement gérée par Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de la dernière mise à jour.
     * Automatiquement gérée par Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}