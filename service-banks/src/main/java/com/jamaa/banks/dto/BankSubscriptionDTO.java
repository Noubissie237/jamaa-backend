package com.jamaa.banks.dto;

import com.jamaa.banks.entity.SubscriptionStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BankSubscriptionDTO {
    private Long id;

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    @Positive(message = "L'identifiant de l'utilisateur doit être positif")
    private Long userId;

    @NotNull(message = "L'identifiant de la banque est obligatoire")
    @Positive(message = "L'identifiant de la banque doit être positif")
    private Long bankId;

    @NotNull(message = "Le statut de la souscription est obligatoire")
    private SubscriptionStatus status;

    private LocalDateTime createdAt;
} 