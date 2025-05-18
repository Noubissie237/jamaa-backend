package com.jamaa.banks.dto;


import com.jamaa.banks.model.enums.BankServiceType;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Data
public class UpdateBankInput {
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    private String logoUrl;

    @Size(min = 8, max = 15, message = "Le numéro de téléphone doit contenir entre 8 et 15 chiffres")
    private String customerServicePhone;

    @Size(min = 5, max = 255, message = "L'email doit contenir entre 5 et 255 caractères")
    private String customerServiceEmail;

    private Double minimumBalance;

    @Size(max = 500, message = "La description du solde minimum ne doit pas dépasser 500 caractères")
    private String minimumBalanceDescription;

    private Boolean active;

    private Set<BankServiceType> services;

    private List<BankFeeInput> fees;
} 