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
    private RegistrationController registrationController;
    private String pendingPasswordResetUsername;
    private String pendingNewPassword;

    public LoginController(UserDatabase db) {
        this.db = db;
    }

    public void setRegistrationController(RegistrationController registrationController) {
        this.registrationController = registrationController;
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
                case MENU_EXIT -> handleMenuExit();
                case LOGIN ->
                        handleLogin(matcher.group("username"),
                                matcher.group("password"), matcher.group("stayLoggedIn"));
                case FORGET_PASSWORD ->
                        handleForgetPassword(matcher.group("username"),
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

    private void handleMenuEnter(String menuName) {
        // TODO: implement after main menu navigation is done.
        getAuthView().errorInvalidMenuName();
    }

    private void handleShowCurrent() {
        getAuthView().showCurrentLoginMenu();
    }

    private void handleMenuExit() {
        clearPendingPasswordReset();
        parser.switchController(registrationController);
    }

    private void handleLogin(String username, String password, String stayLoggedIn) {
        User user = db.getUser(username);
        if (user == null || !user.authenticate(password)) {
            getAuthView().errorWrongUsernameOrPassword();
            return;
        }

        clearPendingPasswordReset();
        if (stayLoggedIn != null) {
            StayLoggedInStorage.saveSession(user.getUsername(), user.getPasswordHash());
        } else {
            StayLoggedInStorage.clear();
        }

        App.getInstance().setCurrentUser(user);
        getAuthView().showUserLoggedIn();
        parser.switchController(new MainMenuController(user, registrationController, db));
    }

    private void handleForgetPassword(String username, String email, String answer) {
        User user = db.getUser(username);
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            getAuthView().errorWrongUsernameOrEmail();
            return;
        }

        if (!user.validateSecurityAnswer(answer)) {
            getAuthView().errorWrongSecurityAnswer();
            clearPendingPasswordReset();
            parser.switchController(registrationController);
            return;
        }

        pendingPasswordResetUsername = username;
        pendingNewPassword = null;
        getAuthView().promptNewPassword();
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

        if (!pendingNewPassword.equals(input)) {
            getAuthView().errorRepeatPasswordDoseNotMatch();
            pendingNewPassword = null;
            getAuthView().promptNewPassword();
            return;
        }

        db.updatePassword(pendingPasswordResetUsername, HashUtil.hashSHA256(pendingNewPassword));
        StayLoggedInStorage.clear();
        clearPendingPasswordReset();
        getAuthView().showPasswordChanged();
    }

    private void clearPendingPasswordReset() {
        pendingPasswordResetUsername = null;
        pendingNewPassword = null;
    }

    private AuthView getAuthView() {
        return (AuthView) view;
    }
}
