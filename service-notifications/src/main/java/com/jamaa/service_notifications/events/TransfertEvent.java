package com.jamaa.service_notifications.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransfertEvent {
    @JsonProperty("idAccountSender")
    private Long idAccountSender;

    @JsonProperty("idBankSender")
    private Long idBankSender;

    @JsonProperty("idAccountReceiver")
    private Long idAccountReceiver;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("status")
    private TransactionStatus status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
