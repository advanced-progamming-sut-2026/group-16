package io.github.finalwave.server.db;

import io.github.finalwave.model.collection.OwnedPlant;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.UnlockKind;
import io.github.finalwave.model.user.User;

import java.util.List;

public final class ServerDatabase {
    private final Object lock = new Object();
    private io.github.finalwave.model.user.UserDatabase delegate;

    public void initializeSchema() {
        synchronized (lock) {
            delegate = io.github.finalwave.model.user.UserDatabase.getInstance();
        }
    }

    public void registerUser(User user) {
        synchronized (lock) {
            requireDelegate().registerUser(user);
        }
    }

    public User getUser(String username) {
        synchronized (lock) {
            return requireDelegate().getUser(username);
        }
    }

    public List<User> getAllUsers() {
        synchronized (lock) {
            return requireDelegate().getAllUsers();
        }
    }

    public boolean isUsernameTaken(String username) {
        synchronized (lock) {
            return requireDelegate().isUsernameTaken(username);
        }
    }

    public boolean emailExists(String email) {
        synchronized (lock) {
            return requireDelegate().emailExists(email);
        }
    }

    public void saveBestMeowPoint(User user) {
        synchronized (lock) {
            requireDelegate().saveBestMeowPoint(user);
        }
    }

    public void saveUserWallet(User user) {
        synchronized (lock) {
            requireDelegate().saveUserWallet(user);
        }
    }

    public void savePlantEntry(User user, OwnedPlant plant) {
        synchronized (lock) {
            requireDelegate().savePlantEntry(user, plant);
        }
    }

    public void saveGreenhousePot(User user, GreenhousePot pot) {
        synchronized (lock) {
            requireDelegate().saveGreenhousePot(user, pot);
        }
    }

    public void saveStoredBoosts(User user) {
        synchronized (lock) {
            requireDelegate().saveStoredBoosts(user);
        }
    }

    public void saveUnlock(User user, UnlockKind kind, String name) {
        synchronized (lock) {
            requireDelegate().saveUnlock(user, kind, name);
        }
    }

    public void saveAdventureProgress(User user) {
        synchronized (lock) {
            requireDelegate().saveAdventureProgress(user);
        }
    }

    public void saveMatchSnapshot(User user, MatchSaveSnapshot snapshot) {
        synchronized (lock) {
            requireDelegate().saveMatchSnapshot(user, snapshot);
        }
    }

    public void clearMatchSave(User user) {
        synchronized (lock) {
            requireDelegate().clearMatchSave(user);
        }
    }

    public void saveUserNews(User user) {
        synchronized (lock) {
            requireDelegate().saveUserNews(user);
        }
    }

    public void saveUserSettings(User user) {
        synchronized (lock) {
            requireDelegate().saveUserSettings(user);
        }
    }

    public io.github.finalwave.model.user.UserDatabase delegate() {
        synchronized (lock) {
            return requireDelegate();
        }
    }

    private io.github.finalwave.model.user.UserDatabase requireDelegate() {
        if (delegate == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return delegate;
    }
}
