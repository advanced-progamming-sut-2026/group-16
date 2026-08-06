package io.github.finalwave.model.user;

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
        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 5; x++) {
                user.getGreenhousePots().add(new GreenhousePot(x, y, y > 1));
            }
        }
        user.setDailyOfferPlant(null);
        user.setDailyOfferDate(null);
        user.setDailyOfferPurchased(false);
        user.getUnlockedMinigames().add(MiniGameId.VASE_BREAKER.getKey());
        user.getUnlockedLevels().add("1-1");
    }
}
