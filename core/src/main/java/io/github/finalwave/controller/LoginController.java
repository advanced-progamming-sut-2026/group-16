package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.command.LoginMenuCommands;
import io.github.finalwave.model.user.SecurityQuestion;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.login.LoginGateway;
import io.github.finalwave.login.PasswordChangeGateway;
import io.github.finalwave.login.NetworkPasswordChangeGateway;
import io.github.finalwave.leaderboard.LeaderboardGateway;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
import io.github.finalwave.network.auth.PasswordChangeFailPayload;
import io.github.finalwave.network.auth.PasswordChangeFailReason;
import io.github.finalwave.network.auth.PasswordChangeOkPayload;
import io.github.finalwave.network.auth.ResetPasswordRequest;
import io.github.finalwave.network.sync.ProgressSyncService;
import io.github.finalwave.profile.LocalProfileCache;
import io.github.finalwave.profile.ProfileApplier;
import io.github.finalwave.registration.RegistrationGateway;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.LoginFailMessages;
import io.github.finalwave.util.RegistrationValidator;
import io.github.finalwave.util.SessionResumeCredentials;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.api.AuthView;

import java.util.regex.Matcher;

public class LoginController extends ViewController {
    private final LoginGateway loginGateway;
    private final UserDatabase db;
    private final RegistrationGateway registrationGateway;
    private final LeaderboardGateway leaderboardGateway;
    private final ScoreSubmitGateway scoreSubmitGateway;
    private final boolean usernameOnlyStayLoggedIn;
    private String pendingPasswordResetUsername;
    private String pendingPasswordResetEmail;
    private String pendingPasswordResetAnswer;
    private String pendingSecurityQuestionText;
    private String pendingNewPassword;

    public LoginController(
            LoginGateway loginGateway,
            UserDatabase db,
            RegistrationGateway registrationGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway
    ) {
        this(loginGateway, db, registrationGateway, leaderboardGateway, scoreSubmitGateway, false);
    }

    public LoginController(
            LoginGateway loginGateway,
            UserDatabase db,
            RegistrationGateway registrationGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway,
            boolean usernameOnlyStayLoggedIn
    ) {
        this.loginGateway = loginGateway;
        this.db = db;
        this.registrationGateway = registrationGateway;
        this.leaderboardGateway = leaderboardGateway;
        this.scoreSubmitGateway = scoreSubmitGateway;
        this.usernameOnlyStayLoggedIn = usernameOnlyStayLoggedIn;
    }

    @Override
    public void displayMenu() {
        getAuthView().showLoginMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (LoginMenuCommands cmd : LoginMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_ENTER -> handleMenuEnter(matcher.group("menuName"));
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> back();
                case LOGIN -> login(matcher.group("username"),
                        matcher.group("password"), matcher.group("stayLoggedIn") != null);
                case FORGET_PASSWORD -> {
                    beginPasswordReset(matcher.group("username"), matcher.group("email"));
                    if (pendingPasswordResetUsername != null) {
                        verifySecurityAnswer(matcher.group("answer"));
                    }
                }
            }
            return;
        }

        if (pendingPasswordResetUsername != null) {
            handlePendingPasswordInput(input.trim());
            return;
        }

        getAuthView().errorInvalidLoginCommand();
    }

    public void login(String username, String password, boolean stayLoggedIn) {
        getAuthView().showLoginInlineError("");
        String passwordHash = HashUtil.hashSHA256(password);
        LoginRequest request = new LoginRequest(username, password);
        loginGateway.login(request, new LoginGateway.Callback() {
            @Override
            public void onSuccess(LoginOkPayload payload) {
                SessionResumeCredentials.remember(payload.getUsername(), passwordHash);
                handleLoginSuccess(payload, stayLoggedIn);
            }

            @Override
            public void onFailure(LoginFailPayload payload) {
                handleLoginFailure(payload);
            }
        });
    }

    public String securityQuestionText(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        User user = db.getUser(username.trim());
        if (user == null) {
            return null;
        }
        SecurityQuestion question = SecurityQuestion.fromNumber(user.getSecurityQuestionId());
        return question == null ? null : question.getText();
    }

    public String pendingSecurityQuestionText() {
        if (pendingSecurityQuestionText != null && !pendingSecurityQuestionText.isBlank()) {
            return pendingSecurityQuestionText;
        }
        return securityQuestionText(pendingPasswordResetUsername);
    }

    public void beginPasswordReset(String username, String email) {
        User user = db.getUser(username);
        if (user == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email)) {
            getAuthView().errorWrongUsernameOrEmail();
            return;
        }

        pendingPasswordResetUsername = username;
        pendingPasswordResetEmail = email;
        pendingPasswordResetAnswer = null;
        pendingNewPassword = null;
        pendingSecurityQuestionText = null;

        SecurityQuestion question = SecurityQuestion.fromNumber(user.getSecurityQuestionId());
        if (question != null) {
            pendingSecurityQuestionText = question.getText();
            getAuthView().promptPasswordResetSecurityAnswer();
            return;
        }

        PasswordChangeGateway gateway = NetworkPasswordChangeGateway.getInstance();
        if (gateway == null) {
            getAuthView().promptPasswordResetSecurityAnswer();
            return;
        }

        gateway.lookupSecurityQuestion(
                new io.github.finalwave.network.auth.SecurityQuestionLookupRequest(username, email),
                new PasswordChangeGateway.LookupCallback() {
                    @Override
                    public void onSuccess(io.github.finalwave.network.auth.SecurityQuestionLookupOkPayload payload) {
                        if (payload != null && payload.getSecurityQuestionNumber() > 0) {
                            pendingSecurityQuestionText = payload.getQuestionText();
                            db.updateSecurityQuestion(username, payload.getSecurityQuestionNumber(), null);
                        }
                        getAuthView().promptPasswordResetSecurityAnswer();
                    }

                    @Override
                    public void onFailure(io.github.finalwave.network.auth.PasswordChangeFailPayload payload) {
                        getAuthView().promptPasswordResetSecurityAnswer();
                    }
                });
    }

    public void verifySecurityAnswer(String securityAnswer) {
        if (pendingPasswordResetUsername == null || pendingPasswordResetEmail == null) {
            getAuthView().errorInvalidLoginCommand();
            return;
        }

        User user = db.getUser(pendingPasswordResetUsername);
        if (user == null) {
            clearPendingPasswordReset();
            getAuthView().errorWrongUsernameOrEmail();
            return;
        }

        if (user.getSecurityAnswerHash() != null
                && !user.getSecurityAnswerHash().isBlank()
                && !user.validateSecurityAnswer(securityAnswer)) {
            getAuthView().errorWrongSecurityAnswer();
            getAuthView().promptPasswordResetSecurityAnswer();
            return;
        }

        pendingPasswordResetAnswer = securityAnswer;
        pendingNewPassword = null;
        getAuthView().promptNewPassword();
    }

    /** CLI / one-shot: username + email + answer in a single command. */
    public void verifyIdentity(String username, String email, String securityAnswer) {
        beginPasswordReset(username, email);
        if (pendingPasswordResetUsername != null) {
            verifySecurityAnswer(securityAnswer);
        }
    }

    public void resetPassword(String newPassword, String confirm) {
        if (pendingPasswordResetUsername == null) {
            getAuthView().errorInvalidLoginCommand();
            return;
        }

        if (!RegistrationValidator.isStrongPassword(newPassword)) {
            getAuthView().errorWeakPassword();
            getAuthView().promptNewPassword();
            return;
        }

        if (!newPassword.equals(confirm)) {
            getAuthView().errorRepeatPasswordDoseNotMatch();
            pendingNewPassword = null;
            getAuthView().promptNewPassword();
            return;
        }

        PasswordChangeGateway gateway = NetworkPasswordChangeGateway.getInstance();
        if (gateway == null) {
            applyLocalPasswordReset(newPassword);
            return;
        }

        String username = pendingPasswordResetUsername;
        String email = pendingPasswordResetEmail;
        String answer = pendingPasswordResetAnswer;
        ResetPasswordRequest request = new ResetPasswordRequest(username, email, answer, newPassword);
        String hash = HashUtil.hashSHA256(newPassword);
        gateway.resetPassword(request, new PasswordChangeGateway.Callback() {
            @Override
            public void onSuccess(PasswordChangeOkPayload payload) {
                db.updatePassword(username, hash);
                StayLoggedInStorage.clear();
                clearPendingPasswordReset();
                getAuthView().showPasswordChanged();
            }

            @Override
            public void onFailure(PasswordChangeFailPayload payload) {
                handleResetFailure(payload);
            }
        });
    }

    private void applyLocalPasswordReset(String newPassword) {
        db.updatePassword(pendingPasswordResetUsername, HashUtil.hashSHA256(newPassword));
        StayLoggedInStorage.clear();
        clearPendingPasswordReset();
        getAuthView().showPasswordChanged();
    }

    private void handleResetFailure(PasswordChangeFailPayload payload) {
        String reason = payload == null ? null : payload.getReason();
        if (PasswordChangeFailReason.WRONG_SECURITY_ANSWER.equals(reason)) {
            clearPendingPasswordReset();
            getAuthView().errorWrongSecurityAnswer();
            return;
        }
        if (PasswordChangeFailReason.USER_NOT_FOUND.equals(reason)
                || PasswordChangeFailReason.BAD_CREDENTIALS.equals(reason)) {
            clearPendingPasswordReset();
            getAuthView().errorWrongUsernameOrEmail();
            return;
        }
        if (PasswordChangeFailReason.WEAK_PASSWORD.equals(reason)) {
            getAuthView().errorWeakPassword();
            getAuthView().promptNewPassword();
            return;
        }
        if (PasswordChangeFailReason.SAME_PASSWORD.equals(reason)) {
            getAuthView().errorWeakPassword();
            getAuthView().promptNewPassword();
            return;
        }
        if (PasswordChangeFailReason.NOT_CONNECTED.equals(reason)) {
            getAuthView().showLoginInlineError(LoginFailMessages.messageFor(LoginFailReason.NOT_CONNECTED));
            return;
        }
        getAuthView().showLoginInlineError(LoginFailMessages.messageFor(LoginFailReason.SERVER_ERROR));
    }

    public void back() {
        clearPendingPasswordReset();
        navigator.pop();
    }

    public void cancelPasswordReset() {
        clearPendingPasswordReset();
    }

    private void handleLoginSuccess(LoginOkPayload payload, boolean stayLoggedIn) {
        User user = ProfileApplier.apply(payload);
        String passwordHash = SessionResumeCredentials.passwordHash();
        if (passwordHash != null && !passwordHash.isBlank()) {
            user.setPasswordHash(passwordHash);
        }
        LocalProfileCache.sync(db, user, passwordHash);
        clearPendingPasswordReset();
        if (stayLoggedIn) {
            if (usernameOnlyStayLoggedIn) {
                StayLoggedInStorage.saveUsername(user.getUsername());
            } else {
                User stored = db.getUser(user.getUsername());
                String storedHash = stored != null ? stored.getPasswordHash() : user.getPasswordHash();
                StayLoggedInStorage.saveSession(user.getUsername(), storedHash);
            }
        } else {
            StayLoggedInStorage.clear();
        }
        App.getInstance().setCurrentUser(user);
        ProgressSyncService sync = ProgressSyncService.getInstance();
        if (sync != null) {
            sync.arm();
        }
        getAuthView().showUserLoggedIn();
        navigator.reset(new MainMenuController(user, db, registrationGateway, loginGateway, leaderboardGateway, scoreSubmitGateway));
    }

    private void handleLoginFailure(LoginFailPayload payload) {
        String reason = payload == null ? null : payload.getReason();
        getAuthView().showLoginInlineError(LoginFailMessages.messageFor(reason));
    }

    private void handleMenuEnter(String menuName) {
        getAuthView().errorInvalidMenuName();
    }

    private void handleShowCurrent() {
        getAuthView().showCurrentLoginMenu();
    }

    private void handlePendingPasswordInput(String input) {
        if (pendingPasswordResetUsername != null && pendingPasswordResetAnswer == null) {
            verifySecurityAnswer(input);
            return;
        }
        if (pendingNewPassword == null) {
            if (!RegistrationValidator.isStrongPassword(input)) {
                getAuthView().errorWeakPassword();
                getAuthView().promptNewPassword();
                return;
            }
            pendingNewPassword = input;
            getAuthView().promptPasswordConfirm();
            return;
        }

        resetPassword(pendingNewPassword, input);
    }

    private void clearPendingPasswordReset() {
        pendingPasswordResetUsername = null;
        pendingPasswordResetEmail = null;
        pendingPasswordResetAnswer = null;
        pendingSecurityQuestionText = null;
        pendingNewPassword = null;
    }

    private AuthView getAuthView() {
        return (AuthView) view;
    }
}
