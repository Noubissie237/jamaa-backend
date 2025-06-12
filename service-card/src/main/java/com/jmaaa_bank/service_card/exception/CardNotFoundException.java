package com.jmaaa_bank.service_card.exception;

public class CardNotFoundException  extends RuntimeException{
     public CardNotFoundException(String message) {
        super(message);
    }
    
    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
