package io.github.finalwave.view.cli;

import io.github.finalwave.model.user.SecurityQuestion;
import io.github.finalwave.view.api.AuthView;

public class AuthViewCli extends CliView implements AuthView {
    // register command
    //     errors:
    @Override
    public void errorDuplicateName() {
        displayError("This username is already taken. Please use another one.");
    }

    @Override
    public void errorWeakPassword() {
        displayError("This password is weak. Please use a stronger one.\n" +
                "It should at least have 8 characters using lowercase, uppercase, numbers and " +
                "special characters (? > < , \" ' ; : \\ / | [ ] } { + = ( ) * & ^ % $ # !   ).");
    }

    @Override
    public void errorRepeatPasswordDoseNotMatch() {
        displayError("Re-Password does not match the password.");
    }

    @Override
    public void errorNicknameLength() {
        displayError("Nickname length should at least be 3 characters at most 30.");
    }

    @Override
    public void errorWrongEmailPattern() {
        displayError("This email is not a correct one.");
    }

    //     success:
    @Override
    public void showUserCreated() {
        displayMessage("User account has been successfully created.");
    }

    // login command
    //     errors:
    @Override
    public void errorWrongUsernameOrPassword() {
        displayError("Wrong Username or Password has been given.");
    }

    @Override
    public void errorWrongUsernameOrEmail() {
        displayError("Wrong Username or Email has been given.");
    }

    @Override
    public void errorWrongSecurityAnswer() {
        displayError("The answer is wrong.");
    }

    @Override
    public void errorInvalidUsernamePattern() {
        displayError("Username can only contain letters, numbers, and hyphen.");
    }

    @Override
    public void errorDuplicateEmail() {
        displayError("This email is already registered.");
    }

    @Override
    public void errorInvalidGender() {
        displayError("Invalid gender.");
    }

    @Override
    public void errorMustRegisterFirst() {
        displayError("You must register first.");
    }

    @Override
    public void errorSecurityAnswerMismatch() {
        displayError("Security answer confirmation does not match.");
    }

    @Override
    public void errorInvalidSecurityQuestionNumber() {
        displayError("Invalid security question number.");
    }

    @Override
    public void errorInvalidRegisterCommand() {
        displayError("Invalid register command.");
    }

    @Override
    public void errorInvalidLoginCommand() {
        displayError("Invalid login command.");
    }

    @Override
    public void errorInvalidMenuName() {
        displayError("Invalid menu name.");
    }

    @Override
    public void promptNewPassword() {
        displayMessage("Please enter your new password.");
    }

    @Override
    public void promptPasswordConfirm() {
        displayMessage("Please confirm your new password.");
    }

    //     success:
    @Override
    public void showUserLoggedIn() {
        displayMessage("You have been successfully logged in.");
    }

    @Override
    public void showPasswordChanged() {
        displayMessage("Your password has been successfully changed.");
    }

    @Override
    public void showRegistrationMenu() {
        displayMessage("Registration menu");
    }

    @Override
    public void showLoginMenu() {
        displayMessage("Login menu");
    }

    @Override
    public void showCurrentRegistrationMenu() {
        displayMessage("Current menu: registration");
    }

    @Override
    public void showCurrentLoginMenu() {
        displayMessage("Current menu: login");
    }

    @Override
    public void showSecurityQuestions() {
        displayMessage("Pick one security question (Choose a number):");
        for (SecurityQuestion question : SecurityQuestion.values()) {
            displayMessage(question.getNumber() + ". " + question.getText());
        }
    }

    @Override
    public void promptSecurityAnswer() {
        displayMessage("Please enter your security answer.");
    }

    @Override
    public void promptSecurityAnswerConfirm() {
        displayMessage("Please confirm your security answer.");
    }
}
