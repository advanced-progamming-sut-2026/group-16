package model;

import model.user.User;

public class App {
    private App app;

    private User currentUser;

    private App() {
    }

    public static App getInstance() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
