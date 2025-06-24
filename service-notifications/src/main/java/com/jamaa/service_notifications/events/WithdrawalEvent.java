package com.jamaa.service_notifications.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.actuate.endpoint.OperationType;

import lombok.NoArgsConstructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawalEvent {
    private Long accountId;
    private Long cardId;
    private BigDecimal amount;
    private OperationType operationType;
    private String status;
    private LocalDateTime createdAt;
} 