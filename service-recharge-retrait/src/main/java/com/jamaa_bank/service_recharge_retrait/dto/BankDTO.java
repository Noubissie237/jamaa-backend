package com.jamaa_bank.service_recharge_retrait.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankDTO {
    private Long id;
    private String name;
    private String slogan;
    private String logoUrl;
    private String createdAt;
    private String updatedAt;
    private BigDecimal minimumBalance;
    private BigDecimal withdrawFees;
    private BigDecimal internalTransferFees;
    private BigDecimal externalTransferFees;
    private Boolean isActive;
}