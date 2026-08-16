package io.github.finalwave.model.shop;

import io.github.finalwave.model.greenhouse.GreenhouseLayout;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserProgressInitializer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopManagerTest {
    @Test
    void buyPotRequiresDiamondsAndUnlocksSlots() {
        User poor = createUser();
        ShopManager shop = new ShopManager(new FixedRandom(0));
        assertEquals("insufficient_diamonds", shop.purchase(poor, "pot", 1, null).status());

        User rich = createUser();
        rich.setDiamonds(GreenhouseLayout.POT_UNLOCK_COST_DIAMONDS * 2);
        assertEquals("success", shop.purchase(rich, "pot", 2, null).status());
        assertEquals(6, rich.countUnlockedPots());
        assertEquals(0, rich.getDiamonds());
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

    @Test
    void offersListDailyFirstThenPermanentItems() {
        User user = createUser();
        ShopManager shop = new ShopManager(new FixedRandom(1));
        List<ShopOffer> offers = shop.offers(user);

        assertEquals(6, offers.size());
        ShopOffer daily = offers.get(0);
        assertEquals("daily", daily.id());
        assertEquals("Sunflower", daily.previewPlant());
        assertFalse(daily.soldOut());
        assertNotNull(daily.remainingLabel());
        assertTrue(daily.remainingLabel().contains("remaining"));
        assertEquals("1600 coins", daily.priceLabel());

        assertEquals("pot", offers.get(1).id());
        assertEquals("plant_food", offers.get(2).id());
        assertEquals("seed_random", offers.get(3).id());
        assertEquals("seed_selective", offers.get(4).id());
        assertTrue(offers.get(4).requiresPlantType());
        assertEquals("gem_to_coin", offers.get(5).id());
        assertEquals("5 diamonds", offers.get(5).priceLabel());
        assertEquals(ShopTab.SEEDS, daily.tab());
        assertEquals(ShopTab.GARDEN, offers.get(1).tab());
        assertEquals(ShopTab.SEEDS, offers.get(3).tab());
        assertEquals(ShopTab.COINS, offers.get(5).tab());
    }

    @Test
    void coinPacksUseExistingGemToCoinRate() {
        User user = createUser();
        ShopManager shop = new ShopManager(new FixedRandom(0));
        List<ShopOffer> packs = shop.offers(user, ShopTab.COINS);
        assertEquals(8, packs.size());
        assertEquals("gem_to_coin", packs.get(0).id());
        assertEquals(1, packs.get(0).purchaseCount());
        assertEquals(5, packs.get(0).price());
        assertEquals("x500", packs.get(0).quantityLabel());
        assertEquals(10, packs.get(3).purchaseCount());
        assertEquals(50, packs.get(3).price());
        assertEquals("x5,000", packs.get(3).quantityLabel());
    }

    @Test
    void seedTabContainsDailyAndSeedPackets() {
        User user = createUser();
        ShopManager shop = new ShopManager(new FixedRandom(1));
        List<ShopOffer> seeds = shop.offers(user, ShopTab.SEEDS);
        assertEquals(3, seeds.size());
        assertEquals("daily", seeds.get(0).id());
        assertEquals("seed_random", seeds.get(1).id());
        assertEquals("seed_selective", seeds.get(2).id());
        assertTrue(seeds.get(2).requiresPlantType());
    }

    @Test
    void dailyOfferSoldOutAfterPurchase() {
        User user = createUser();
        user.setCoins(2000);
        ShopManager shop = new ShopManager(new FixedRandom(1));
        shop.offers(user);
        assertEquals("success", shop.purchase(user, "daily", 1, null).status());

        ShopOffer daily = shop.offers(user).get(0);
        assertEquals("daily", daily.id());
        assertTrue(daily.soldOut());
    }

    @Test
    void potAndPlantFoodSoldOutAtCapacity() {
        User user = createUser();
        user.getGreenhousePots().forEach(pot -> pot.setLocked(false));
        user.setPlantFood(3);
        ShopManager shop = new ShopManager(new FixedRandom(0));
        List<ShopOffer> offers = shop.offers(user);

        ShopOffer pot = offers.stream().filter(offer -> "pot".equals(offer.id())).findFirst().orElseThrow();
        ShopOffer plantFood = offers.stream().filter(offer -> "plant_food".equals(offer.id())).findFirst().orElseThrow();
        assertTrue(pot.soldOut());
        assertTrue(plantFood.soldOut());
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
