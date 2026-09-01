package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.command.LoginMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.login.LoginGateway;
import io.github.finalwave.leaderboard.LeaderboardGateway;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
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
                case FORGET_PASSWORD -> verifyIdentity(matcher.group("username"),
                        matcher.group("email"), matcher.group("answer"));
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

    public void verifyIdentity(String username, String email, String securityAnswer) {
        User user = db.getUser(username);
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            getAuthView().errorWrongUsernameOrEmail();
            return;
        }

        if (!user.validateSecurityAnswer(securityAnswer)) {
            getAuthView().errorWrongSecurityAnswer();
            clearPendingPasswordReset();
            navigator.pop();
            return;
        }

        pendingPasswordResetUsername = username;
        pendingNewPassword = null;
        getAuthView().promptNewPassword();
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

        db.updatePassword(pendingPasswordResetUsername, HashUtil.hashSHA256(newPassword));
        StayLoggedInStorage.clear();
        clearPendingPasswordReset();
        getAuthView().showPasswordChanged();
    }

    public void back() {
        clearPendingPasswordReset();
        navigator.pop();
    }

    private void handleLoginSuccess(LoginOkPayload payload, boolean stayLoggedIn) {
        User user = ProfileApplier.apply(payload);
        LocalProfileCache.sync(db, user, SessionResumeCredentials.passwordHash());
        clearPendingPasswordReset();
        if (stayLoggedIn) {
            if (usernameOnlyStayLoggedIn) {
                StayLoggedInStorage.saveUsername(user.getUsername());
            } else {
                User stored = db.getUser(user.getUsername());
                String passwordHash = stored != null ? stored.getPasswordHash() : user.getPasswordHash();
                StayLoggedInStorage.saveSession(user.getUsername(), passwordHash);
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
        pendingNewPassword = null;
    }

    private AuthView getAuthView() {
        return (AuthView) view;
    }
}
