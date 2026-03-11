package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CardMaskUtil {
    private static final int MASK_LENGTH = 4;
    private static final int GROUP_SIZE = 4;

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < MASK_LENGTH) {
            return "****";
        }

        String lastFour = cardNumber.substring(cardNumber.length() - MASK_LENGTH);
        StringBuilder masked = new StringBuilder();

        int totalGroups = cardNumber.length() / GROUP_SIZE;

        for (int i = 0; i < totalGroups - 1; i++) {
            masked.append("****");
            if (i < totalGroups - 2) {
                masked.append(" ");
            }
        }

        masked.append(" ").append(lastFour);
        return masked.toString();
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber.isBlank()) {
            return false;
        }
        for (char c : cardNumber.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
