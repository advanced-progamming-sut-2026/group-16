package view.api;

import model.user.User;

public interface ProfileView extends View {
    void errorSameUsername();

    void errorInvalidUsername();

    void errorSameNickname();

    void errorSameEmail();

    void errorInvalidEmail();

    void errorSamePassword();

    void errorWrongOldPassword();

    void errorWeakPassword();

    void showUserInfo(User user);

    void showProfileMenu();

    void showCurrentMenu();

    void promptNewUsername();

    void promptNewNickname();

    void promptNewEmail();

    void promptNewPassword();

    void errorInvalidCommand();
}
