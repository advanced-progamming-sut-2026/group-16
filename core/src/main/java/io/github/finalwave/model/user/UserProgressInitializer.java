package io.github.finalwave.model.user;

import io.github.finalwave.model.greenhouse.GreenhouseLayout;
import io.github.finalwave.model.minigame.MiniGameId;

public final class UserProgressInitializer {
    private UserProgressInitializer() {
    }

    public static void initializeUserProgress(User user) {
        user.setCoins(0);
        user.setDiamonds(0);
        user.setPlantFood(0);
        user.getStoredBoosts().clear();
        user.getGreenhousePots().clear();
        ensureGreenhousePots(user);
        user.setDailyOfferPlant(null);
        user.setDailyOfferDate(null);
        user.setDailyOfferPurchased(false);
        user.getUnlockedMinigames().add(MiniGameId.VASE_BREAKER.getKey());
        user.getUnlockedLevels().add("1-1");
    }

    public static void ensureGreenhousePots(User user) {
        if (user == null) {
            return;
        }
        for (int y = 1; y <= GreenhouseLayout.ROWS; y++) {
            for (int x = 1; x <= GreenhouseLayout.COLUMNS; x++) {
                if (user.getPotAt(x, y) != null) {
                    continue;
                }
                user.getGreenhousePots().add(new GreenhousePot(x, y, GreenhouseLayout.startsLocked(y)));
            }
        }
    }
}
