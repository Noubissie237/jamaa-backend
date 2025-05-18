package com.jamaa.banks.model.enums;

/**
 * Énumération des différents statuts possibles pour une souscription bancaire.
 */
public enum SubscriptionStatus {
    PENDING("En attente", "Souscription en attente de validation"),
    APPROVED("Approuvée", "Souscription approuvée mais pas encore active"),
    ACTIVE("Active", "Souscription active et utilisable"),
    REJECTED("Rejetée", "Souscription rejetée par la banque"),
    CLOSED("Clôturée", "Souscription clôturée par l'utilisateur ou la banque");

    private final String label;
    private final String description;

    SubscriptionStatus(String label, String description) {
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