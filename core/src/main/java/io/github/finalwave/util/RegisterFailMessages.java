package io.github.finalwave.util;

import io.github.finalwave.network.auth.RegisterFailReason;

public final class RegisterFailMessages {
    private RegisterFailMessages() {
    }

    public static String messageFor(String reason) {
        if (reason == null) {
            return "Registration failed.";
        }
        return switch (reason) {
            case RegisterFailReason.USERNAME_TAKEN -> "This username is already taken. Please use another one.";
            case RegisterFailReason.EMAIL_TAKEN -> "This email is already registered.";
            case RegisterFailReason.WEAK_PASSWORD ->
                    "Password must be at least 8 characters and include lower, upper, digit, and special characters.";
            case RegisterFailReason.INVALID_USERNAME ->
                    "Username may only contain letters, numbers, and hyphens (max 32 characters).";
            case RegisterFailReason.INVALID_EMAIL -> "This email is not a correct one.";
            case RegisterFailReason.INVALID_NICKNAME -> "Nickname must be between 3 and 30 characters.";
            case RegisterFailReason.INVALID_GENDER -> "Invalid gender.";
            case RegisterFailReason.INVALID_SECURITY_QUESTION -> "Invalid security question number.";
            case RegisterFailReason.MISSING_SECURITY_ANSWER -> "Security answer is required.";
            case RegisterFailReason.NOT_CONNECTED -> "Cannot reach the server. Start the server and try again.";
            case RegisterFailReason.INVALID_INPUT -> "Please fill in all required fields.";
            default -> "Registration failed. Please try again.";
        };
    }
}
