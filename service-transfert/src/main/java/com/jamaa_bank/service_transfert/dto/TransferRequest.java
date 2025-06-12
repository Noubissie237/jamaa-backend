package com.jamaa_bank.service_transfert.dto;

import java.math.BigDecimal;
import com.jamaa_bank.service_transfert.model.AccountType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private Long fromAccountId;
    private AccountType fromAccountType;
    private Long toAccountId;
    private AccountType toAccountType;
    private BigDecimal amount;
} 