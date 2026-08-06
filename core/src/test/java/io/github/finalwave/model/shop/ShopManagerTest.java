package io.github.finalwave.model.shop;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserProgressInitializer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopManagerTest {
    @Test
    void buyPotRequiresCoinsAndUnlocksSlots() {
        User poor = createUser();
        ShopManager shop = new ShopManager(new FixedRandom(0));
        assertEquals("insufficient_coins", shop.purchase(poor, "pot", 1, null).status());

        User rich = createUser();
        rich.setCoins(4000);
        assertEquals("success", shop.purchase(rich, "pot", 2, null).status());
        assertEquals(7, rich.countUnlockedPots());
    }

    @Test
    void plantFoodCannotExceedCapacity() {
        User user = createUser();
        user.setDiamonds(20);
        user.setPlantFood(2);

        ShopManager shop = new ShopManager(new FixedRandom(0));
        assertEquals("max_capacity", shop.purchase(user, "plant_food", 2, null).status());
        assertEquals("success", shop.purchase(user, "plant_food", 1, null).status());
        assertEquals(3, user.getPlantFood());
    }

    @Test
    void randomAndSelectiveSeedsModifyPlantProgress() {
        User user = createUser();
        user.setCoins(5000);
        user.setDiamonds(20);
        ShopManager shop = new ShopManager(new FixedRandom(0));

        assertEquals("success", shop.purchase(user, "seed_random", 1, null).status());
        assertTrue(user.getPlantProgress().getOwnedPlant("Peashooter").orElseThrow().getSeedPackets() >= 5);

        assertEquals("plant_type_required", shop.purchase(user, "seed_selective", 1, null).status());
        assertEquals("success", shop.purchase(user, "seed_selective", 2, "Sunflower").status());
        assertEquals(20, user.getPlantProgress().getOwnedPlant("Sunflower").orElseThrow().getSeedPackets());
        assertEquals("plant_not_unlocked", shop.purchase(user, "seed_selective", 1, "Cherry Bomb").status());
    }

    @Test
    void gemToCoinConsumesDiamonds() {
        User user = createUser();
        user.setDiamonds(10);

        ShopManager shop = new ShopManager(new FixedRandom(0));
        assertEquals("success", shop.purchase(user, "gem_to_coin", 2, null).status());
        assertEquals(1000, user.getCoins());
        assertEquals(0, user.getDiamonds());
    }

    @Test
    void dailyOfferRefreshesAndCanOnlyBeBoughtOncePerDay() {
        User user = createUser();
        user.setCoins(2000);
        user.setDailyOfferDate(LocalDate.now().minusDays(1));

        ShopManager shop = new ShopManager(new FixedRandom(1));
        shop.refreshDailyOfferIfNeeded(user);
        assertEquals("Sunflower", user.getDailyOfferPlant());

        assertEquals("success", shop.purchase(user, "daily", 1, null).status());
        assertTrue(user.isDailyOfferPurchased());
        assertEquals("daily_purchased", shop.purchase(user, "daily", 1, null).status());
    }

    private User createUser() {
        User user = new User("shop-user", "hash", "nick", "s@example.com", Gender.MALE);
        UserProgressInitializer.initializeUserProgress(user);
        return user;
    }

    private static final class FixedRandom extends Random {
        private final int value;

        private FixedRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            return Math.min(value, Math.max(0, bound - 1));
        }
    }
}
