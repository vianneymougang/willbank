package com.willbank.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for generating cryptographically secure passwords
 * 
 * Generated passwords follow these rules:
 * - Minimum 12 characters
 * - At least 2 uppercase letters
 * - At least 2 lowercase letters
 * - At least 2 digits
 * - At least 2 special characters
 * - No ambiguous characters (0, O, l, I, 1)
 * 
 * Example output: "Kp8#mLx2Qw!n"
 */
@Service
@Slf4j
public class PasswordGenerationService {

    // Character sets (excluding ambiguous chars)
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // No O, I
    private static final String LOWERCASE = "abcdefghjkmnpqrstuvwxyz"; // No l, i
    private static final String DIGITS = "23456789"; // No 0, 1
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?"; // Common special chars
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private static final int MIN_LENGTH = 12;
    private static final int MIN_EACH_TYPE = 2;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a secure random password
     */
    public String generateSecurePassword() {
        List<Character> passwordChars = new ArrayList<>();

        // Ensure minimum of each character type
        passwordChars.addAll(getRandomChars(UPPERCASE, MIN_EACH_TYPE));
        passwordChars.addAll(getRandomChars(LOWERCASE, MIN_EACH_TYPE));
        passwordChars.addAll(getRandomChars(DIGITS, MIN_EACH_TYPE));
        passwordChars.addAll(getRandomChars(SPECIAL, MIN_EACH_TYPE));

        // Fill remaining with random characters
        int remaining = MIN_LENGTH - passwordChars.size();
        if (remaining > 0) {
            passwordChars.addAll(getRandomChars(ALL_CHARS, remaining));
        }

        // Shuffle to randomize positions
        Collections.shuffle(passwordChars, secureRandom);

        // Convert to string
        StringBuilder password = new StringBuilder(passwordChars.size());
        for (Character c : passwordChars) {
            password.append(c);
        }

        String result = password.toString();
        log.debug("Generated secure password with length: {}", result.length());

        return result;
    }

    /**
     * Get random characters from a character set
     */
    private List<Character> getRandomChars(String charSet, int count) {
        List<Character> chars = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int randomIndex = secureRandom.nextInt(charSet.length());
            chars.add(charSet.charAt(randomIndex));
        }
        return chars;
    }

    /**
     * Validate password strength (for user-provided passwords)
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }

        int uppercase = 0, lowercase = 0, digits = 0, special = 0;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                uppercase++;
            else if (Character.isLowerCase(c))
                lowercase++;
            else if (Character.isDigit(c))
                digits++;
            else
                special++;
        }

        return uppercase >= MIN_EACH_TYPE
                && lowercase >= MIN_EACH_TYPE
                && digits >= MIN_EACH_TYPE
                && special >= MIN_EACH_TYPE;
    }
}
