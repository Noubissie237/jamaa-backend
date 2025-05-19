package com.jamaa.banks.dto;

import com.jamaa.banks.model.enums.BankServiceType;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

/**
 * DTO pour la création d'une nouvelle banque
 */
@Data
public class CreateBankInput {
    /**
     * Nom de la banque.
     * Doit être unique dans le système.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    /**
     * Description détaillée de la banque.
     * Peut inclure des informations sur l'histoire, les valeurs et la mission de la banque.
     */
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    /**
     * URL du logo de la banque.
     * Doit être une URL valide pointant vers une image.
     */
    @NotBlank(message = "L'URL du logo est obligatoire")
    @Size(min = 5, max = 255, message = "L'URL du logo doit contenir entre 5 et 255 caractères")
    private String logoUrl;

    /**
     * Numéro de téléphone du service client.
     * Format international recommandé.
     */
    @NotBlank(message = "Le numéro de téléphone du service client est obligatoire")
    @Size(min = 8, max = 15, message = "Le numéro de téléphone doit contenir entre 8 et 15 chiffres")
    private String customerServicePhone;

    /**
     * Adresse email du service client.
     * Utilisée pour les communications officielles.
     */
    @NotBlank(message = "L'email du service client est obligatoire")
    @Size(min = 5, max = 255, message = "L'email du service client doit contenir entre 5 et 255 caractères")
    private String customerServiceEmail;

    /**
     * Solde minimum requis pour l'ouverture d'un compte.
     * La valeur par défaut est 0.0
     */
    @NotNull(message = "Le solde minimum est obligatoire")
    private Double minimumBalance;

    @Size(max = 500, message = "La description du solde minimum ne doit pas dépasser 500 caractères")
    private String minimumBalanceDescription;

    /**
     * Liste des services proposés par la banque.
     * Au moins un service doit être sélectionné.
     */
    @NotNull(message = "La liste des services est obligatoire")
    @Size(min = 1, message = "Au moins un service doit être spécifié")
    private Set<BankServiceType> services;

    private List<BankFeeInput> fees;
} 