package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.util.StayLoggedInStorage;


public final class AppBootstrap {
    private final UserDatabase userDatabase;
    private final ControllerNavigator navigator;

    public AppBootstrap(UserDatabase userDatabase, NavigationBinder binder) {
        this.userDatabase = userDatabase;
        this.navigator = new ControllerNavigator(binder);
    }

    public UserDatabase userDatabase() {
        return userDatabase;
    }

    public ControllerNavigator navigator() {
        return navigator;
    }


    public void start() {
        User stayLoggedInUser = restoreStayLoggedInUser();
        if (stayLoggedInUser != null) {
            App.getInstance().setCurrentUser(stayLoggedInUser);
            navigator.reset(new MainMenuController(stayLoggedInUser, userDatabase));
        } else {
            navigator.reset(new RegistrationController(userDatabase));
        }
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
        return user;
    }
}
