package com.jamaa.service_notifications.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransfertEvent {
    @JsonProperty("idAccountSender")
    private Long idAccountSender;

    @JsonProperty("idAccountReceiver")
    private Long idAccountReceiver;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}