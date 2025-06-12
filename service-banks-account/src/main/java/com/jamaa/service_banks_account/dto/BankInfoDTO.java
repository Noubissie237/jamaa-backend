package com.jamaa.service_banks_account.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankInfoDTO {
    private Long id;
    private String name;
    private BigDecimal minimumBalance;
    private BigDecimal withdrawFees;
    private BigDecimal internalTransferFees;
    private BigDecimal externalTransferFees;
}