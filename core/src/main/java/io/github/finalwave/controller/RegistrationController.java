package io.github.finalwave.controller;

import io.github.finalwave.model.command.RegisterMenuCommands;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.SecurityQuestion;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegistrationValidator;
import io.github.finalwave.view.api.AuthView;

import java.util.regex.Matcher;

public class RegistrationController extends ViewController {
    private final UserDatabase db;

    private String pendingUsername;
    private String pendingPasswordHash;
    private String pendingNickname;
    private String pendingEmail;
    private Gender pendingGender;
    private boolean awaitingSecurityQuestion;
    private SecurityQuestion pendingSecurityQuestion;
    private String pendingSecurityAnswer;

    public RegistrationController(UserDatabase db) {
        this.db = db;
    }

    @Override
    public void displayMenu() {
        getAuthView().showRegistrationMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (RegisterMenuCommands cmd : RegisterMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_ENTER -> handleMenuEnter(matcher.group("menuName"));
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case REGISTER -> handleRegister(matcher.group("username"), matcher.group("password"),
                        matcher.group("passwordConfirm"), matcher.group("nickname"),
                        matcher.group("email"), matcher.group("gender"));
                case PICK_QUESTION -> handlePickQuestion(matcher.group("questionNumber"),
                        matcher.group("answer").trim(), matcher.group("answerConfirm").trim());
            }
            return;
        }

        if (awaitingSecurityQuestion) {
            handlePendingSecurityQuestionInput(input.trim());
            return;
        }

        getAuthView().errorInvalidRegisterCommand();
    }

    private void handleMenuEnter(String menuName) {
        if ("login".equals(normalizeMenuName(menuName))) {
            navigator.push(new LoginController(db));
            return;
        }
        getAuthView().errorInvalidMenuName();
    }

    private void handleShowCurrent() {
        getAuthView().showCurrentRegistrationMenu();
    }

    private void handleMenuExit() {
        System.exit(0);
    }

    private void handleRegister(String username, String password, String passwordConfirm,
                                String nickname, String email, String genderText) {
        if (!validateRegisterInput(username, password, passwordConfirm, nickname, email, genderText)) {
            return;
        }

        pendingUsername = username;
        pendingPasswordHash = HashUtil.hashSHA256(password);
        pendingNickname = nickname;
        pendingEmail = email;
        pendingGender = parseGender(genderText);
        awaitingSecurityQuestion = true;
        pendingSecurityQuestion = null;
        pendingSecurityAnswer = null;

        getAuthView().showSecurityQuestions();
    }

    private void handlePickQuestion(String questionNumber, String answer, String answerConfirm) {
        if (!awaitingSecurityQuestion) {
            getAuthView().errorMustRegisterFirst();
            return;
        }

        SecurityQuestion question = parseSecurityQuestion(questionNumber);
        if (question == null) {
            getAuthView().errorInvalidSecurityQuestionNumber();
            return;
        }

        completeRegistration(question, answer, answerConfirm);
    }

    private void handlePendingSecurityQuestionInput(String input) {
        if (pendingSecurityQuestion == null) {
            SecurityQuestion question = parseSecurityQuestion(input);
            if (question == null) {
                getAuthView().errorInvalidSecurityQuestionNumber();
                getAuthView().showSecurityQuestions();
                return;
            }
            pendingSecurityQuestion = question;
            getAuthView().promptSecurityAnswer();
            return;
        }

        if (pendingSecurityAnswer == null) {
            pendingSecurityAnswer = input;
            getAuthView().promptSecurityAnswerConfirm();
            return;
        }

        completeRegistration(pendingSecurityQuestion, pendingSecurityAnswer, input);
    }

    private void completeRegistration(SecurityQuestion question, String answer, String answerConfirm) {
        if (!answer.equals(answerConfirm)) {
            getAuthView().errorSecurityAnswerMismatch();
            pendingSecurityQuestion = question;
            pendingSecurityAnswer = null;
            getAuthView().promptSecurityAnswer();
            return;
        }

        User user = new User(pendingUsername, pendingPasswordHash, pendingNickname,
                pendingEmail, pendingGender);
        user.setSecurityQuestionId(question.getNumber());
        user.setSecurityAnswerHash(HashUtil.hashSHA256(answer));

        try {
            db.registerUser(user);
        } catch (RuntimeException e) {
            getAuthView().errorDuplicateName();
            clearPendingRegistration();
            return;
        }

        getAuthView().showUserCreated();
        clearPendingRegistration();
        navigator.push(new LoginController(db));
    }

    private SecurityQuestion parseSecurityQuestion(String questionNumber) {
        try {
            return SecurityQuestion.fromNumber(Integer.parseInt(questionNumber));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean validateRegisterInput(String username, String password, String passwordConfirm,
                                          String nickname, String email, String genderText) {
        if (!RegistrationValidator.isValidUsername(username)) {
            getAuthView().errorInvalidUsernamePattern();
            return false;
        }
        if (db.isUsernameTaken(username)) {
            getAuthView().errorDuplicateName();
            return false;
        }
        if (!RegistrationValidator.isStrongPassword(password)) {
            getAuthView().errorWeakPassword();
            return false;
        }
        if (!password.equals(passwordConfirm)) {
            getAuthView().errorRepeatPasswordDoseNotMatch();
            return false;
        }
        if (!RegistrationValidator.isValidNickname(nickname)) {
            getAuthView().errorNicknameLength();
            return false;
        }
        if (!RegistrationValidator.isValidEmail(email)) {
            getAuthView().errorWrongEmailPattern();
            return false;
        }
        if (db.emailExists(email)) {
            getAuthView().errorDuplicateEmail();
            return false;
        }
        if (parseGender(genderText) == null) {
            getAuthView().errorInvalidGender();
            return false;
        }
        return true;
    }

    private void clearPendingRegistration() {
        pendingUsername = null;
        pendingPasswordHash = null;
        pendingNickname = null;
        pendingEmail = null;
        pendingGender = null;
        awaitingSecurityQuestion = false;
        pendingSecurityQuestion = null;
        pendingSecurityAnswer = null;
    }

    private Gender parseGender(String genderText) {
        return Gender.fromString(genderText);
    }

    private String normalizeMenuName(String menuName) {
        return menuName.trim().toLowerCase();
    }

    private AuthView getAuthView() {
        return (AuthView) view;
    }
}
