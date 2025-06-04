package com.jmaaa_bank.service_card.dto;

import java.math.BigDecimal;

import com.jmaaa_bank.service_card.enums.CardType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardCreateRequest {


    @NotBlank(message = "le nom du porteur est requis")
    private String holderName;

    @NotNull(message = "L'ID du client est requis")
    private Long customerId;
    
    @NotNull(message = "Le type de carte est requis")
    private CardType cardType;

    private BigDecimal creditLimit;
    
    private String pin;



    
}
