package com.jamaa_bank.service_transfert.events;

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
    @JsonProperty("senderAccountId")
    private Long senderAccountId;

    @JsonProperty("receiverAccountId")
    private Long receiverAccountId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("createAt")
    private LocalDateTime createAt;
}
