package com.jamaa.service_notifications.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@NoArgsConstructor
@ToString
public class AccountEvent extends BaseEvent {
    
   
    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("holderName")
    private String holderName;
    @JsonProperty("customerId")
    private Long customerId;
    @JsonProperty("cardType")
    private String cardType;
    @JsonProperty("expiryDate")
    private String expiryDate; // Format MM/YY
    @JsonProperty("cvv")
    private String cvv;
    @JsonProperty("creditLimit")
    private BigDecimal creditLimit;
}
