package io.github.finalwave.server.db;

public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:sqlite:users.db";
    private static final String PROPERTY = "pvz.database.url";
    private static final String ENV = "PVZ_DATABASE_URL";

    private DatabaseConfig() {
    }

    public static void apply() {
        if (System.getProperty(PROPERTY) != null) {
            return;
        }
        String env = System.getenv(ENV);
        if (env != null && !env.isBlank()) {
            System.setProperty(PROPERTY, env.trim());
            return;
        }
        System.setProperty(PROPERTY, DEFAULT_URL);
    }

    public static String resolvedUrl() {
        return System.getProperty(PROPERTY, DEFAULT_URL);
    }
}
