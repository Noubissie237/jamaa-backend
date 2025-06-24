package com.jamaa_bank.service_recharge_retrait.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jamaa_bank.service_recharge_retrait.model.OperationType;
import com.jamaa_bank.service_recharge_retrait.model.TransactionStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RechargeRetraitEvent {
    private Long bankId;
    private Long accountId;
    private Long cardId;
    private BigDecimal amount;
    private OperationType operationType;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
