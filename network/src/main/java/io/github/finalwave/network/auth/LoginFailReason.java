package io.github.finalwave.network.auth;

public final class LoginFailReason {
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    public static final String ALREADY_LOGGED_IN = "ALREADY_LOGGED_IN";
    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String NOT_CONNECTED = "NOT_CONNECTED";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private LoginFailReason() {
    }
}
