package io.github.finalwave.controller;

import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

final class ZombieSeenUnlock {

    private ZombieSeenUnlock() {
    }

    static void unlock(User user, UserDatabase userDatabase, UnlockService unlockService, String type) {
        if (unlockService.unlockZombie(user, type)) {
            userDatabase.saveUserWallet(user);
        }
    }
}
