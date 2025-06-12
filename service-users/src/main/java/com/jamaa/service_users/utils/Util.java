package com.jamaa.service_users.utils;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.stereotype.Component;


@Component
public class Util {

    private final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final int CODE_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public String generateRandomPassword() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString(); 
    }

    public String generateUsername(String firstName, String lastName) {
        String base = (firstName.charAt(0) + lastName).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        int randomNumber = new SecureRandom().nextInt(1000);
        return base + randomNumber;
    }

}

