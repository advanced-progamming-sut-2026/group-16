package io.github.finalwave.view.gui;

import io.github.finalwave.controller.ViewController;
import io.github.finalwave.view.api.AuthView;
import io.github.finalwave.view.gui.screen.ScreenRouter;


public final class AuthViewGui extends GuiViewBase implements AuthView {
    public AuthViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(ViewController controller) {

    }

    @Override
    public void showRegistrationMenu() {

    }

    @Override
    public void showLoginMenu() {

    }

    @Override
    public void showSecurityQuestions() {
        router.openSignupSecurityModal();
    }

    @Override
    public void promptSecurityAnswer() {
        toast("Please enter your security answer.");
    }

    @Override
    public void promptSecurityAnswerConfirm() {
        toast("Please confirm your security answer.");
    }

    @Override
    public void promptNewPassword() {
        router.showLoginPasswordResetStep();
    }

    @Override
    public void promptPasswordConfirm() {
        toast("Please confirm your new password.");
    }

    @Override
    public void showUserCreated() {
        toast("User account has been successfully created.");
    }

    @Override
    public void showUserLoggedIn() {
        toast("You have been successfully logged in.");
    }

    @Override
    public void showPasswordChanged() {
        router.closeLoginForgotPasswordModal();
        toast("Your password has been successfully changed.");
    }

    @Override
    public void showCurrentRegistrationMenu() {
        toast("Current menu: registration");
    }

    @Override
    public void showCurrentLoginMenu() {
        toast("Current menu: login");
    }

    @Override
    public void errorDuplicateName() {
        toastError("This username is already taken. Please use another one.");
    }

    @Override
    public void errorWeakPassword() {
        toastError("Password must be at least 8 characters and include lower, upper, digit, and special characters.");
    }

    @Override
    public void errorRepeatPasswordDoseNotMatch() {
        toastError("Re-Password does not match the password.");
    }

    @Override
    public void errorNicknameLength() {
        toastError("Nickname must be between 3 and 30 characters.");
    }

    @Override
    public void errorWrongEmailPattern() {
        toastError("This email is not a correct one.");
    }

    @Override
    public void errorWrongUsernameOrPassword() {
        toastError("Wrong Username or Password has been given.");
    }

    @Override
    public void errorWrongUsernameOrEmail() {
        toastError("Wrong Username or Email has been given.");
    }

    @Override
    public void errorWrongSecurityAnswer() {
        toastError("The answer is wrong.");
    }

    @Override
    public void errorInvalidUsernamePattern() {
        toastError("Username may only contain letters, numbers, and hyphens.");
    }

    @Override
    public void errorDuplicateEmail() {
        toastError("This email is already registered.");
    }

    @Override
    public void errorInvalidGender() {
        toastError("Invalid gender.");
    }

    @Override
    public void errorMustRegisterFirst() {
        toastError("You must register first.");
    }

    @Override
    public void errorSecurityAnswerMismatch() {
        toastError("Security answer confirmation does not match.");
    }

    @Override
    public void errorInvalidSecurityQuestionNumber() {
        toastError("Invalid security question number.");
    }

    @Override
    public void errorInvalidRegisterCommand() {
        toastError("Invalid register command.");
    }

    @Override
    public void errorInvalidLoginCommand() {
        toastError("Invalid login command.");
    }

    @Override
    public void errorInvalidMenuName() {
        toastError("Invalid menu name.");
    }

    @Override
    public void showRegisterInlineError(String message) {
        if (message == null || message.isBlank()) {
            router.clearSignupInlineError();
            return;
        }
        router.showSignupInlineError(message);
    }

    @Override
    public void showLoginInlineError(String message) {
        if (message == null || message.isBlank()) {
            router.clearLoginInlineError();
            return;
        }
        router.showLoginInlineError(message);
    }
}
