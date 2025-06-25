package com.jamaa.service_notifications.exception;

public class TransferServiceException extends Exception {
    public TransferServiceException(String message) {
        super(message);
    }
    
    public TransferServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}