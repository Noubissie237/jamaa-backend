package com.jamaa_bank.service_transfert.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BankDTO {
    private Long id;
    private String name;
    private BigDecimal internalTransferFees;
    private BigDecimal externalTransferFees;
}
