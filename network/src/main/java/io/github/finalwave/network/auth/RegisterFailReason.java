package io.github.finalwave.network.auth;

public final class RegisterFailReason {
    public static final String USERNAME_TAKEN = "USERNAME_TAKEN";
    public static final String EMAIL_TAKEN = "EMAIL_TAKEN";
    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String WEAK_PASSWORD = "WEAK_PASSWORD";
    public static final String INVALID_USERNAME = "INVALID_USERNAME";
    public static final String INVALID_EMAIL = "INVALID_EMAIL";
    public static final String INVALID_NICKNAME = "INVALID_NICKNAME";
    public static final String INVALID_GENDER = "INVALID_GENDER";
    public static final String INVALID_SECURITY_QUESTION = "INVALID_SECURITY_QUESTION";
    public static final String MISSING_SECURITY_ANSWER = "MISSING_SECURITY_ANSWER";
    public static final String NOT_CONNECTED = "NOT_CONNECTED";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private RegisterFailReason() {
    }
}
