package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.command.RegisterMenuCommands;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.SecurityQuestion;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.model.user.UserProgressInitializer;
import io.github.finalwave.login.LoginGateway;
import io.github.finalwave.leaderboard.LeaderboardGateway;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.network.sync.ProgressSyncService;
import io.github.finalwave.profile.LocalProfileCache;
import io.github.finalwave.registration.RegistrationGateway;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegisterFailMessages;
import io.github.finalwave.util.RegistrationValidator;
import io.github.finalwave.util.SessionResumeCredentials;
import io.github.finalwave.view.api.AuthView;

import java.util.regex.Matcher;

public class RegistrationController extends ViewController {
    private final RegistrationGateway registrationGateway;
    private final UserDatabase db;
    private final LoginGateway loginGateway;
    private final LeaderboardGateway leaderboardGateway;
    private final ScoreSubmitGateway scoreSubmitGateway;
    private final boolean usernameOnlyStayLoggedIn;

    private String pendingUsername;
    private String pendingPassword;
    private String pendingNickname;
    private String pendingEmail;
    private Gender pendingGender;
    private boolean awaitingSecurityQuestion;
    private SecurityQuestion pendingSecurityQuestion;
    private String pendingSecurityAnswer;

    public RegistrationController(
            RegistrationGateway registrationGateway,
            UserDatabase db,
            LoginGateway loginGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway
    ) {
        this(registrationGateway, db, loginGateway, leaderboardGateway, scoreSubmitGateway, false);
    }

    public RegistrationController(
            RegistrationGateway registrationGateway,
            UserDatabase db,
            LoginGateway loginGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway,
            boolean usernameOnlyStayLoggedIn
    ) {
        this.registrationGateway = registrationGateway;
        this.db = db;
        this.loginGateway = loginGateway;
        this.leaderboardGateway = leaderboardGateway;
        this.scoreSubmitGateway = scoreSubmitGateway;
        this.usernameOnlyStayLoggedIn = usernameOnlyStayLoggedIn;
    }

    public RegistrationGateway registrationGateway() {
        return registrationGateway;
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
                case REGISTER -> register(matcher.group("username"), matcher.group("password"),
                        matcher.group("passwordConfirm"), matcher.group("nickname"),
                        matcher.group("email"), matcher.group("gender"));
                case PICK_QUESTION -> pickSecurityQuestion(matcher.group("questionNumber"),
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

    public void register(String username, String password, String passwordConfirm,
                         String nickname, String email, String genderText) {
        getAuthView().showRegisterInlineError("");
        if (!validateRegisterInput(username, password, passwordConfirm, nickname, email, genderText)) {
            return;
        }

        pendingUsername = username.trim();
        pendingPassword = password;
        pendingNickname = nickname.trim();
        pendingEmail = email.trim();
        pendingGender = parseGender(genderText);
        awaitingSecurityQuestion = true;
        pendingSecurityQuestion = null;
        pendingSecurityAnswer = null;

        getAuthView().showSecurityQuestions();
    }

    public void pickSecurityQuestion(String questionNumber, String answer, String answerConfirm) {
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

    public void goToLogin() {
        navigator.push(new LoginController(
                loginGateway, db, registrationGateway, leaderboardGateway, scoreSubmitGateway, usernameOnlyStayLoggedIn));
    }

    private void handleMenuEnter(String menuName) {
        if ("login".equals(normalizeMenuName(menuName))) {
            goToLogin();
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

        RegisterRequest request = new RegisterRequest(
                pendingUsername,
                pendingPassword,
                pendingNickname,
                pendingEmail,
                pendingGender.name(),
                question.getNumber(),
                answer
        );

        registrationGateway.register(request, new RegistrationGateway.Callback() {
            @Override
            public void onSuccess(RegisterOkPayload payload) {
                handleRegisterSuccess(payload);
            }

            @Override
            public void onFailure(RegisterFailPayload payload) {
                handleRegisterFailure(payload);
            }
        });
    }

    private void handleRegisterSuccess(RegisterOkPayload payload) {
        User user = userFromPayload(payload);
        if (pendingSecurityQuestion != null) {
            user.setSecurityQuestionId(pendingSecurityQuestion.getNumber());
            if (pendingSecurityAnswer != null && !pendingSecurityAnswer.isBlank()) {
                user.setSecurityAnswerHash(HashUtil.hashSHA256(pendingSecurityAnswer.trim()));
            }
        } else if (payload.getSecurityQuestionNumber() > 0) {
            user.setSecurityQuestionId(payload.getSecurityQuestionNumber());
        }
        String passwordHash = pendingPassword != null ? HashUtil.hashSHA256(pendingPassword) : null;
        if (passwordHash != null) {
            LocalProfileCache.sync(db, user, passwordHash);
            SessionResumeCredentials.remember(payload.getUsername(), passwordHash);
        }
        App.getInstance().setCurrentUser(user);
        ProgressSyncService sync = ProgressSyncService.getInstance();
        if (sync != null) {
            sync.arm();
        }
        getAuthView().showUserCreated();
        clearPendingRegistration();
        navigator.reset(new MainMenuController(
                user, db, registrationGateway, loginGateway, leaderboardGateway, scoreSubmitGateway));
    }

    private void handleRegisterFailure(RegisterFailPayload payload) {
        String reason = payload == null ? null : payload.getReason();
        getAuthView().showRegisterInlineError(RegisterFailMessages.messageFor(reason));
        clearPendingRegistration();
    }

    private User userFromPayload(RegisterOkPayload payload) {
        Gender gender = Gender.fromString(payload.getGender());
        User user = new User(
                payload.getUsername(),
                "",
                payload.getNickname(),
                payload.getEmail(),
                gender
        );
        user.setId(payload.getUserId());
        UserProgressInitializer.initializeUserProgress(user);
        user.setCoins(payload.getCoins());
        user.setDiamonds(payload.getDiamonds());
        user.setPlantFood(payload.getPlantFood());
        return user;
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
            getAuthView().showRegisterInlineError(RegisterFailMessages.messageFor(RegisterFailReason.INVALID_USERNAME));
            return false;
        }
        if (!RegistrationValidator.isStrongPassword(password)) {
            getAuthView().showRegisterInlineError(RegisterFailMessages.messageFor(RegisterFailReason.WEAK_PASSWORD));
            return false;
        }
        if (!password.equals(passwordConfirm)) {
            getAuthView().errorRepeatPasswordDoseNotMatch();
            getAuthView().showRegisterInlineError("Re-Password does not match the password.");
            return false;
        }
        if (!RegistrationValidator.isValidNickname(nickname)) {
            getAuthView().showRegisterInlineError(RegisterFailMessages.messageFor(RegisterFailReason.INVALID_NICKNAME));
            return false;
        }
        if (!RegistrationValidator.isValidEmail(email)) {
            getAuthView().showRegisterInlineError(RegisterFailMessages.messageFor(RegisterFailReason.INVALID_EMAIL));
            return false;
        }
        if (parseGender(genderText) == null) {
            getAuthView().showRegisterInlineError(RegisterFailMessages.messageFor(RegisterFailReason.INVALID_GENDER));
            return false;
        }
        return true;
    }

    private void clearPendingRegistration() {
        pendingUsername = null;
        pendingPassword = null;
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
