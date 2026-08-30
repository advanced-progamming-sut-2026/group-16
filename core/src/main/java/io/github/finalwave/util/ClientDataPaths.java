package io.github.finalwave.util;

import java.nio.file.Path;

public final class ClientDataPaths {
    private static final String DATABASE_URL_PROPERTY = "pvz.database.url";
    private static final String SESSION_FILE_PROPERTY = "pvz.session.file";
    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:users.db";

    private ClientDataPaths() {
    }

    public static Path sessionFile() {
        String explicit = System.getProperty(SESSION_FILE_PROPERTY);
        if (explicit != null && !explicit.isBlank()) {
            return Path.of(explicit.trim());
        }
        String databaseUrl = System.getProperty(DATABASE_URL_PROPERTY, DEFAULT_DATABASE_URL);
        if (databaseUrl.startsWith("jdbc:sqlite:")) {
            String filePath = databaseUrl.substring("jdbc:sqlite:".length());
            if (filePath.endsWith(".db")) {
                return Path.of(filePath.substring(0, filePath.length() - 3) + ".session");
            }
            return Path.of(filePath + ".session");
        }
        return Path.of("user.session");
    }
}
