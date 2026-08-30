package io.github.finalwave.network.sync;

public final class SyncFailReason {
    public static final String VALIDATION = "VALIDATION";
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String SERVER_ERROR = "SERVER_ERROR";
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    public static final String ALREADY_LOGGED_IN = "ALREADY_LOGGED_IN";

    private SyncFailReason() {
    }
}
