package com.jamaa.banks.model.enums;

/**
 * Types de services bancaires disponibles
 */
public enum BankServiceType {
    VIRTUAL_CARD("Carte virtuelle"),
    CASH_DEPOSIT("Dépôt d'argent"),
    CASH_WITHDRAWAL("Retrait d'argent"),
    MONEY_TRANSFER("Transfert d'argent");
    
    private final String description;

    BankServiceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 