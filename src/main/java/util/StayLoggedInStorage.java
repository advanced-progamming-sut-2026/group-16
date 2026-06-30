package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StayLoggedInStorage {
    private static final Path SESSION_FILE = Path.of("user.session");

    private StayLoggedInStorage() {
    }

    public record Session(String username, String passwordHash) {
    }

    public static void saveSession(String username, String passwordHash) {
        try {
            Files.writeString(SESSION_FILE, username + "\n" + passwordHash);
        } catch (IOException e) {
            throw new RuntimeException("Could not save stay logged in session.", e);
        }
    }

    public static Session loadSession() {
        try {
            if (!Files.exists(SESSION_FILE)) {
                return null;
            }

            return readSessionFile(SESSION_FILE);
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
            Files.deleteIfExists(SESSION_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Could not clear stay logged in session.", e);
        }
    }
}
