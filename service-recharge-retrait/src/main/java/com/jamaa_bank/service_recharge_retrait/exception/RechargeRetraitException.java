package com.jamaa_bank.service_recharge_retrait.exception;

public class RechargeRetraitException extends RuntimeException {
    public RechargeRetraitException(String message) {
        super(message);
    }
    
    public RechargeRetraitException(String message, Throwable cause) {
        super(message, cause);
    }
}
