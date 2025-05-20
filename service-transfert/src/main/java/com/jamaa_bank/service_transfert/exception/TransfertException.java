package com.jamaa_bank.service_transfert.exception;

public class TransfertException extends RuntimeException {
    public TransfertException(String message) {
        super(message);
    }
    
    public TransfertException(String message, Throwable cause) {
        super(message, cause);
    }
}