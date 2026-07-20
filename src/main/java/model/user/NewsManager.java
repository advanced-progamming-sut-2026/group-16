package model.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NewsManager {
    private static long nextId = 1;

    public NewsItem addNews(User user, NewsType type, String subject, String message) {
        NewsItem item = new NewsItem(type, subject, message, System.currentTimeMillis(), false);
        item.setId(nextId++);
        user.getNewsItems().add(item);
        return item;
    }

    public List<NewsItem> getUnread(User user) {
        List<NewsItem> unread = new ArrayList<>();
        for (NewsItem item : user.getNewsItems()) {
            if (!item.isRead()) {
                unread.add(item);
            }
        }
        unread.sort(Comparator.comparingLong(NewsItem::getCreatedAtMillis).reversed());
        return unread;
    }

    public List<NewsItem> getAll(User user) {
        List<NewsItem> all = new ArrayList<>(user.getNewsItems());
        all.sort(Comparator.comparingLong(NewsItem::getCreatedAtMillis).reversed());
        return all;
    }

    public void markAsRead(List<NewsItem> items) {
        for (NewsItem item : items) {
            item.setRead(true);
        }
    }

    public boolean hasUnread(User user) {
        for (NewsItem item : user.getNewsItems()) {
            if (!item.isRead()) {
                return true;
            }
        }
        return false;
    }

    public String format(NewsItem item) {
        return item.getMessage();
    }

    public List<String> formatAll(List<NewsItem> items) {
        List<String> lines = new ArrayList<>();
        for (NewsItem item : items) {
            lines.add(format(item));
        }
        return lines;
    }

    public void publishPlantUnlocked(User user, String plantName) {
        addNews(user, NewsType.PLANT_UNLOCKED, plantName,
                "New plant unlocked: " + plantName);
    }

    public void publishZombieUnlocked(User user, String zombieName) {
        addNews(user, NewsType.ZOMBIE_UNLOCKED, zombieName,
                "New zombie unlocked: " + zombieName);
    }

    public void publishLevelUnlocked(User user, String levelId) {
        addNews(user, NewsType.LEVEL_UNLOCKED, levelId,
                "New level unlocked: " + levelId);
    }

    public void publishMinigameUnlocked(User user, String minigameId) {
        addNews(user, NewsType.MINIGAME_UNLOCKED, minigameId,
                "New minigame unlocked: " + minigameId);
    }

    public static void syncNextIdFrom(List<NewsItem> items) {
        long maxId = 0;
        for (NewsItem item : items) {
            if (item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        if (maxId >= nextId) {
            nextId = maxId + 1;
        }
    }
}
