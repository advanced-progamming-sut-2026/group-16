package io.github.finalwave.model.quest;

import io.github.finalwave.model.App;
import io.github.finalwave.model.collection.PlantCollection;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.quest.reward.QuestReward;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public final class QuestService {

    private QuestService() {
    }

    public static QuestTracker createTrackerFor(User user, Consumer<Quest> onCompletedExtra) {
        QuestTracker tracker = new QuestTracker(quest -> {
            UserDatabase.getInstance().saveQuestProgress(user);
            if (onCompletedExtra != null) {
                onCompletedExtra.accept(quest);
            }
        });
        tracker.setQuests(QuestFactory.createAllQuests(questSeed(user)));
        user.setQuestTracker(tracker);
        UserDatabase.getInstance().loadQuestProgress(user, tracker);
        refreshDailyQuestsIfNeeded(user, tracker);
        return tracker;
    }

    public static boolean claimReward(User user, Quest quest) {
        if (user == null || quest == null || !quest.isCompleted() || quest.isRewardClaimed()) {
            return false;
        }
        applyReward(user, quest.getReward());
        quest.markRewardClaimed();
        UserDatabase.getInstance().saveQuestProgress(user);
        return true;
    }

    public static int claimAll(User user, List<Quest> quests) {
        if (user == null || quests == null || quests.isEmpty()) {
            return 0;
        }
        int claimed = 0;
        for (Quest quest : quests) {
            if (claimReward(user, quest)) {
                claimed++;
            }
        }
        return claimed;
    }

    public static void refreshDailyQuestsIfNeeded(User user, QuestTracker tracker) {
        if (user == null || tracker == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate lastDay = user.getQuestDay();
        if (today.equals(lastDay)) {
            return;
        }
        tracker.resetDailyQuests();
        user.setQuestDay(today);
        UserDatabase.getInstance().saveQuestProgress(user);
        UserDatabase.getInstance().saveUserWallet(user);
    }

    public static void applyReward(User user, QuestReward reward) {
        if (user == null || reward == null) {
            return;
        }
        if (reward.getCoins() > 0) {
            user.addCoins(reward.getCoins());
        }
        if (reward.getDiamonds() > 0) {
            user.addDiamonds(reward.getDiamonds());
        }

        PlantRegistry registry = App.getInstance().getPlantRegistry();
        PlantCollection collection = new PlantCollection(registry, user.getPlantProgress(), user.getCoins());

        String seedPlant = reward.getSeedPacketPlantId();
        String seedPlantSaved = null;
        if (seedPlant != null && reward.getSeedPacketCount() > 0) {
            if ("ANY".equalsIgnoreCase(seedPlant)) {
                seedPlant = pickRandomUnlockedPlant(user, registry);
            }
            if (seedPlant != null) {
                try {
                    collection.addSeedPackets(seedPlant, reward.getSeedPacketCount());
                    seedPlantSaved = seedPlant;
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        UnlockService unlockService = new UnlockService();
        String unlock = reward.getUnlockTargetId();
        if (unlock != null) {
            if ("RANDOM_PLANT".equals(unlock)) {
                unlock = pickRandomLockedPlant(user, registry);
            }
            if (unlock != null) {
                PlantDefinition def = registry.getDefinition(unlock);
                if (def != null) {
                    unlockService.unlockPlant(user, def.getName());
                }
            }
        }

        user.setCoins(Math.max(user.getCoins(), collection.getCoins()));
        UserDatabase.getInstance().saveUserWallet(user);
        if (unlock != null) {
            UserDatabase.getInstance().savePlant(user, unlock);
        }
        if (seedPlantSaved != null && !seedPlantSaved.equals(unlock)) {
            UserDatabase.getInstance().savePlant(user, seedPlantSaved);
        }
        UserDatabase.getInstance().saveUserNews(user);
    }

    private static String pickRandomUnlockedPlant(User user, PlantRegistry registry) {
        List<String> unlocked = user.getPlantProgress().getUnlockedPlantNames();
        if (unlocked.isEmpty()) {
            return null;
        }
        return unlocked.get(new Random().nextInt(unlocked.size()));
    }

    private static String pickRandomLockedPlant(User user, PlantRegistry registry) {
        List<String> locked = registry.getAllDefinitions().stream()
                .map(PlantDefinition::getName)
                .filter(name -> !user.getPlantProgress().isOwned(name))
                .toList();
        if (locked.isEmpty()) {
            return null;
        }
        return locked.get(new Random().nextInt(locked.size()));
    }

    private static long questSeed(User user) {
        if (user.getId() != 0L) {
            return user.getId();
        }
        String username = user.getUsername();
        return username == null ? 0L : username.hashCode();
    }
}
