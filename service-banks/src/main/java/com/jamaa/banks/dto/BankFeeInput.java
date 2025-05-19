package com.jamaa.banks.dto;

import com.jamaa.banks.model.enums.BankFeeType;
import com.jamaa.banks.model.enums.BankFeeFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO pour la création ou la mise à jour d'un frais bancaire
 */
@Data
public class BankFeeInput {
    @NotNull(message = "Le type de frais est obligatoire")
    private BankFeeType type;

    @NotNull(message = "Le montant est obligatoire")
    @PositiveOrZero(message = "Le montant doit être positif ou zéro")
    private Double amount;

    @NotNull(message = "La fréquence est obligatoire")
    private BankFeeFrequency frequency;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;
} 