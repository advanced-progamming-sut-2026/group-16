package view.api;

public interface AuthView extends View {
    public void errorDuplicateName();

    public void errorWeakPassword();

    public void errorRepeatPasswordDoseNotMatch();

    public void errorNicknameLength();

    public void errorWrongEmailPattern();

    public void showUserCreated();

    public void errorWrongUsernameOrPassword();

    public void errorWrongUsernameOrEmail();

    public void errorWrongSecurityAnswer();

    public void errorInvalidUsernamePattern();

    public void errorDuplicateEmail();

    public void errorInvalidGender();

    public void errorMustRegisterFirst();

    public void errorSecurityAnswerMismatch();

    public void errorInvalidSecurityQuestionNumber();

    public void errorInvalidRegisterCommand();

    public void errorInvalidLoginCommand();

    public void errorInvalidMenuName();

    public void promptNewPassword();

    public void promptPasswordConfirm();

    public void showUserLoggedIn();

    public void showPasswordChanged();

    public void showRegistrationMenu();

    public void showLoginMenu();

    public void showCurrentRegistrationMenu();

    public void showCurrentLoginMenu();

    public void showSecurityQuestions();

    public void promptSecurityAnswer();

    public void promptSecurityAnswerConfirm();

}
