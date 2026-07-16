package model.greenhouse;

import model.definition.PlantRegistry;
import model.user.Gender;
import model.user.GreenhousePot;
import model.user.User;
import model.user.UserProgressInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseServiceTest {
    private PlantRegistry plantRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
    }

    @Test
    void plantRejectsLockedOrOccupiedPot() {
        User user = createUser();
        GreenhouseService service = new GreenhouseService(plantRegistry, new FixedRandom(true, 0));

        assertEquals("locked", service.plant(user, 1, 2).status());
        assertEquals("success", service.plant(user, 1, 1).status());
        assertEquals("occupied", service.plant(user, 1, 1).status());
    }

    @Test
    void plantCanChooseMarigoldOrUnlockedPlantFoodPlant() {
        User user = createUser();

        GreenhouseService marigoldService = new GreenhouseService(plantRegistry, new FixedRandom(true, 0));
        GreenhouseService.PlantingResult marigold = marigoldService.plant(user, 1, 1);
        assertEquals(GreenhousePot.MARIGOLD, marigold.plantType());

        User secondUser = createUser();
        GreenhouseService plantService = new GreenhouseService(plantRegistry, new FixedRandom(false, 0));
        GreenhouseService.PlantingResult plant = plantService.plant(secondUser, 1, 1);
        assertFalse(GreenhousePot.MARIGOLD.equals(plant.plantType()));
        assertTrue(secondUser.getPlantProgress().isOwned(plant.plantType()));
    }

    @Test
    void collectMarigoldAddsCoins() {
        User user = createUser();
        GreenhousePot pot = user.getPotAt(1, 1);
        pot.plant(GreenhousePot.MARIGOLD, true, System.currentTimeMillis() - GreenhouseService.MARIGOLD_GROWTH_MILLIS);

        GreenhouseService service = new GreenhouseService(plantRegistry, new FixedRandom(true, 0));
        GreenhouseService.CollectResult result = service.collect(user, 1, 1);

        assertEquals("success", result.status());
        assertEquals(500, user.getCoins());
        assertTrue(pot.isEmpty());
    }

    @Test
    void collectPlantStoresOnlyOneBoostPerType() {
        User user = createUser();
        GreenhousePot firstPot = user.getPotAt(1, 1);
        GreenhousePot secondPot = user.getPotAt(2, 1);
        long plantedAt = System.currentTimeMillis() - GreenhouseService.PLANT_GROWTH_MILLIS;
        firstPot.plant("Sunflower", false, plantedAt);
        secondPot.plant("Sunflower", false, plantedAt);

        GreenhouseService service = new GreenhouseService(plantRegistry, new FixedRandom(false, 0));
        assertEquals("success", service.collect(user, 1, 1).status());
        assertTrue(user.hasStoredBoost("Sunflower"));

        String reward = service.collect(user, 2, 1).reward();
        assertTrue(reward.contains("already stored"));
        assertEquals(1, user.getStoredBoosts().size());
    }

    @Test
    void growRoundsRemainingHoursUp() {
        User user = createUser();
        user.setDiamonds(10);
        GreenhousePot pot = user.getPotAt(1, 1);
        long plantedAt = System.currentTimeMillis() - (GreenhouseService.PLANT_GROWTH_MILLIS - (long) (2.5 * 60 * 60 * 1000));
        pot.plant("Sunflower", false, plantedAt);

        GreenhouseService service = new GreenhouseService(plantRegistry, new FixedRandom(false, 0));
        GreenhouseService.GrowResult result = service.grow(user, 1, 1);

        assertEquals("success", result.status());
        assertEquals(3, result.diamondsSpent());
        assertEquals(7, user.getDiamonds());
    }

    @Test
    void growRejectsReadyPlant() {
        User user = createUser();
        GreenhousePot pot = user.getPotAt(1, 1);
        pot.plant(GreenhousePot.MARIGOLD, true, System.currentTimeMillis() - GreenhouseService.MARIGOLD_GROWTH_MILLIS);

        GreenhouseService service = new GreenhouseService(plantRegistry, new FixedRandom(true, 0));
        assertEquals("already_ready", service.grow(user, 1, 1).status());
    }

    private User createUser() {
        User user = new User("greenhouse-user", "hash", "nick", "g@example.com", Gender.MALE);
        UserProgressInitializer.initializeUserProgress(user);
        return user;
    }

    private static final class FixedRandom extends Random {
        private final boolean nextBoolean;
        private final int nextInt;

        private FixedRandom(boolean nextBoolean, int nextInt) {
            this.nextBoolean = nextBoolean;
            this.nextInt = nextInt;
        }

        @Override
        public boolean nextBoolean() {
            return nextBoolean;
        }

        @Override
        public int nextInt(int bound) {
            return Math.min(nextInt, Math.max(0, bound - 1));
        }
    }
}
