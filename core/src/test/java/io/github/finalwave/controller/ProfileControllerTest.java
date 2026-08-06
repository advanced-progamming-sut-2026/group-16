package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.view.api.ProfileView;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileControllerTest {
    private static final Path DATABASE = Path.of("target", "profile-controller-test.db");

    private UserDatabase database;
    private RecordingProfileView view;
    private ProfileController controller;
    private User user;

    @BeforeAll
    static void configureDatabase() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
        App.getInstance().setCurrentUser(null);
    }

    @BeforeEach
    void setUp() {
        database = UserDatabase.getInstance();
        view = new RecordingProfileView();
        controller = new ProfileController(database);
        controller.setView(view);

        String unique = "u" + System.nanoTime();
        user = new User(
                unique,
                HashUtil.hashSHA256("Passw0rd!"),
                "NickName",
                unique + "@example.com",
                Gender.MALE);
        database.registerUser(user);
        user.setCoins(120);
        user.setDiamonds(4);
        user.setBestMeowPoint(350);
        user.recordGamePlayed();
        database.saveUserWallet(user);
        user.getChapterProgress().markLevelCompleted(
                io.github.finalwave.model.adventure.ChapterId.ANCIENT_EGYPT, 1);
        database.saveAdventureProgress(user);

        App.getInstance().setCurrentUser(user);
    }

    @Test
    void showInfoDisplaysDocsFields() {
        controller.handleCommand("menu profile show-info");
        assertNotNull(view.lastShownUser);
        assertEquals(user.getUsername(), view.lastShownUser.getUsername());
        assertEquals("NickName", view.lastShownUser.getNickname());
        assertEquals(1, view.lastShownUser.getGamesPlayed());
        assertEquals(120, view.lastShownUser.getCoins());
        assertEquals(4, view.lastShownUser.getDiamonds());
        assertEquals(1, view.lastShownUser.getChapterProgress().countCompletedLevels());
        assertEquals(350, view.lastShownUser.getBestMeowPoint());
    }

    @Test
    void changeNicknamePersistsAcrossReload() {
        controller.handleCommand("menu profile change-nickname -u FreshNick");
        assertEquals("FreshNick", user.getNickname());

        User loaded = database.getUser(user.getUsername());
        assertNotNull(loaded);
        assertEquals("FreshNick", loaded.getNickname());
    }

    @Test
    void changeUsernameRejectsTakenNameAndAcceptsUnique() {
        User other = new User(
                "takenname",
                HashUtil.hashSHA256("Passw0rd!"),
                "Other",
                "taken-" + System.nanoTime() + "@example.com",
                Gender.FEMALE);
        database.registerUser(other);
        String oldName = user.getUsername();

        controller.handleCommand("menu profile change-username -u takenname");
        assertTrue(view.usernameTakenCalled);
        assertEquals(oldName, App.getInstance().getCurrentUser().getUsername());

        String newName = "renamed" + System.nanoTime();
        controller.handleCommand("menu profile change-username -u " + newName);
        assertEquals(newName, user.getUsername());
        assertNull(database.getUser(oldName));
        assertNotNull(database.getUser(newName));
    }

    @Test
    void changePasswordPersistsViaUpdatePassword() {
        controller.handleCommand("menu profile change-password -p NewPass1! -o Passw0rd!");
        User loaded = database.getUser(user.getUsername());
        assertNotNull(loaded);
        assertEquals(HashUtil.hashSHA256("NewPass1!"), loaded.getPasswordHash());
        assertTrue(loaded.authenticate("NewPass1!"));
    }

    @Test
    void changeEmailRejectsTakenEmail() {
        User other = new User(
                "emailuser" + System.nanoTime(),
                HashUtil.hashSHA256("Passw0rd!"),
                "Other",
                "taken-email@example.com",
                Gender.MALE);
        database.registerUser(other);

        controller.handleCommand("menu profile change-email -e taken-email@example.com");
        assertTrue(view.emailTakenCalled);
        assertEquals(user.getEmail(), App.getInstance().getCurrentUser().getEmail());
    }

    private static final class RecordingProfileView implements ProfileView {
        User lastShownUser;
        boolean usernameTakenCalled;
        boolean emailTakenCalled;

        @Override
        public void displayMessage(String line) {
        }

        @Override
        public void displayError(String line) {
        }

        @Override
        public void errorSameUsername() {
        }

        @Override
        public void errorInvalidUsername() {
        }

        @Override
        public void errorUsernameTaken() {
            usernameTakenCalled = true;
        }

        @Override
        public void errorSameNickname() {
        }

        @Override
        public void errorSameEmail() {
        }

        @Override
        public void errorInvalidEmail() {
        }

        @Override
        public void errorEmailTaken() {
            emailTakenCalled = true;
        }

        @Override
        public void errorSamePassword() {
        }

        @Override
        public void errorWrongOldPassword() {
        }

        @Override
        public void errorWeakPassword() {
        }

        @Override
        public void showUserInfo(User user) {
            lastShownUser = user;
        }

        @Override
        public void showProfileMenu() {
        }

        @Override
        public void showCurrentMenu() {
        }

        @Override
        public void promptNewUsername() {
        }

        @Override
        public void promptNewNickname() {
        }

        @Override
        public void promptNewEmail() {
        }

        @Override
        public void promptNewPassword() {
        }

        @Override
        public void errorInvalidCommand() {
        }
    }
}
