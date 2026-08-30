package io.github.finalwave.network.auth;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public final class LoginOkPayload {
    private long userId;
    private String username;
    private String nickname;
    private String email;
    private String gender;
    private int coins;
    private int diamonds;
    private int plantFood;
    private String dailyOfferPlant;
    private String dailyOfferDate;
    private boolean dailyOfferPurchased;
    private int gamesPlayed;
    private String questDay;
    private List<PlantEntry> plants = new ArrayList<>();
    private List<String> storedBoosts = new ArrayList<>();
    private List<GreenhousePotEntry> greenhousePots = new ArrayList<>();
    private List<String> unlockedZombies = new ArrayList<>();
    private List<String> unlockedLevels = new ArrayList<>();
    private List<String> unlockedMinigames = new ArrayList<>();
    private AdventureEntry adventure;
    private List<MinigameStageEntry> minigameStages = new ArrayList<>();
    private SettingsEntry settings;
    private ScoreGameEntry scoreGame;
    private List<QuestProgressEntry> questProgress = new ArrayList<>();
    private JsonNode matchSave;

    public LoginOkPayload() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public int getPlantFood() {
        return plantFood;
    }

    public void setPlantFood(int plantFood) {
        this.plantFood = plantFood;
    }

    public String getDailyOfferPlant() {
        return dailyOfferPlant;
    }

    public void setDailyOfferPlant(String dailyOfferPlant) {
        this.dailyOfferPlant = dailyOfferPlant;
    }

    public String getDailyOfferDate() {
        return dailyOfferDate;
    }

    public void setDailyOfferDate(String dailyOfferDate) {
        this.dailyOfferDate = dailyOfferDate;
    }

    public boolean isDailyOfferPurchased() {
        return dailyOfferPurchased;
    }

    public void setDailyOfferPurchased(boolean dailyOfferPurchased) {
        this.dailyOfferPurchased = dailyOfferPurchased;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getQuestDay() {
        return questDay;
    }

    public void setQuestDay(String questDay) {
        this.questDay = questDay;
    }

    public List<PlantEntry> getPlants() {
        return plants;
    }

    public void setPlants(List<PlantEntry> plants) {
        this.plants = plants;
    }

    public List<String> getStoredBoosts() {
        return storedBoosts;
    }

    public void setStoredBoosts(List<String> storedBoosts) {
        this.storedBoosts = storedBoosts;
    }

    public List<GreenhousePotEntry> getGreenhousePots() {
        return greenhousePots;
    }

    public void setGreenhousePots(List<GreenhousePotEntry> greenhousePots) {
        this.greenhousePots = greenhousePots;
    }

    public List<String> getUnlockedZombies() {
        return unlockedZombies;
    }

    public void setUnlockedZombies(List<String> unlockedZombies) {
        this.unlockedZombies = unlockedZombies;
    }

    public List<String> getUnlockedLevels() {
        return unlockedLevels;
    }

    public void setUnlockedLevels(List<String> unlockedLevels) {
        this.unlockedLevels = unlockedLevels;
    }

    public List<String> getUnlockedMinigames() {
        return unlockedMinigames;
    }

    public void setUnlockedMinigames(List<String> unlockedMinigames) {
        this.unlockedMinigames = unlockedMinigames;
    }

    public AdventureEntry getAdventure() {
        return adventure;
    }

    public void setAdventure(AdventureEntry adventure) {
        this.adventure = adventure;
    }

    public List<MinigameStageEntry> getMinigameStages() {
        return minigameStages;
    }

    public void setMinigameStages(List<MinigameStageEntry> minigameStages) {
        this.minigameStages = minigameStages;
    }

    public SettingsEntry getSettings() {
        return settings;
    }

    public void setSettings(SettingsEntry settings) {
        this.settings = settings;
    }

    public ScoreGameEntry getScoreGame() {
        return scoreGame;
    }

    public void setScoreGame(ScoreGameEntry scoreGame) {
        this.scoreGame = scoreGame;
    }

    public List<QuestProgressEntry> getQuestProgress() {
        return questProgress;
    }

    public void setQuestProgress(List<QuestProgressEntry> questProgress) {
        this.questProgress = questProgress;
    }

    public JsonNode getMatchSave() {
        return matchSave;
    }

    public void setMatchSave(JsonNode matchSave) {
        this.matchSave = matchSave;
    }

    public static final class PlantEntry {
        private String plantName;
        private int level;
        private boolean unlocked;
        private int seedPackets;

        public PlantEntry() {
        }

        public PlantEntry(String plantName, int level, boolean unlocked, int seedPackets) {
            this.plantName = plantName;
            this.level = level;
            this.unlocked = unlocked;
            this.seedPackets = seedPackets;
        }

        public String getPlantName() {
            return plantName;
        }

        public void setPlantName(String plantName) {
            this.plantName = plantName;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public void setUnlocked(boolean unlocked) {
            this.unlocked = unlocked;
        }

        public int getSeedPackets() {
            return seedPackets;
        }

        public void setSeedPackets(int seedPackets) {
            this.seedPackets = seedPackets;
        }
    }

    public static final class GreenhousePotEntry {
        private int x;
        private int y;
        private boolean locked;
        private String plantType;
        private long plantedAtMillis;
        private boolean marigold;

        public GreenhousePotEntry() {
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public String getPlantType() {
            return plantType;
        }

        public void setPlantType(String plantType) {
            this.plantType = plantType;
        }

        public long getPlantedAtMillis() {
            return plantedAtMillis;
        }

        public void setPlantedAtMillis(long plantedAtMillis) {
            this.plantedAtMillis = plantedAtMillis;
        }

        public boolean isMarigold() {
            return marigold;
        }

        public void setMarigold(boolean marigold) {
            this.marigold = marigold;
        }
    }

    public static final class AdventureEntry {
        private String unlockedChapter;
        private int difficultyLevel;
        private String completedLevels;

        public AdventureEntry() {
        }

        public String getUnlockedChapter() {
            return unlockedChapter;
        }

        public void setUnlockedChapter(String unlockedChapter) {
            this.unlockedChapter = unlockedChapter;
        }

        public int getDifficultyLevel() {
            return difficultyLevel;
        }

        public void setDifficultyLevel(int difficultyLevel) {
            this.difficultyLevel = difficultyLevel;
        }

        public String getCompletedLevels() {
            return completedLevels;
        }

        public void setCompletedLevels(String completedLevels) {
            this.completedLevels = completedLevels;
        }
    }

    public static final class MinigameStageEntry {
        private String minigameId;
        private int stageIndex;

        public MinigameStageEntry() {
        }

        public MinigameStageEntry(String minigameId, int stageIndex) {
            this.minigameId = minigameId;
            this.stageIndex = stageIndex;
        }

        public String getMinigameId() {
            return minigameId;
        }

        public void setMinigameId(String minigameId) {
            this.minigameId = minigameId;
        }

        public int getStageIndex() {
            return stageIndex;
        }

        public void setStageIndex(int stageIndex) {
            this.stageIndex = stageIndex;
        }
    }

    public static final class SettingsEntry {
        private int gameSpeed;
        private boolean showLawnGrid;
        private boolean debugMode;

        public SettingsEntry() {
        }

        public int getGameSpeed() {
            return gameSpeed;
        }

        public void setGameSpeed(int gameSpeed) {
            this.gameSpeed = gameSpeed;
        }

        public boolean isShowLawnGrid() {
            return showLawnGrid;
        }

        public void setShowLawnGrid(boolean showLawnGrid) {
            this.showLawnGrid = showLawnGrid;
        }

        public boolean isDebugMode() {
            return debugMode;
        }

        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
        }
    }

    public static final class ScoreGameEntry {
        private boolean hasPlayed;
        private int bestMeowPoint;

        public ScoreGameEntry() {
        }

        public boolean isHasPlayed() {
            return hasPlayed;
        }

        public void setHasPlayed(boolean hasPlayed) {
            this.hasPlayed = hasPlayed;
        }

        public int getBestMeowPoint() {
            return bestMeowPoint;
        }

        public void setBestMeowPoint(int bestMeowPoint) {
            this.bestMeowPoint = bestMeowPoint;
        }
    }

    public static final class QuestProgressEntry {
        private String questId;
        private boolean completed;
        private boolean claimed;
        private String progressBlob;

        public QuestProgressEntry() {
        }

        public String getQuestId() {
            return questId;
        }

        public void setQuestId(String questId) {
            this.questId = questId;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isClaimed() {
            return claimed;
        }

        public void setClaimed(boolean claimed) {
            this.claimed = claimed;
        }

        public String getProgressBlob() {
            return progressBlob;
        }

        public void setProgressBlob(String progressBlob) {
            this.progressBlob = progressBlob;
        }
    }
}
