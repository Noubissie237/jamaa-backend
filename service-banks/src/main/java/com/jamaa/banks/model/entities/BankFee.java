package com.jamaa.banks.model.entities;

import com.jamaa.banks.model.enums.BankFeeType;
import com.jamaa.banks.model.enums.BankFeeFrequency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Entité représentant un frais bancaire
 */
@Getter
@Setter
@ToString(exclude = "bank")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_fees")
public class BankFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankFeeType type;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankFeeFrequency frequency;

    @Column(columnDefinition = "TEXT")
    private String description;
} 