package model.user;

import model.minigame.MiniGameId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
