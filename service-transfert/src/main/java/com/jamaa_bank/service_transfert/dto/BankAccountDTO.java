package com.jamaa_bank.service_transfert.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BankAccountDTO {
    private Long id;
    private Long bankId;
    private BigDecimal totalBalance;
    private BigDecimal totalInternalTransferFees;
    private BigDecimal totalExternalTransferFees;
}
