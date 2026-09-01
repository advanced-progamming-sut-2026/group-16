package io.github.finalwave.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StayLoggedInStorage {

    private StayLoggedInStorage() {
    }

    public record Session(String username, String passwordHash) {
    }

    public static void saveSession(String username, String passwordHash) {
        try {
            Files.writeString(sessionFile(), username + "\n" + passwordHash);
        } catch (IOException e) {
            throw new RuntimeException("Could not save stay logged in session.", e);
        }
    }

    public static void saveUsername(String username) {
        try {
            Files.writeString(sessionFile(), username + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Could not save stay logged in username.", e);
        }
    }

    public static String loadUsername() {
        Session session = loadSession();
        if (session != null) {
            return session.username();
        }
        Path file = sessionFile();
        try {
            if (!Files.exists(file)) {
                return null;
            }
            String[] lines = Files.readString(file).split("\\R", -1);
            if (lines.length >= 1) {
                String username = lines[0].trim();
                if (!username.isEmpty()) {
                    return username;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static Session loadSession() {
        Path file = sessionFile();
        try {
            if (!Files.exists(file)) {
                return null;
            }
            return readSessionFile(file);
        } catch (IOException e) {
            return null;
        }
    }

    private static Session readSessionFile(Path path) throws IOException {
        String[] lines = Files.readString(path).split("\\R", -1);
        if (lines.length < 2) {
            return null;
        }
        String username = lines[0].trim();
        String passwordHash = lines[1].trim();
        if (username.isEmpty() || passwordHash.isEmpty()) {
            return null;
        }
        return new Session(username, passwordHash);
    }

    public static void clear() {
        try {
            Files.deleteIfExists(sessionFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not clear stay logged in session.", e);
        }
    }

    private static Path sessionFile() {
        return ClientDataPaths.sessionFile();
    }
}
