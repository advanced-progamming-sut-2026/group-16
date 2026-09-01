package io.github.finalwave.util;

import java.util.regex.Pattern;

public final class RegistrationValidator {
    public static final int MAX_USERNAME_LENGTH = 32;
    public static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!#$%^&*()=+}{\\[\\]|/\\\\:;'\",<>?]).{8,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]+(?:[._-][A-Za-z0-9]+)*@[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*"
                    + "(?:\\.[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)*\\.[A-Za-z]{2,}$");

    private RegistrationValidator() {
    }

    public static boolean isValidUsername(String username) {
        return username != null
                && username.length() <= MAX_USERNAME_LENGTH
                && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isStrongPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null
                && email.length() <= MAX_EMAIL_LENGTH
                && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidNickname(String nickname) {
        return nickname != null && nickname.length() >= 3 && nickname.length() <= 30;
    }
}
