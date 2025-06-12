package com.jamaa.service_banks_account.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bank_accounts")
@NoArgsConstructor
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_id", nullable = false, unique = true)
    private Long bankId;

    @Column(name = "total_balance", nullable = false)
    private BigDecimal totalBalance;

    @Column(name = "total_withdraw_fees", nullable = false)
    private BigDecimal totalWithdrawFees;

    @Column(name = "total_internal_transfer_fees", nullable = false)
    private BigDecimal totalInternalTransferFees;

    @Column(name = "total_external_transfer_fees", nullable = false)
    private BigDecimal totalExternalTransferFees;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        totalBalance = BigDecimal.ZERO;
        totalWithdrawFees = BigDecimal.ZERO;
        totalInternalTransferFees = BigDecimal.ZERO;
        totalExternalTransferFees = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}