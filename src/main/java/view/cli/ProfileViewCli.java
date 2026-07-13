package view.cli;

import model.user.User;
import view.api.ProfileView;

public class ProfileViewCli extends CliView implements ProfileView {
    // profile change-username command
    @Override
    public void errorSameUsername() {
        displayError("The given username is the same as your old one.");
    }

    @Override
    public void errorInvalidUsername() {
        displayError("The given username is not valid.");
    }

    // profile change-nickname command
    @Override
    public void errorSameNickname() {
        displayError("The given nickname is the same as your old one.");
    }

    // profile change-email command
    @Override
    public void errorSameEmail() {
        displayError("The given email is the same as your old one.");
    }

    @Override
    public void errorInvalidEmail() {
        displayError("The given email is not valid.");
    }

    // profile change-password command
    @Override
    public void errorSamePassword() {
        displayError("The given password is the same as your old one.");
    }

    @Override
    public void errorWrongOldPassword() {
        displayError("The given old password is incorrect.");
    }

    @Override
    public void errorWeakPassword() {
        displayError("The given new password is weak.");
    }

    // profile show-info
    @Override
    public void showUserInfo(User user) {
        // TODO: implement after the User class is done.
    }

    @Override
    public void showProfileMenu() {
        displayMessage("Profile Menu");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: profile");
    }

    // prompt new try for fields
    @Override
    public void promptNewUsername() {
        displayMessage("Please enter your new username.");
    }

    @Override
    public void promptNewNickname() {
        displayMessage("Please enter your new nickname.");
    }

    @Override
    public void promptNewEmail() {
        displayMessage("Please enter your new email.");
    }

    @Override
    public void promptNewPassword() {
        displayMessage("Please enter your new password.");
    }
}
