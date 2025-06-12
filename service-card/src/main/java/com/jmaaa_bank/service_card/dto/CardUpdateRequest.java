package com.jmaaa_bank.service_card.dto;

import java.math.BigDecimal;

import com.jmaaa_bank.service_card.enums.CardStatus;

import lombok.Data;

@Data
public class CardUpdateRequest {

    private CardStatus status;
    private BigDecimal creditLimit;
    private String pin;
    
}
