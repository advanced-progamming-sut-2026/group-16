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
            applyReward(user, quest.getReward());
            quest.markRewardClaimed();
            UserDatabase.getInstance().saveQuestProgress(user);
            if (onCompletedExtra != null) {
                onCompletedExtra.accept(quest);
            }
        });
        tracker.setQuests(QuestFactory.createAllQuests());
        user.setQuestTracker(tracker);
        UserDatabase.getInstance().loadQuestProgress(user, tracker);
        refreshDailyQuestsIfNeeded(user, tracker);
        return tracker;
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
        if (seedPlant != null && reward.getSeedPacketCount() > 0) {
            if ("ANY".equalsIgnoreCase(seedPlant)) {
                seedPlant = pickRandomUnlockedPlant(user, registry);
            }
            if (seedPlant != null) {
                try {
                    collection.addSeedPackets(seedPlant, reward.getSeedPacketCount());
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
        UserDatabase.getInstance().savePlantProgress(user);
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
}
