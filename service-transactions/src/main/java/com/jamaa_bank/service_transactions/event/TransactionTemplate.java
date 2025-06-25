package com.jamaa_bank.service_transactions.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jamaa_bank.service_transactions.model.TransactionStatus;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TransactionTemplate {
    @JsonProperty("idAccountSender")
    private Long idAccountSender;
    @JsonProperty("bankId")
    private Long bankId;
    @JsonProperty("idAccountReceiver")
    private Long idAccountReceiver;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("status")
    private TransactionStatus status;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}