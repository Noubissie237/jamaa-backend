package com.jamaa.banks.dto;



import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BankSubscriptionInputDTO {
    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    @Positive(message = "L'identifiant de l'utilisateur doit être positif")
    private Long userId;

    @NotNull(message = "L'identifiant de la banque est obligatoire")
    @Positive(message = "L'identifiant de la banque doit être positif")
    private Long bankId;
}
