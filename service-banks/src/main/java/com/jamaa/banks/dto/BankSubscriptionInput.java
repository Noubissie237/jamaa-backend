package com.jamaa.banks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BankSubscriptionInput {
    @NotNull(message = "L'ID de l'utilisateur est requis")
    private Long userId;

    @NotNull(message = "L'ID de la banque est requis")
    private Long bankId;
} 