package com.jamaa.banks.model.enums;

/**
 * Types de frais bancaires
 */
public enum BankFeeType {
    MAINTENANCE_FEE("Frais de maintenance"),
    ACCOUNT_OPENING_FEE("Frais d'ouverture de compte"),
    VIRTUAL_CARD_FEE("Frais de carte virtuelle"),
    WITHDRAWAL_FEE("Frais de retrait"),
    INTERNAL_TRANSFER_FEE("Frais de transfert interne"),
    EXTERNAL_TRANSFER_FEE("Frais de transfert externe");

    private final String description;

    BankFeeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 