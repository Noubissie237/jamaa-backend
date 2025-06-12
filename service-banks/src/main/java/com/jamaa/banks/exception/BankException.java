package com.jamaa.banks.exception;

public class BankException extends RuntimeException {
    public BankException(String message) {
        super(message);
    }

    public static BankException alreadySubscribed(Long userId, Long bankId) {
        return new BankException(
            String.format("L'utilisateur %d est déjà souscrit à la banque %d", userId, bankId)
        );
    }

    public static BankException subscriptionNotFound(Long subscriptionId) {
        return new BankException(
            String.format("Souscription non trouvée avec l'id: %d", subscriptionId)
        );
    }

    public static BankException bankInactive(Long bankId) {
        return new BankException(
            String.format("La banque %d est inactive", bankId)
        );
    }
} 