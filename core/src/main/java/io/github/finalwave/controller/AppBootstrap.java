package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.login.LoginGateway;
import io.github.finalwave.leaderboard.LeaderboardGateway;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.profile.ProfileApplier;
import io.github.finalwave.registration.RegistrationGateway;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.profile.ProfileExporter;


public final class AppBootstrap {
    private final UserDatabase userDatabase;
    private final RegistrationGateway registrationGateway;
    private final LoginGateway loginGateway;
    private final LeaderboardGateway leaderboardGateway;
    private final ScoreSubmitGateway scoreSubmitGateway;
    private final boolean restoreLocalSession;
    private final boolean usernameOnlyStayLoggedIn;
    private final ControllerNavigator navigator;

    public AppBootstrap(
            UserDatabase userDatabase,
            RegistrationGateway registrationGateway,
            LoginGateway loginGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway,
            NavigationBinder binder,
            boolean restoreLocalSession
    ) {
        this(userDatabase, registrationGateway, loginGateway, leaderboardGateway, scoreSubmitGateway, binder, restoreLocalSession, !restoreLocalSession);
    }

    public AppBootstrap(
            UserDatabase userDatabase,
            RegistrationGateway registrationGateway,
            LoginGateway loginGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway,
            NavigationBinder binder,
            boolean restoreLocalSession,
            boolean usernameOnlyStayLoggedIn
    ) {
        this.userDatabase = userDatabase;
        this.registrationGateway = registrationGateway;
        this.loginGateway = loginGateway;
        this.leaderboardGateway = leaderboardGateway;
        this.scoreSubmitGateway = scoreSubmitGateway;
        this.restoreLocalSession = restoreLocalSession;
        this.usernameOnlyStayLoggedIn = usernameOnlyStayLoggedIn;
        this.navigator = new ControllerNavigator(binder);
    }

    public UserDatabase userDatabase() {
        return userDatabase;
    }

    public RegistrationGateway registrationGateway() {
        return registrationGateway;
    }

    public LoginGateway loginGateway() {
        return loginGateway;
    }

    public LeaderboardGateway leaderboardGateway() {
        return leaderboardGateway;
    }

    public ScoreSubmitGateway scoreSubmitGateway() {
        return scoreSubmitGateway;
    }

    public ControllerNavigator navigator() {
        return navigator;
    }

    public void start() {
        if (restoreLocalSession) {
            User stayLoggedInUser = restoreStayLoggedInUser();
            if (stayLoggedInUser != null) {
                App.getInstance().setCurrentUser(stayLoggedInUser);
                navigator.reset(new MainMenuController(
                        stayLoggedInUser,
                        userDatabase,
                        registrationGateway,
                        loginGateway,
                        leaderboardGateway,
                        scoreSubmitGateway
                ));
                return;
            }
        }
        navigator.reset(new RegistrationController(
                registrationGateway,
                userDatabase,
                loginGateway,
                leaderboardGateway,
                scoreSubmitGateway,
                usernameOnlyStayLoggedIn
        ));
    }

    public User restoreStayLoggedInUser() {
        StayLoggedInStorage.Session session = StayLoggedInStorage.loadSession();
        if (session == null) {
            return null;
        }

        User user = userDatabase.getUser(session.username());
        if (user == null || !session.passwordHash().equals(user.getPasswordHash())) {
            StayLoggedInStorage.clear();
            return null;
        }
        try {
            LoginOkPayload payload = ProfileExporter.export(user, userDatabase);
            return ProfileApplier.apply(payload);
        } catch (RuntimeException exception) {
            StayLoggedInStorage.clear();
            return null;
        }
    }
}
