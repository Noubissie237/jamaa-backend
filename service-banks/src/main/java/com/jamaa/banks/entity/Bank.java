package com.jamaa.banks.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "banks")
@NoArgsConstructor
@Getter
@Setter
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String slogan;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "minimum_balance", nullable = false)
    private BigDecimal minimumBalance;

    @Column(name = "withdraw_fees", nullable = false)
    private BigDecimal withdrawFees;

    @Column(name = "internal_transfer_fees", nullable = false)
    private BigDecimal internalTransferFees;

    @Column(name = "external_transfer_fees", nullable = false)
    private BigDecimal externalTransferFees;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 