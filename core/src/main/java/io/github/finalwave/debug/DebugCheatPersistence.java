package io.github.finalwave.debug;

import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

public final class DebugCheatPersistence {
    private static final int MAX_STORED_PLANT_FOOD = 3;

    private DebugCheatPersistence() {
    }

    public static boolean addCoins(User user, UserDatabase database, int amount) {
        if (!canApply(user, database, amount)) {
            return false;
        }
        user.addCoins(amount);
        persistWallet(user, database);
        return true;
    }

    public static boolean addDiamonds(User user, UserDatabase database, int amount) {
        if (!canApply(user, database, amount)) {
            return false;
        }
        user.addDiamonds(amount);
        persistWallet(user, database);
        return true;
    }

    public static boolean addPlantFood(User user, UserDatabase database, int amount) {
        if (!canApply(user, database, amount)) {
            return false;
        }
        int next = Math.min(MAX_STORED_PLANT_FOOD, user.getPlantFood() + amount);
        if (next == user.getPlantFood()) {
            return false;
        }
        user.setPlantFood(next);
        persistWallet(user, database);
        return true;
    }

    private static boolean canApply(User user, UserDatabase database, int amount) {
        return user != null && database != null && user.isDebugMode() && amount > 0;
    }

    private static void persistWallet(User user, UserDatabase database) {
        database.saveUserWallet(user);
    }
}
