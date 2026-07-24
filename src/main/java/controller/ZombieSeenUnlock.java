package controller;

import model.user.UnlockService;
import model.user.User;
import model.user.UserDatabase;

final class ZombieSeenUnlock {

    private ZombieSeenUnlock() {
    }

    static void unlock(User user, UserDatabase userDatabase, UnlockService unlockService, String type) {
        if (unlockService.unlockZombie(user, type)) {
            userDatabase.saveUserWallet(user);
        }
    }
}
