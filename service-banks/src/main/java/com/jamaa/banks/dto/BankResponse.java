package com.jamaa.banks.dto;

import com.jamaa.banks.model.enums.BankServiceType;
import com.jamaa.banks.model.enums.BankFeeType;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO pour la réponse contenant les informations d'une banque
 */
@Data
@Builder
public class BankResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String customerServicePhone;
    private String customerServiceEmail;
    private String ussdCode;
    private Set<BankServiceType> services;
    private Set<BankFeeResponse> fees;
    private Double minimumBalance;
    private String minimumBalanceDescription;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class BankFeeResponse {
        private Long id;
        private BankFeeType type;
        private Double amount;
        private String frequency;
        private String description;
    }
} 