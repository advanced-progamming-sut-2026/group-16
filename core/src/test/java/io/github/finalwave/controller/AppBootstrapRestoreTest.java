package io.github.finalwave.controller;

import io.github.finalwave.leaderboard.FailingLeaderboardGateway;
import io.github.finalwave.leaderboard.NetworkLeaderboardGateway;
import io.github.finalwave.login.LocalLoginGateway;
import io.github.finalwave.model.App;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.registration.LocalRegistrationGateway;
import io.github.finalwave.score.FailingScoreSubmitGateway;
import io.github.finalwave.score.NetworkScoreSubmitGateway;
import io.github.finalwave.util.ClientDataPaths;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.SessionResumeCredentials;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.api.MainMenuView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppBootstrapRestoreTest {
    private static final Path DATABASE = Path.of("target", "app-bootstrap-restore-test.db");
    private static final String USERNAME = "restore-user";
    private static final String PASSWORD_HASH = HashUtil.hashSHA256("Passw0rd!");

    private UserDatabase database;
    private Path sessionFile;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
        database = UserDatabase.getInstance();
        sessionFile = ClientDataPaths.sessionFile();
        Files.deleteIfExists(sessionFile);
        StayLoggedInStorage.clear();
        SessionResumeCredentials.clear();
        App.getInstance().setCurrentUser(null);

        User user = new User(USERNAME, PASSWORD_HASH, "RestoreNick", "restore@example.com", Gender.MALE);
        database.registerUser(user);
        StayLoggedInStorage.saveSession(USERNAME, PASSWORD_HASH);
    }

    @AfterEach
    void tearDown() throws Exception {
        App.getInstance().setCurrentUser(null);
        SessionResumeCredentials.clear();
        StayLoggedInStorage.clear();
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
        Files.deleteIfExists(sessionFile);
    }

    @Test
    void startRestoresSavedSessionIntoMainMenu() {
        AppBootstrap bootstrap = new AppBootstrap(
                database,
                new LocalRegistrationGateway(database),
                new LocalLoginGateway(database),
                new FailingLeaderboardGateway(NetworkLeaderboardGateway.NOT_CONNECTED),
                new FailingScoreSubmitGateway(NetworkScoreSubmitGateway.NOT_CONNECTED),
                controller -> {
                    if (controller instanceof MainMenuController mainMenuController) {
                        mainMenuController.setView(new NoopMainMenuView());
                    }
                },
                true
        );

        bootstrap.start();

        assertInstanceOf(MainMenuController.class, bootstrap.navigator().current());
        User currentUser = App.getInstance().getCurrentUser();
        assertNotNull(currentUser);
        assertEquals(USERNAME, currentUser.getUsername());
        assertEquals(USERNAME, SessionResumeCredentials.username());
        assertEquals(PASSWORD_HASH, SessionResumeCredentials.passwordHash());
    }

    private static final class NoopMainMenuView implements MainMenuView {
        @Override
        public void displayMessage(String line) {
        }

        @Override
        public void displayError(String line) {
        }

        @Override
        public void showMainMenu(String nickname, boolean hasUnreadNews) {
        }

        @Override
        public void showCurrentMenu() {
        }

        @Override
        public void showLoggedOut() {
        }

        @Override
        public void errorInvalidMainMenuCommand() {
        }

        @Override
        public void errorInvalidMenuName() {
        }
    }
}
