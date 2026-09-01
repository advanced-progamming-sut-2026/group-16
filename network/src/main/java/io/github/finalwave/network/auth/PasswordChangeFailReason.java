package io.github.finalwave.network.auth;

public final class PasswordChangeFailReason {
    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    public static final String WRONG_SECURITY_ANSWER = "WRONG_SECURITY_ANSWER";
    public static final String SAME_PASSWORD = "SAME_PASSWORD";
    public static final String WEAK_PASSWORD = "WEAK_PASSWORD";
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String NOT_CONNECTED = "NOT_CONNECTED";
    public static final String SERVER_ERROR = "SERVER_ERROR";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    private PasswordChangeFailReason() {
    }
}
