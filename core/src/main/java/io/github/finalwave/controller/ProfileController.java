package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.command.ProfileMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegistrationValidator;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.api.ProfileView;

import java.util.regex.Matcher;

public class ProfileController extends ViewController {
    private final UserDatabase userDatabase;
    private MainMenuController mainMenuController;
    private boolean pendingUsername;
    private boolean pendingNickname;
    private boolean pendingEmail;
    private boolean pendingPassword;

    public ProfileController(UserDatabase userDatabase) {
        if (userDatabase == null) {
            throw new IllegalArgumentException("userDatabase must not be null");
        }
        this.userDatabase = userDatabase;
    }

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    @Override
    public void displayMenu() {
        getProfileView().showProfileMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (ProfileMenuCommands cmd : ProfileMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null)
                continue;

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case CHANGE_USERNAME -> handleChangeUsername(matcher.group("username"));
                case CHANGE_NICKNAME -> handleChangeNickname(matcher.group("nickname"));
                case CHANGE_EMAIL -> handleChangeEmail(matcher.group("email"));
                case CHANGE_PASSWORD -> handleChangePassword(matcher.group("newPassword"),
                        matcher.group("oldPassword"));
                case SHOW_INFO -> handleShowInfo();
            }
            return;
        }

        if (pendingUsername) {
            handlePendingUsername(input.trim());
            return;
        }

        if (pendingNickname) {
            handlePendingNickname(input.trim());
            return;
        }

        if (pendingEmail) {
            handlePendingEmail(input.trim());
            return;
        }

        if (pendingPassword) {
            handlePendingPassword(input.trim());
            return;
        }

        getProfileView().errorInvalidCommand();
    }

    private void handleShowCurrent() {
        getProfileView().showCurrentMenu();
    }

    private void handleMenuExit() {
        clearPendingStates();
        parser.switchController(mainMenuController);
    }

    private void handleChangeUsername(String username) {
        clearPendingStates();
        User user = currentUser();
        if (user.getUsername().equals(username)) {
            getProfileView().errorSameUsername();
            pendingUsername = true;
            getProfileView().promptNewUsername();
            return;
        }

        if (!RegistrationValidator.isValidUsername(username)) {
            getProfileView().errorInvalidUsername();
            pendingUsername = true;
            getProfileView().promptNewUsername();
            return;
        }

        if (userDatabase.isUsernameTaken(username)) {
            getProfileView().errorUsernameTaken();
            pendingUsername = true;
            getProfileView().promptNewUsername();
            return;
        }

        applyUsernameChange(user, username);
    }

    private void handlePendingUsername(String input) {
        User user = currentUser();
        if (user.getUsername().equals(input)) {
            getProfileView().errorSameUsername();
            getProfileView().promptNewUsername();
            return;
        }

        if (!RegistrationValidator.isValidUsername(input)) {
            getProfileView().errorInvalidUsername();
            getProfileView().promptNewUsername();
            return;
        }

        if (userDatabase.isUsernameTaken(input)) {
            getProfileView().errorUsernameTaken();
            getProfileView().promptNewUsername();
            return;
        }

        applyUsernameChange(user, input);
        pendingUsername = false;
    }

    private void applyUsernameChange(User user, String username) {
        user.setUsername(username);
        persistProfile(user);
        refreshStayLoggedInSession(user);
    }

    private void handleChangeNickname(String nickname) {
        clearPendingStates();
        User user = currentUser();
        if (user.getNickname().equals(nickname)) {
            getProfileView().errorSameNickname();
            pendingNickname = true;
            getProfileView().promptNewNickname();
            return;
        }

        applyNicknameChange(user, nickname);
    }

    private void handlePendingNickname(String input) {
        User user = currentUser();
        if (user.getNickname().equals(input)) {
            getProfileView().errorSameNickname();
            getProfileView().promptNewNickname();
            return;
        }

        applyNicknameChange(user, input);
        pendingNickname = false;
    }

    private void applyNicknameChange(User user, String nickname) {
        user.setNickname(nickname);
        persistProfile(user);
    }

    private void handleChangeEmail(String email) {
        clearPendingStates();
        User user = currentUser();
        if (user.getEmail().equals(email)) {
            getProfileView().errorSameEmail();
            pendingEmail = true;
            getProfileView().promptNewEmail();
            return;
        }

        if (!RegistrationValidator.isValidEmail(email)) {
            getProfileView().errorInvalidEmail();
            pendingEmail = true;
            getProfileView().promptNewEmail();
            return;
        }

        if (userDatabase.emailExists(email)) {
            getProfileView().errorEmailTaken();
            pendingEmail = true;
            getProfileView().promptNewEmail();
            return;
        }

        applyEmailChange(user, email);
    }

    private void handlePendingEmail(String input) {
        User user = currentUser();
        if (user.getEmail().equals(input)) {
            getProfileView().errorSameEmail();
            getProfileView().promptNewEmail();
            return;
        }

        if (!RegistrationValidator.isValidEmail(input)) {
            getProfileView().errorInvalidEmail();
            getProfileView().promptNewEmail();
            return;
        }

        if (userDatabase.emailExists(input)) {
            getProfileView().errorEmailTaken();
            getProfileView().promptNewEmail();
            return;
        }

        applyEmailChange(user, input);
        pendingEmail = false;
    }

    private void applyEmailChange(User user, String email) {
        user.setEmail(email);
        persistProfile(user);
    }

    private void handleChangePassword(String newPassword, String oldPassword) {
        clearPendingStates();
        User user = currentUser();
        if (!user.getPasswordHash().equals(HashUtil.hashSHA256(oldPassword))) {
            getProfileView().errorWrongOldPassword();
            return;
        }

        if (user.getPasswordHash().equals(HashUtil.hashSHA256(newPassword))) {
            getProfileView().errorSamePassword();
            pendingPassword = true;
            getProfileView().promptNewPassword();
            return;
        }

        if (!RegistrationValidator.isStrongPassword(newPassword)) {
            getProfileView().errorWeakPassword();
            pendingPassword = true;
            getProfileView().promptNewPassword();
            return;
        }

        applyPasswordChange(user, newPassword);
    }

    private void handlePendingPassword(String input) {
        User user = currentUser();
        if (user.getPasswordHash().equals(HashUtil.hashSHA256(input))) {
            getProfileView().errorSamePassword();
            getProfileView().promptNewPassword();
            return;
        }

        if (!RegistrationValidator.isStrongPassword(input)) {
            getProfileView().errorWeakPassword();
            getProfileView().promptNewPassword();
            return;
        }

        applyPasswordChange(user, input);
        pendingPassword = false;
    }

    private void applyPasswordChange(User user, String newPassword) {
        String hash = HashUtil.hashSHA256(newPassword);
        user.setPasswordHash(hash);
        userDatabase.updatePassword(user.getUsername(), hash);
        refreshStayLoggedInSession(user);
    }

    private void persistProfile(User user) {
        userDatabase.updateProfile(user);
    }

    private void refreshStayLoggedInSession(User user) {
        StayLoggedInStorage.Session session = StayLoggedInStorage.loadSession();
        if (session != null) {
            StayLoggedInStorage.saveSession(user.getUsername(), user.getPasswordHash());
        }
    }

    private void clearPendingStates() {
        pendingUsername = false;
        pendingNickname = false;
        pendingEmail = false;
        pendingPassword = false;
    }

    private void handleShowInfo() {
        getProfileView().showUserInfo(currentUser());
    }

    private User currentUser() {
        return App.getInstance().getCurrentUser();
    }

    private ProfileView getProfileView() {
        return (ProfileView) view;
    }
}
