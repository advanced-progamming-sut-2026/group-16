package io.github.finalwave.controller;

import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.util.StayLoggedInStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthIntentSmokeTest {
    private static final Path DATABASE = Path.of("target", "auth-intent-smoke.db");
    private Path sessionFile;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
        sessionFile = Path.of("user.session");
        Files.deleteIfExists(sessionFile);
        StayLoggedInStorage.clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        StayLoggedInStorage.clear();
        Files.deleteIfExists(DATABASE);
        Files.deleteIfExists(sessionFile);
    }

    @Test
    void typedIntentsRegisterLoginLogoutThroughNavigator() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        RegistrationController registration = (RegistrationController) parser.getCurrentController();
        registration.register("intent-user", "Passw0rd!", "Passw0rd!", "IntentNick", "intent@example.com", "male");
        registration.pickSecurityQuestion("1", "fluffy", "fluffy");
        assertInstanceOf(LoginController.class, parser.getCurrentController());

        LoginController login = (LoginController) parser.getCurrentController();
        login.login("intent-user", "Passw0rd!", true);
        assertInstanceOf(MainMenuController.class, parser.getCurrentController());
        assertTrue(Files.exists(sessionFile) || StayLoggedInStorage.loadSession() != null);

        MainMenuController main = (MainMenuController) parser.getCurrentController();
        main.logout();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());
    }
}
