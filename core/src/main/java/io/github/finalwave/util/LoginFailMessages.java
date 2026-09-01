package io.github.finalwave.util;

import io.github.finalwave.network.auth.LoginFailReason;

public final class LoginFailMessages {
    private LoginFailMessages() {
    }

    public static String messageFor(String reason) {
        if (reason == null) {
            return "Login failed.";
        }
        return switch (reason) {
            case LoginFailReason.BAD_CREDENTIALS -> "Wrong Username or Password has been given.";
            case LoginFailReason.ALREADY_LOGGED_IN -> "This account is already logged in elsewhere.";
            case LoginFailReason.INVALID_INPUT -> "Please enter your username and password.";
            case LoginFailReason.NOT_CONNECTED -> "Cannot reach the server. Start the server and try again.";
            default -> "Login failed. Please try again.";
        };
    }
}
