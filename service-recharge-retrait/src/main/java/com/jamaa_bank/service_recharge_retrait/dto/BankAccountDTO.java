package com.jamaa_bank.service_recharge_retrait.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountDTO {
    private Long id;
    private BigDecimal totalBalance;
    private Long bankId;
}
