package com.jamaa.service_notifications.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountDTO {
    private Long id;
    private String userId;
    private String userEmail;
    private BigDecimal balance;
}
