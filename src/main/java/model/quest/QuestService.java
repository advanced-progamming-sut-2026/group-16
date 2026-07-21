package model.quest;

import model.App;
import model.collection.PlantCollection;
import model.definition.PlantRegistry;
import model.definition.plant.PlantDefinition;
import model.quest.reward.QuestReward;
import model.user.User;
import model.user.UserDatabase;

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
        return tracker;
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
                    // unknown plant id - skip seeds
                }
            }
        }

        String unlock = reward.getUnlockTargetId();
        if (unlock != null) {
            if ("RANDOM_PLANT".equals(unlock)) {
                unlock = pickRandomLockedPlant(user, registry);
            }
            if (unlock != null) {
                PlantDefinition def = registry.getDefinition(unlock);
                if (def != null) {
                    user.getPlantProgress().unlock(def.getName());
                }
            }
        }

        user.setCoins(Math.max(user.getCoins(), collection.getCoins()));
        UserDatabase.getInstance().saveUserWallet(user);
        UserDatabase.getInstance().savePlantProgress(user);
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
