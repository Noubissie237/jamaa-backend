package com.jamaa_bank.service_recharge_retrait.exception;

public class AccountServiceException extends RuntimeException {
    public AccountServiceException(String message) {
        super(message);
    }
    
    public AccountServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
