package com.jmaaa_bank.service_card.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jmaaa_bank.service_card.enums.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "le numéro de carte ne peut pas etre vide")
    private String cardNumber;


    @NotBlank(message = "le nom du porteur  ne peut pas etre vide")
    private String holderName;

    @NotNull(message = "l'ID du client ne peut pas etre vide")
    private Long customerId;


    @Enumerated(EnumType.STRING)
    @NotNull(message = "le type de carte ne peut pas etre null")
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CardStatus status = CardStatus.PENDING_ACTIVATION;


    @Column(nullable = false)
    private String expiryDate; // Foramt MM/YY

    @Column(nullable = false)
    private String cvv;

    @Column(precision = 15 , scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    private String pin; // Hashé

    @Builder.Default
    private Boolean isVirtual = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastUsedAt;




    
}
