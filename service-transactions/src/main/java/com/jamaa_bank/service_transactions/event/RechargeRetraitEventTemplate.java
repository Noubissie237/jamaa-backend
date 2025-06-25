package com.jamaa_bank.service_transactions.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RechargeRetraitEventTemplate {
    private Long bankId;
    private Long accountId;
    private Long cardId;
    private BigDecimal amount;
    private String operationType; // RECHARGE ou RETRAIT
    private String status;
    private LocalDateTime createdAt;
}
