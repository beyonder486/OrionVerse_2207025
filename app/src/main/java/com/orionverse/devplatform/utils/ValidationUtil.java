package com.orionverse.devplatform.utils;

import android.util.Patterns;

public class ValidationUtil {
    
    // Validate email format
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && 
               Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validate password strength (minimum 6 characters)
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Validate username (3-20 characters, alphanumeric and underscore)
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    // Validate non-empty string
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    // Get password strength message
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (password.length() < 8) {
            return "Weak password";
        }
        if (password.matches(".*[A-Z].*") && password.matches(".*[0-9].*")) {
            return "Strong password";
        }
        return "Medium password";
    }
}
