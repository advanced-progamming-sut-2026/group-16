package view.cli;

import model.user.User;
import view.api.ProfileView;

public class ProfileViewCli extends CliView implements ProfileView {
    @Override
    public void errorSameUsername() {
        displayError("The given username is the same as your old one.");
    }

    @Override
    public void errorInvalidUsername() {
        displayError("The given username is not valid.");
    }

    @Override
    public void errorUsernameTaken() {
        displayError("This username is already taken. Please use another one.");
    }

    @Override
    public void errorSameNickname() {
        displayError("The given nickname is the same as your old one.");
    }

    @Override
    public void errorSameEmail() {
        displayError("The given email is the same as your old one.");
    }

    @Override
    public void errorInvalidEmail() {
        displayError("The given email is not valid.");
    }

    @Override
    public void errorEmailTaken() {
        displayError("This email is already taken. Please use another one.");
    }

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

    @Override
    public void showUserInfo(User user) {
        if (user == null) {
            displayError("No user is logged in.");
            return;
        }
        displayMessage("Username: " + user.getUsername());
        displayMessage("Nickname: " + user.getNickname());
        displayMessage("Games played: " + user.getGamesPlayed());
        displayMessage("Coins: " + user.getCoins());
        displayMessage("Diamonds: " + user.getDiamonds());
        displayMessage("Levels cleared: " + user.getChapterProgress().countCompletedLevels());
        displayMessage("Best MeowPoint: " + user.getBestMeowPoint());
    }

    @Override
    public void showProfileMenu() {
        displayMessage("Profile Menu");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: profile");
    }

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

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid profile command.");
    }
}
