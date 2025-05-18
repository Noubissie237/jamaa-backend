package com.jamaa.banks.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.enums.BankServiceType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_service_details")
public class BankService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankServiceType serviceType;

    @Column(length = 1000)
    private String description;

    @Column
    private String serviceCode;  // Code spécifique pour le service (ex: code USSD)

    @Column
    private boolean available = true;

    @Column
    private String additionalInfo;  // Informations supplémentaires sur le service
} 