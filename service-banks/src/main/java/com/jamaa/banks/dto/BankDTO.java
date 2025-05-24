package com.jamaa.banks.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BankDTO {
    private Long id;

    @NotBlank(message = "Le nom de la banque est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(max = 200, message = "Le slogan ne doit pas dépasser 200 caractères")
    private String slogan;

    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", 
             message = "L'URL du logo n'est pas valide")
    private String logoUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotNull(message = "Le solde minimum est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le solde minimum doit être positif")
    private BigDecimal minimumBalance;

    @NotNull(message = "Les frais de retrait sont obligatoires")
    @DecimalMin(value = "0.0", inclusive = true, message = "Les frais de retrait doivent être positifs")
    private BigDecimal withdrawFees;

    @NotNull(message = "Les frais de transfert interne sont obligatoires")
    @DecimalMin(value = "0.0", inclusive = true, message = "Les frais de transfert interne doivent être positifs")
    private BigDecimal internalTransferFees;

    @NotNull(message = "Les frais de transfert externe sont obligatoires")
    @DecimalMin(value = "0.0", inclusive = true, message = "Les frais de transfert externe doivent être positifs")
    private BigDecimal externalTransferFees;

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean isActive;
} 