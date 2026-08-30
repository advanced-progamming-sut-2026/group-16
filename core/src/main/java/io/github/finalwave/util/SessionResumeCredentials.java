package io.github.finalwave.util;

public final class SessionResumeCredentials {
    private static volatile String username;
    private static volatile String passwordHash;

    private SessionResumeCredentials() {
    }

    public static void remember(String user, String hash) {
        username = user;
        passwordHash = hash;
    }

    public static String username() {
        return username;
    }

    public static String passwordHash() {
        return passwordHash;
    }

    public static void clear() {
        username = null;
        passwordHash = null;
    }
}
