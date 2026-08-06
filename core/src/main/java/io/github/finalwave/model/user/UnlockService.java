package io.github.finalwave.model.user;

public final class UnlockService {
    private final NewsManager newsManager = new NewsManager();

    public boolean unlockPlant(User user, String plantName) {
        if (user == null || plantName == null || plantName.isBlank()) {
            return false;
        }
        boolean newlyUnlocked = user.getPlantProgress().unlock(plantName);
        if (newlyUnlocked) {
            newsManager.publishPlantUnlocked(user, plantName);
        }
        return newlyUnlocked;
    }

    public boolean unlockZombie(User user, String zombieName) {
        if (user == null || zombieName == null || zombieName.isBlank()) {
            return false;
        }
        if (!user.getUnlockedZombies().add(zombieName)) {
            return false;
        }
        newsManager.publishZombieUnlocked(user, zombieName);
        return true;
    }

    public boolean unlockLevel(User user, String levelId) {
        if (user == null || levelId == null || levelId.isBlank()) {
            return false;
        }
        if (!user.getUnlockedLevels().add(levelId)) {
            return false;
        }
        newsManager.publishLevelUnlocked(user, levelId);
        return true;
    }

    public boolean unlockMinigame(User user, String minigameId) {
        if (user == null || minigameId == null || minigameId.isBlank()) {
            return false;
        }
        if (!user.getUnlockedMinigames().add(minigameId)) {
            return false;
        }
        newsManager.publishMinigameUnlocked(user, minigameId);
        return true;
    }
}
