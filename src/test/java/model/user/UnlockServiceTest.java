package model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnlockServiceTest {

    @Test
    void unlockPlantPublishesNewsOnce() {
        User user = createUser();
        UnlockService unlockService = new UnlockService();

        assertTrue(unlockService.unlockPlant(user, "Cherry Bomb"));
        assertTrue(user.getPlantProgress().isOwned("Cherry Bomb"));
        assertEquals(1, user.getNewsItems().size());
        assertTrue(user.hasUnreadNews());
        assertEquals(NewsType.PLANT_UNLOCKED, user.getNewsItems().get(0).getType());
        assertEquals("Cherry Bomb", user.getNewsItems().get(0).getSubject());

        assertFalse(unlockService.unlockPlant(user, "Cherry Bomb"));
        assertEquals(1, user.getNewsItems().size());
    }

    @Test
    void unlockPlantIgnoresBlankOrNull() {
        User user = createUser();
        UnlockService unlockService = new UnlockService();

        assertFalse(unlockService.unlockPlant(user, null));
        assertFalse(unlockService.unlockPlant(user, "   "));
        assertTrue(user.getNewsItems().isEmpty());
    }

    @Test
    void unlockZombieLevelMinigamePublishOnce() {
        User user = createUser();
        UnlockService unlockService = new UnlockService();

        assertTrue(unlockService.unlockZombie(user, "Conehead"));
        assertTrue(unlockService.unlockLevel(user, "1-2"));
        assertTrue(unlockService.unlockMinigame(user, "Beghouled"));

        assertTrue(user.getUnlockedZombies().contains("Conehead"));
        assertTrue(user.getUnlockedLevels().contains("1-2"));
        assertTrue(user.getUnlockedMinigames().contains("Beghouled"));
        assertEquals(3, user.getNewsItems().size());

        assertFalse(unlockService.unlockZombie(user, "Conehead"));
        assertFalse(unlockService.unlockLevel(user, "1-2"));
        assertFalse(unlockService.unlockMinigame(user, "Beghouled"));
        assertEquals(3, user.getNewsItems().size());
    }

    @Test
    void starterPlantUnlockDoesNotPublish() {
        User user = createUser();
        UnlockService unlockService = new UnlockService();

        assertFalse(unlockService.unlockPlant(user, "Peashooter"));
        assertTrue(user.getNewsItems().isEmpty());
        assertTrue(user.getPlantProgress().isOwned("Peashooter"));
    }

    private static User createUser() {
        return new User("unlock-user", "hash", "nick", "unlock@example.com", Gender.MALE);
    }
}
