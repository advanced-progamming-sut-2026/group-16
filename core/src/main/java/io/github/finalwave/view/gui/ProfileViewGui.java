package io.github.finalwave.view.gui;

import io.github.finalwave.controller.ProfileController;
import io.github.finalwave.model.App;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.api.ProfileView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

public final class ProfileViewGui extends GuiViewBase implements ProfileView {
    public ProfileViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(ProfileController controller) {
    }

    @Override
    public void errorSameUsername() {
        toastError("The given username is the same as your old one.");
    }

    @Override
    public void errorInvalidUsername() {
        toastError("The given username is not valid.");
    }

    @Override
    public void errorUsernameTaken() {
        toastError("This username is already taken. Please use another one.");
    }

    @Override
    public void errorSameNickname() {
        toastError("The given nickname is the same as your old one.");
    }

    @Override
    public void errorSameEmail() {
        toastError("The given email is the same as your old one.");
    }

    @Override
    public void errorInvalidEmail() {
        toastError("The given email is not valid.");
    }

    @Override
    public void errorEmailTaken() {
        toastError("This email is already taken. Please use another one.");
    }

    @Override
    public void errorSamePassword() {
        toastError("The given password is the same as your old one.");
    }

    @Override
    public void errorWrongOldPassword() {
        toastError("The given old password is incorrect.");
    }

    @Override
    public void errorWeakPassword() {
        toastError("The given new password is weak.");
    }

    @Override
    public void showUserInfo(User user) {
        router.refreshProfile(user);
        router.closeProfilePasswordModal();
    }

    @Override
    public void showProfileMenu() {
        User user = App.getInstance().getCurrentUser();
        router.refreshProfile(user);
    }

    @Override
    public void showCurrentMenu() {
        toast("Current menu: profile");
    }

    @Override
    public void promptNewUsername() {
        toast("Please enter your new username.");
    }

    @Override
    public void promptNewNickname() {
        toast("Please enter your new nickname.");
    }

    @Override
    public void promptNewEmail() {
        toast("Please enter your new email.");
    }

    @Override
    public void promptNewPassword() {
        toast("Please enter your new password.");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid profile command.");
    }
}
