package com.jamaa_bank.service_transactions.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jamaa_bank.service_transactions.model.TransactionStatus;
import com.jamaa_bank.service_transactions.model.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionEvent {
    
    @JsonProperty("transactionType")
    private TransactionType transactionType;

    @JsonProperty("idAccountSender")
    private Long idAccountSender;

    @JsonProperty("idAccountReceiver")
    private Long idAccountReceiver;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("status")
    private TransactionStatus status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("dateEvent")
    private LocalDateTime dateEvent;

}
