package com.jamaa.service_notifications.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CardDTO {
    private Long id;
    private BigDecimal currentBalance;
    private Long bankId;
    private String bankName;
    private String cardNumber;
    private String cardType;
    private String holderName;
    private String expiryDate;
    private String status;
    private BigDecimal creditLimit;
    private BigDecimal usedCredit;
    private BigDecimal availableCredit;
    private Long customerId;
    private Boolean isVirtual;
    private String cvv;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    

}
