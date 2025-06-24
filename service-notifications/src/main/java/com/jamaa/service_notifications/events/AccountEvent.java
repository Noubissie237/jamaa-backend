package com.jamaa.service_notifications.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    
    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("cardId")
    private Long cardId;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("holderName")
    private String holderName;
    
    @JsonProperty("cardType")
    private String cardType;

    @JsonProperty("customerEmail")
    private String customerEmail;
    
    @JsonProperty("creditLimit")
    private BigDecimal creditLimit;
    
    @JsonProperty("expiryDate")
    private LocalDate expiryDate;
    
    // Champs spécifiques aux événements de carte

    // Méthode utilitaire pour extraire les 4 derniers chiffres
    public String getLastFourDigits() {
        return cardNumber != null && cardNumber.length() > 4 
            ? cardNumber.substring(cardNumber.length() - 4)
            : cardNumber;
    }
}
