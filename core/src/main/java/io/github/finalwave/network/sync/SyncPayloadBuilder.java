package io.github.finalwave.network.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.collection.OwnedPlant;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.AdventureProgressStore;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.NewsItem;
import io.github.finalwave.model.user.UnlockKind;
import io.github.finalwave.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SyncPayloadBuilder {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SyncPayloadBuilder() {
    }

    public static UpdateWalletPayload wallet(User user) {
        UpdateWalletPayload payload = new UpdateWalletPayload();
        payload.setCoins(user.getCoins());
        payload.setDiamonds(user.getDiamonds());
        payload.setPlantFood(user.getPlantFood());
        payload.setGamesPlayed(user.getGamesPlayed());
        payload.setQuestDay(user.getQuestDay() == null ? null : user.getQuestDay().toString());
        payload.setDailyOfferPlant(user.getDailyOfferPlant());
        payload.setDailyOfferDate(user.getDailyOfferDate() == null ? null : user.getDailyOfferDate().toString());
        payload.setDailyOfferPurchased(user.isDailyOfferPurchased());
        return payload;
    }

    public static UpdatePlantPayload plant(User user, String plantName) {
        OwnedPlant plant = user.getPlantProgress().getOwnedPlant(plantName).orElse(null);
        if (plant == null) {
            return null;
        }
        return new UpdatePlantPayload(
                plant.getPlantName(),
                plant.getLevel(),
                plant.isUnlocked(),
                plant.getSeedPackets()
        );
    }

    public static UpdateGreenhousePotPayload greenhousePot(GreenhousePot pot) {
        UpdateGreenhousePotPayload payload = new UpdateGreenhousePotPayload();
        payload.setX(pot.getX());
        payload.setY(pot.getY());
        payload.setLocked(pot.isLocked());
        payload.setPlantType(pot.getPlantType());
        payload.setPlantedAtMillis(pot.getPlantedAtMillis());
        payload.setMarigold(pot.isMarigold());
        return payload;
    }

    public static UpdateBoostsPayload boosts(User user) {
        UpdateBoostsPayload payload = new UpdateBoostsPayload();
        payload.setPlantTypes(new ArrayList<>(user.getStoredBoosts()));
        return payload;
    }

    public static UnlockContentPayload unlock(String kind, String name) {
        return new UnlockContentPayload(kind, name);
    }

    public static UpdateQuestProgressPayload questProgress(User user) {
        UpdateQuestProgressPayload payload = new UpdateQuestProgressPayload();
        List<UpdateQuestProgressPayload.QuestProgressRow> rows = new ArrayList<>();
        if (user.getQuestTracker() != null) {
            for (Quest quest : user.getQuestTracker().getQuests()) {
                UpdateQuestProgressPayload.QuestProgressRow row = new UpdateQuestProgressPayload.QuestProgressRow();
                row.setQuestId(quest.getId());
                row.setCompleted(quest.isCompleted());
                row.setClaimed(quest.isRewardClaimed());
                row.setProgressBlob(quest.exportProgressBlob());
                rows.add(row);
            }
        }
        payload.setRows(rows);
        return payload;
    }

    public static UpdateAdventurePayload adventure(User user) {
        UpdateAdventurePayload payload = new UpdateAdventurePayload();
        payload.setUnlockedChapter(user.getChapterProgress().getUnlockedChapter().getKey());
        payload.setDifficultyLevel(user.getDifficultyLevel());
        payload.setCompletedLevels(AdventureProgressStore.serializeCompleted(user.getChapterProgress()));
        return payload;
    }

    public static UpdateMinigameStagesPayload minigameStages(User user) {
        UpdateMinigameStagesPayload payload = new UpdateMinigameStagesPayload();
        List<UpdateMinigameStagesPayload.MinigameStageRow> rows = new ArrayList<>();
        for (Map.Entry<MiniGameId, Set<Integer>> entry : user.getMiniGameProgress().getAllCompletedStages().entrySet()) {
            for (Integer stageIndex : entry.getValue()) {
                UpdateMinigameStagesPayload.MinigameStageRow row = new UpdateMinigameStagesPayload.MinigameStageRow();
                row.setMinigameId(entry.getKey().getKey());
                row.setStageIndex(stageIndex);
                rows.add(row);
            }
        }
        payload.setRows(rows);
        return payload;
    }

    public static UpdateMatchSavePayload matchSave(MatchSaveSnapshot snapshot) {
        UpdateMatchSavePayload payload = new UpdateMatchSavePayload();
        payload.setSnapshot(MAPPER.valueToTree(snapshot));
        return payload;
    }

    public static UpdateNewsPayload news(User user) {
        UpdateNewsPayload payload = new UpdateNewsPayload();
        List<UpdateNewsPayload.NewsRow> rows = new ArrayList<>();
        for (NewsItem item : user.getNewsItems()) {
            UpdateNewsPayload.NewsRow row = new UpdateNewsPayload.NewsRow();
            row.setId(item.getId());
            row.setType(item.getType().name());
            row.setSubject(item.getSubject());
            row.setMessage(item.getMessage());
            row.setCreatedAtMillis(item.getCreatedAtMillis());
            row.setRead(item.isRead());
            rows.add(row);
        }
        payload.setRows(rows);
        return payload;
    }

    public static UpdateSettingsPayload settings(User user) {
        UpdateSettingsPayload payload = new UpdateSettingsPayload();
        payload.setGameSpeed(user.getGameSpeed());
        payload.setShowLawnGrid(user.isShowLawnGrid());
        payload.setDebugMode(user.isDebugMode());
        return payload;
    }

    public static UpdateScoreGamePayload scoreGame(User user) {
        UpdateScoreGamePayload payload = new UpdateScoreGamePayload();
        payload.setHasPlayed(user.hasPlayed());
        payload.setBestMeowPoint(user.getBestMeowPoint());
        return payload;
    }

    public static String unlockKindName(UnlockKind kind) {
        return switch (kind) {
            case ZOMBIES -> "zombies";
            case LEVELS -> "levels";
            case MINIGAMES -> "minigames";
        };
    }
}
