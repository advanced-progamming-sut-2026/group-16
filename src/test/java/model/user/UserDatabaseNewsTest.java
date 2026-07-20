package model.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDatabaseNewsTest {
    private static final Path DATABASE = Path.of("target", "news-progress-test.db");

    @BeforeAll
    static void configureDatabase() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void saveAndLoadNewsItemsPreservesReadState() {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "news-owner",
                "password-hash",
                "News Owner",
                "news-owner@example.com",
                Gender.MALE);
        database.registerUser(user);
        assertTrue(user.getId() > 0);

        NewsManager manager = new NewsManager();
        NewsItem first = manager.addNews(user, NewsType.SYSTEM, "a", "first message");
        NewsItem second = manager.addNews(user, NewsType.SYSTEM, "b", "second message");
        first.setRead(true);
        database.saveUserNews(user);

        User loaded = database.getUser("news-owner");
        assertNotNull(loaded);
        assertEquals(2, loaded.getNewsItems().size());
        assertTrue(loaded.hasUnreadNews());

        NewsItem loadedFirst = findBySubject(loaded, "a");
        NewsItem loadedSecond = findBySubject(loaded, "b");
        assertTrue(loadedFirst.isRead());
        assertEquals("first message", loadedFirst.getMessage());
        assertFalse(loadedSecond.isRead());
        assertEquals("second message", loadedSecond.getMessage());
        assertEquals(second.getId(), loadedSecond.getId());
    }

    @Test
    void saveAndLoadUnlockSets() {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "unlock-sets",
                "password-hash",
                "Unlock Sets",
                "unlock-sets@example.com",
                Gender.FEMALE);
        database.registerUser(user);

        UnlockService unlockService = new UnlockService();
        assertTrue(unlockService.unlockZombie(user, "Conehead"));
        assertTrue(unlockService.unlockLevel(user, "1-2"));
        assertTrue(unlockService.unlockMinigame(user, "Beghouled"));
        database.saveUserNews(user);

        User loaded = database.getUser("unlock-sets");
        assertNotNull(loaded);
        assertTrue(loaded.getUnlockedZombies().contains("Conehead"));
        assertTrue(loaded.getUnlockedLevels().contains("1-2"));
        assertTrue(loaded.getUnlockedMinigames().contains("Beghouled"));
        assertEquals(3, loaded.getNewsItems().size());
        assertTrue(loaded.hasUnreadNews());
        assertEquals(NewsType.ZOMBIE_UNLOCKED, findBySubject(loaded, "Conehead").getType());
        assertEquals(NewsType.LEVEL_UNLOCKED, findBySubject(loaded, "1-2").getType());
        assertEquals(NewsType.MINIGAME_UNLOCKED, findBySubject(loaded, "Beghouled").getType());
    }

    @Test
    void unlockPlantPersistsPlantAndNews() {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "plant-news",
                "password-hash",
                "Plant News",
                "plant-news@example.com",
                Gender.MALE);
        database.registerUser(user);

        UnlockService unlockService = new UnlockService();
        assertTrue(unlockService.unlockPlant(user, "Cherry Bomb"));
        database.savePlantProgress(user);
        database.saveUserNews(user);

        User loaded = database.getUser("plant-news");
        assertNotNull(loaded);
        assertTrue(loaded.getPlantProgress().isOwned("Cherry Bomb"));
        assertEquals(1, loaded.getNewsItems().size());
        NewsItem news = loaded.getNewsItems().get(0);
        assertFalse(news.isRead());
        assertEquals(NewsType.PLANT_UNLOCKED, news.getType());
        assertEquals("Cherry Bomb", news.getSubject());
        assertEquals("New plant unlocked: Cherry Bomb", news.getMessage());
        assertTrue(loaded.hasUnreadNews());
    }

    private static NewsItem findBySubject(User user, String subject) {
        return user.getNewsItems().stream()
                .filter(item -> subject.equals(item.getSubject()))
                .findFirst()
                .orElseThrow();
    }
}
