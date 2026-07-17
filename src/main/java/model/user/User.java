package model.user;

import model.collection.PlayerPlantProgress;
import model.quest.QuestTracker;
import util.HashUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private Gender gender;
    private int securityQuestionId;
    private String securityAnswerHash;
    private PlayerPlantProgress plantProgress;
    private int coins;
    private int diamonds;
    private int plantFood;
    private int difficultyLevel = 3;
    private final ChapterProgress chapterProgress = new ChapterProgress();
    private final List<GreenhousePot> greenhousePots = new ArrayList<>();
    private final Set<String> storedBoosts = new LinkedHashSet<>();
    private String dailyOfferPlant;
    private LocalDate dailyOfferDate;
    private boolean dailyOfferPurchased;
    private QuestTracker questTracker;

    public User() {

    }

    public User(String username, String passwordHash, String nickname, String email, Gender gender) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean authenticate(String password) {
        return passwordHash != null && passwordHash.equals(HashUtil.hashSHA256(password));
    }

    public boolean validateSecurityAnswer(String answer) {
        return securityAnswerHash != null && securityAnswerHash.equals(HashUtil.hashSHA256(answer.trim()));
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getSecurityQuestionId() {
        return securityQuestionId;
    }

    public void setSecurityQuestionId(int securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    public PlayerPlantProgress getPlantProgress() {
        if (plantProgress == null) {
            plantProgress = new PlayerPlantProgress();
        }
        return plantProgress;
    }

    public void setPlantProgress(PlayerPlantProgress plantProgress) {
        this.plantProgress = plantProgress;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoins(int amount) {
        if (amount > 0) {
            coins += amount;
        }
    }

    public boolean spendCoins(int amount) {
        if (amount < 0 || coins < amount) {
            return false;
        }
        coins -= amount;
        return true;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = Math.max(0, diamonds);
    }

    public void addDiamonds(int amount) {
        if (amount > 0) {
            diamonds += amount;
        }
    }

    public boolean spendDiamonds(int amount) {
        if (amount < 0 || diamonds < amount) {
            return false;
        }
        diamonds -= amount;
        return true;
    }

    public int getPlantFood() {
        return plantFood;
    }

    public void setPlantFood(int plantFood) {
        this.plantFood = Math.max(0, plantFood);
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = Math.max(1, Math.min(5, difficultyLevel));
    }

    public ChapterProgress getChapterProgress() {
        return chapterProgress;
    }

    public QuestTracker getQuestTracker() {
        return questTracker;
    }

    public void setQuestTracker(QuestTracker questTracker) {
        this.questTracker = questTracker;
    }

    public QuestTracker ensureQuestTracker() {
        if (questTracker == null) {
            questTracker = model.quest.QuestService.createTrackerFor(this, null);
        }
        return questTracker;
    }

    public List<GreenhousePot> getGreenhousePots() {
        return greenhousePots;
    }

    public GreenhousePot getPotAt(int x, int y) {
        for (GreenhousePot pot : greenhousePots) {
            if (pot.getX() == x && pot.getY() == y) {
                return pot;
            }
        }
        return null;
    }

    public int countUnlockedPots() {
        int count = 0;
        for (GreenhousePot pot : greenhousePots) {
            if (!pot.isLocked()) {
                count++;
            }
        }
        return count;
    }

    public GreenhousePot findNextLockedPot() {
        for (GreenhousePot pot : greenhousePots) {
            if (pot.isLocked()) {
                return pot;
            }
        }
        return null;
    }

    public Set<String> getStoredBoosts() {
        return storedBoosts;
    }

    public boolean hasStoredBoost(String plantType) {
        return storedBoosts.contains(plantType);
    }

    public String getDailyOfferPlant() {
        return dailyOfferPlant;
    }

    public void setDailyOfferPlant(String dailyOfferPlant) {
        this.dailyOfferPlant = dailyOfferPlant;
    }

    public LocalDate getDailyOfferDate() {
        return dailyOfferDate;
    }

    public void setDailyOfferDate(LocalDate dailyOfferDate) {
        this.dailyOfferDate = dailyOfferDate;
    }

    public boolean isDailyOfferPurchased() {
        return dailyOfferPurchased;
    }

    public void setDailyOfferPurchased(boolean dailyOfferPurchased) {
        this.dailyOfferPurchased = dailyOfferPurchased;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' + ", passwordHash='" + passwordHash + '\'' + ", nickname='" + nickname + '\'' + ", email='" + email + '\'' + ", gender=" + gender + '}';
    }
}
