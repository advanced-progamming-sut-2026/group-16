package io.github.finalwave.model.user;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewsManagerTest {

    @Test
    void addNewsCreatesUnreadItem() {
        User user = createUser();
        NewsManager manager = new NewsManager();

        NewsItem item = manager.addNews(user, NewsType.SYSTEM, "subject", "hello");

        assertFalse(item.isRead());
        assertTrue(manager.hasUnread(user));
        assertTrue(user.hasUnreadNews());
        assertEquals(1, user.getNewsItems().size());
    }

    @Test
    void getUnreadExcludesReadItems() {
        User user = createUser();
        NewsManager manager = new NewsManager();
        NewsItem first = manager.addNews(user, NewsType.SYSTEM, "a", "first");
        NewsItem second = manager.addNews(user, NewsType.SYSTEM, "b", "second");
        first.setRead(true);

        List<NewsItem> unread = manager.getUnread(user);

        assertEquals(1, unread.size());
        assertEquals(second.getId(), unread.get(0).getId());
    }

    @Test
    void markAsReadClearsUnreadAndHasUnread() {
        User user = createUser();
        NewsManager manager = new NewsManager();
        manager.addNews(user, NewsType.SYSTEM, "a", "first");
        manager.addNews(user, NewsType.SYSTEM, "b", "second");

        List<NewsItem> unread = manager.getUnread(user);
        manager.markAsRead(unread);

        assertTrue(manager.getUnread(user).isEmpty());
        assertFalse(manager.hasUnread(user));
        assertFalse(user.hasUnreadNews());
        assertEquals(2, manager.getAll(user).size());
    }

    @Test
    void getAllAndGetUnreadSortNewestFirst() {
        User user = createUser();
        NewsManager manager = new NewsManager();
        NewsItem older = manager.addNews(user, NewsType.SYSTEM, "old", "older");
        NewsItem newer = manager.addNews(user, NewsType.SYSTEM, "new", "newer");
        older.setCreatedAtMillis(1_000L);
        newer.setCreatedAtMillis(2_000L);

        List<NewsItem> all = manager.getAll(user);
        List<NewsItem> unread = manager.getUnread(user);

        assertEquals(newer.getId(), all.get(0).getId());
        assertEquals(older.getId(), all.get(1).getId());
        assertEquals(newer.getId(), unread.get(0).getId());
        assertEquals(older.getId(), unread.get(1).getId());
    }

    @Test
    void publishHelpersUseExpectedTypesAndMessages() {
        User user = createUser();
        NewsManager manager = new NewsManager();

        manager.publishPlantUnlocked(user, "Cherry Bomb");
        manager.publishZombieUnlocked(user, "Conehead");
        manager.publishLevelUnlocked(user, "1-2");
        manager.publishMinigameUnlocked(user, "Beghouled");

        List<NewsItem> all = manager.getAll(user);
        assertEquals(4, all.size());

        NewsItem plant = findBySubject(user, "Cherry Bomb");
        assertEquals(NewsType.PLANT_UNLOCKED, plant.getType());
        assertEquals("New plant unlocked: Cherry Bomb", plant.getMessage());

        NewsItem zombie = findBySubject(user, "Conehead");
        assertEquals(NewsType.ZOMBIE_UNLOCKED, zombie.getType());
        assertEquals("New zombie unlocked: Conehead", zombie.getMessage());

        NewsItem level = findBySubject(user, "1-2");
        assertEquals(NewsType.LEVEL_UNLOCKED, level.getType());
        assertEquals("New level unlocked: 1-2", level.getMessage());

        NewsItem minigame = findBySubject(user, "Beghouled");
        assertEquals(NewsType.MINIGAME_UNLOCKED, minigame.getType());
        assertEquals("New minigame unlocked: Beghouled", minigame.getMessage());
    }

    @Test
    void formatAllReturnsMessages() {
        User user = createUser();
        NewsManager manager = new NewsManager();
        NewsItem first = manager.addNews(user, NewsType.SYSTEM, "a", "alpha");
        NewsItem second = manager.addNews(user, NewsType.SYSTEM, "b", "beta");

        List<String> lines = manager.formatAll(List.of(first, second));

        assertEquals(List.of("alpha", "beta"), lines);
    }

    private static NewsItem findBySubject(User user, String subject) {
        return user.getNewsItems().stream()
                .filter(item -> subject.equals(item.getSubject()))
                .findFirst()
                .orElseThrow();
    }

    private static User createUser() {
        return new User("news-user", "hash", "nick", "news@example.com", Gender.MALE);
    }
}
