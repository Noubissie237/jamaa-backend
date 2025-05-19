package com.jamaa.banks.model.enums;

/**
 * Types de fréquence pour les frais bancaires
 */
public enum BankFeeFrequency {
    ONCE("Une fois", "Frais appliqué une seule fois"),
    MONTHLY("Mensuel", "Frais appliqué chaque mois"),
    YEARLY("Annuel", "Frais appliqué chaque année"),
    PER_TRANSACTION("Par transaction", "Frais appliqué à chaque transaction");

    private final String label;
    private final String description;

    BankFeeFrequency(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
} 