package com.jmaaa_bank.service_card.utils;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.jmaaa_bank.service_card.enums.CardType;

@Component
public class CardNumberGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generateCardNumber(CardType cardType) {
        String prefix = getCardPrefix(cardType);
        StringBuilder cardNumber = new StringBuilder(prefix);
        
        // Générer les chiffres restants
        int remainingDigits = 16 - prefix.length() - 1; // -1 pour le chiffre de contrôle Luhn
        
        for (int i = 0; i < remainingDigits; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        // Ajouter le chiffre de contrôle Luhn
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }
    
    public String generateCVV() {
        return String.format("%03d", random.nextInt(1000));
    }
    
    private String getCardPrefix(CardType cardType) {
        return switch (cardType) {
            case VISA -> "4";
            case MASTERCARD -> "5" + (1 + random.nextInt(5)); // 51-55
            case AMERICAN_EXPRESS -> "3" + (4 + random.nextInt(3)); // 34 ou 37
        };
    }
    
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }
    
}
