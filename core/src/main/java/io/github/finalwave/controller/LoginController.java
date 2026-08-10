package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.command.LoginMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegistrationValidator;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.api.AuthView;

import java.util.regex.Matcher;

public class LoginController extends ViewController {
    private final UserDatabase db;
    private String pendingPasswordResetUsername;
    private String pendingNewPassword;

    public LoginController(UserDatabase db) {
        this.db = db;
    }

    @Override
    public void displayMenu() {
        getAuthView().showLoginMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (LoginMenuCommands cmd : LoginMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_ENTER -> handleMenuEnter(matcher.group("menuName"));
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> back();
                case LOGIN -> login(matcher.group("username"),
                        matcher.group("password"), matcher.group("stayLoggedIn") != null);
                case FORGET_PASSWORD -> verifyIdentity(matcher.group("username"),
                        matcher.group("email"), matcher.group("answer"));
            }
            return;
        }

        if (pendingPasswordResetUsername != null) {
            handlePendingPasswordInput(input.trim());
            return;
        }

        getAuthView().errorInvalidLoginCommand();
    }

    public void login(String username, String password, boolean stayLoggedIn) {
        User user = db.getUser(username);
        if (user == null || !user.authenticate(password)) {
            getAuthView().errorWrongUsernameOrPassword();
            return;
        }

        clearPendingPasswordReset();
        if (stayLoggedIn) {
            StayLoggedInStorage.saveSession(user.getUsername(), user.getPasswordHash());
        } else {
            StayLoggedInStorage.clear();
        }

        App.getInstance().setCurrentUser(user);
        getAuthView().showUserLoggedIn();
        navigator.reset(new MainMenuController(user, db));
    }

    public void verifyIdentity(String username, String email, String securityAnswer) {
        User user = db.getUser(username);
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            getAuthView().errorWrongUsernameOrEmail();
            return;
        }

        if (!user.validateSecurityAnswer(securityAnswer)) {
            getAuthView().errorWrongSecurityAnswer();
            clearPendingPasswordReset();
            navigator.pop();
            return;
        }

        pendingPasswordResetUsername = username;
        pendingNewPassword = null;
        getAuthView().promptNewPassword();
    }

    public void resetPassword(String newPassword, String confirm) {
        if (pendingPasswordResetUsername == null) {
            getAuthView().errorInvalidLoginCommand();
            return;
        }

        if (!RegistrationValidator.isStrongPassword(newPassword)) {
            getAuthView().errorWeakPassword();
            getAuthView().promptNewPassword();
            return;
        }

        if (!newPassword.equals(confirm)) {
            getAuthView().errorRepeatPasswordDoseNotMatch();
            pendingNewPassword = null;
            getAuthView().promptNewPassword();
            return;
        }

        db.updatePassword(pendingPasswordResetUsername, HashUtil.hashSHA256(newPassword));
        StayLoggedInStorage.clear();
        clearPendingPasswordReset();
        getAuthView().showPasswordChanged();
    }

    public void back() {
        clearPendingPasswordReset();
        navigator.pop();
    }

    private void handleMenuEnter(String menuName) {
        getAuthView().errorInvalidMenuName();
    }

    private void handleShowCurrent() {
        getAuthView().showCurrentLoginMenu();
    }

    private void handlePendingPasswordInput(String input) {
        if (pendingNewPassword == null) {
            if (!RegistrationValidator.isStrongPassword(input)) {
                getAuthView().errorWeakPassword();
                getAuthView().promptNewPassword();
                return;
            }
            pendingNewPassword = input;
            getAuthView().promptPasswordConfirm();
            return;
        }

        resetPassword(pendingNewPassword, input);
    }

    private void clearPendingPasswordReset() {
        pendingPasswordResetUsername = null;
        pendingNewPassword = null;
    }

    private AuthView getAuthView() {
        return (AuthView) view;
    }
}
