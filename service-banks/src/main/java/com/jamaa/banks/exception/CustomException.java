package com.jamaa.banks.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }

    public static CustomException notFound(String entity, Long id) {
        return new CustomException(String.format("%s non trouvé avec l'id: %d", entity, id));
    }

    public static CustomException alreadyExists(String entity, String field, String value) {
        return new CustomException(String.format("%s avec %s '%s' existe déjà", entity, field, value));
    }

    public static CustomException invalidOperation(String message) {
        return new CustomException(message);
    }
} 