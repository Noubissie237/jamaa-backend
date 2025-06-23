package com.jamaa_bank.service_recharge_retrait.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CardDTO {
    private Long id;
    private BigDecimal currentBalance;
    private Long bankId;
    private String bankName;
}
