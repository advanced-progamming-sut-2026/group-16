package io.github.finalwave.model.user;

import io.github.finalwave.model.minigame.MiniGameId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProgressInitializerTest {

    @Test
    void initializeSeedsStarterLevelAndFirstMinigameOnly() {
        User user = new User("init-user", "hash", "nick", "init@example.com", Gender.MALE);
        UserProgressInitializer.initializeUserProgress(user);

        assertEquals(1, user.getUnlockedMinigames().size());
        assertTrue(user.getUnlockedMinigames().contains(MiniGameId.VASE_BREAKER.getKey()));
        assertEquals(1, user.getUnlockedLevels().size());
        assertTrue(user.getUnlockedLevels().contains("1-1"));
        assertEquals(12, user.getGreenhousePots().size());
        assertFalse(user.getPotAt(1, 1).isLocked());
        assertTrue(user.getPotAt(1, 2).isLocked());
    }

    @Test
    void ensureGreenhousePotsFillsMissingSlotsWithoutWipingExisting() {
        User user = new User("partial-user", "hash", "nick", "p@example.com", Gender.MALE);
        user.getGreenhousePots().add(new GreenhousePot(1, 1, false));
        user.getPotAt(1, 1).plant(GreenhousePot.MARIGOLD, true, 10L);

        UserProgressInitializer.ensureGreenhousePots(user);

        assertEquals(12, user.getGreenhousePots().size());
        assertEquals(GreenhousePot.MARIGOLD, user.getPotAt(1, 1).getPlantType());
        assertFalse(user.getPotAt(1, 1).isLocked());
        assertTrue(user.getPotAt(2, 1).isEmpty());
    }
}
