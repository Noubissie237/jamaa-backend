package com.jmaaa_bank.service_card.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jmaaa_bank.service_card.enums.CardStatus;
import com.jmaaa_bank.service_card.enums.CardType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class CardResponse {
    
    private Long id;
    private String cardNumber;
    private String holderName;
    private Long customerId;
    private CardType cardType;
    private CardStatus status;
    private String expiryDate;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private Boolean isVirtual;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private Long bankId;
    private String bankName;

    public String getMaskedCardNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return "****";
    }

}
