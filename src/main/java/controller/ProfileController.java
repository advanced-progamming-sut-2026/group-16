package controller;

import model.App;
import model.command.ProfileMenuCommands;
import util.HashUtil;
import util.RegistrationValidator;
import view.api.ProfileView;

import java.util.regex.Matcher;

public class ProfileController extends ViewController {
    private MainMenuController mainMenuController;
    private boolean pendingUsername;
    private boolean pendingNickname;
    private boolean pendingEmail;
    private boolean pendingPassword;

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

        view.displayError("Invalid profile command.");
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
        if (App.getInstance().getCurrentUser().getUsername().equals(username)) {
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

        App.getInstance().getCurrentUser().setUsername(username);
    }

    private void handlePendingUsername(String input) {
        if (App.getInstance().getCurrentUser().getUsername().equals(input)) {
            getProfileView().errorSameUsername();
            getProfileView().promptNewUsername();
            return;
        }

        if (!RegistrationValidator.isValidUsername(input)) {
            getProfileView().errorInvalidUsername();
            getProfileView().promptNewUsername();
            return;
        }

        App.getInstance().getCurrentUser().setUsername(input);
        pendingUsername = false;
    }

    private void handleChangeNickname(String nickname) {
        clearPendingStates();
        if (App.getInstance().getCurrentUser().getNickname().equals(nickname)) {
            getProfileView().errorSameNickname();
            pendingNickname = true;
            getProfileView().promptNewNickname();
            return;
        }

        App.getInstance().getCurrentUser().setNickname(nickname);
    }

    private void handlePendingNickname(String input) {
        if (App.getInstance().getCurrentUser().getNickname().equals(input)) {
            getProfileView().errorSameNickname();
            getProfileView().promptNewNickname();
            return;
        }

        App.getInstance().getCurrentUser().setNickname(input);
        pendingNickname = false;
    }

    private void handleChangeEmail(String email) {
        clearPendingStates();
        if (App.getInstance().getCurrentUser().getEmail().equals(email)) {
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

        App.getInstance().getCurrentUser().setEmail(email);
    }

    private void handlePendingEmail(String input) {
        if (App.getInstance().getCurrentUser().getEmail().equals(input)) {
            getProfileView().errorSameEmail();
            getProfileView().promptNewEmail();
            return;
        }

        if (!RegistrationValidator.isValidEmail(input)) {
            getProfileView().errorInvalidEmail();
            getProfileView().promptNewEmail();
            return;
        }

        App.getInstance().getCurrentUser().setEmail(input);
        pendingEmail = false;
    }

    private void handleChangePassword(String newPassword, String oldPassword) {
        clearPendingStates();
        if (!App.getInstance().getCurrentUser().getPasswordHash().equals(HashUtil.hashSHA256(oldPassword))) {
            getProfileView().errorWrongOldPassword();
            return;
        }

        if (App.getInstance().getCurrentUser().getPasswordHash().equals(HashUtil.hashSHA256(newPassword))) {
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

        App.getInstance().getCurrentUser().setPasswordHash(HashUtil.hashSHA256(newPassword));
    }

    private void handlePendingPassword(String input) {
        if (App.getInstance().getCurrentUser().getPasswordHash().equals(HashUtil.hashSHA256(input))) {
            getProfileView().errorSamePassword();
            getProfileView().promptNewPassword();
            return;
        }

        if (!RegistrationValidator.isStrongPassword(input)) {
            getProfileView().errorWeakPassword();
            getProfileView().promptNewPassword();
            return;
        }

        App.getInstance().getCurrentUser().setPasswordHash(HashUtil.hashSHA256(input));
        pendingPassword = false;
    }

    private void clearPendingStates() {
        pendingUsername = false;
        pendingNickname = false;
        pendingEmail = false;
        pendingPassword = false;
    }

    private void handleShowInfo() {
        // TODO: implement after Profile is done.
    }

    private ProfileView getProfileView() {
        return (ProfileView) view;
    }
}