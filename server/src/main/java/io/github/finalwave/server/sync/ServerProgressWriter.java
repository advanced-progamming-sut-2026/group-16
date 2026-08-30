package io.github.finalwave.server.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.collection.OwnedPlant;
import io.github.finalwave.model.greenhouse.GreenhouseLayout;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.AdventureProgressStore;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.NewsItem;
import io.github.finalwave.model.user.NewsType;
import io.github.finalwave.model.user.QuestProgressStore;
import io.github.finalwave.model.user.UnlockKind;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.model.user.UserProgressStore;
import io.github.finalwave.network.sync.UpdateAdventurePayload;
import io.github.finalwave.network.sync.UpdateBoostsPayload;
import io.github.finalwave.network.sync.UpdateGreenhousePotPayload;
import io.github.finalwave.network.sync.UpdateMatchSavePayload;
import io.github.finalwave.network.sync.UpdateMinigameStagesPayload;
import io.github.finalwave.network.sync.UpdateNewsPayload;
import io.github.finalwave.network.sync.UpdatePlantPayload;
import io.github.finalwave.network.sync.UpdateQuestProgressPayload;
import io.github.finalwave.network.sync.UpdateScoreGamePayload;
import io.github.finalwave.network.sync.UpdateSettingsPayload;
import io.github.finalwave.network.sync.UpdateWalletPayload;
import io.github.finalwave.network.sync.UnlockContentPayload;
import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ServerProgressWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerDatabase database;

    public ServerProgressWriter(ServerDatabase database) {
        this.database = database;
    }

    public UpdateWalletPayload applyWallet(String username, UpdateWalletPayload payload) {
        User user = requireUser(username);
        UpdateWalletPayload clamped = clampWallet(payload);
        user.setCoins(clamped.getCoins());
        user.setDiamonds(clamped.getDiamonds());
        user.setPlantFood(clamped.getPlantFood());
        user.setGamesPlayed(clamped.getGamesPlayed());
        user.setDailyOfferPlant(clamped.getDailyOfferPlant());
        if (clamped.getDailyOfferDate() == null || clamped.getDailyOfferDate().isBlank()) {
            user.setDailyOfferDate(null);
        } else {
            user.setDailyOfferDate(LocalDate.parse(clamped.getDailyOfferDate()));
        }
        user.setDailyOfferPurchased(clamped.isDailyOfferPurchased());
        if (clamped.getQuestDay() == null || clamped.getQuestDay().isBlank()) {
            user.setQuestDay(null);
        } else {
            user.setQuestDay(LocalDate.parse(clamped.getQuestDay()));
        }
        database().saveUserWallet(user);
        return walletFrom(user);
    }

    public UpdatePlantPayload applyPlant(String username, UpdatePlantPayload payload) {
        User user = requireUser(username);
        if (payload.getPlantName() == null || payload.getPlantName().isBlank()) {
            throw new SyncValidationException("plantName required");
        }
        int level = Math.max(1, Math.min(4, payload.getLevel()));
        int seedPackets = Math.max(0, payload.getSeedPackets());
        OwnedPlant plant = new OwnedPlant(payload.getPlantName(), level, payload.isUnlocked(), seedPackets);
        database().savePlantEntry(user, plant);
        return new UpdatePlantPayload(
                plant.getPlantName(),
                plant.getLevel(),
                plant.isUnlocked(),
                plant.getSeedPackets()
        );
    }

    public UpdateGreenhousePotPayload applyGreenhousePot(String username, UpdateGreenhousePotPayload payload) {
        User user = requireUser(username);
        if (!GreenhouseLayout.isValid(payload.getX(), payload.getY())) {
            throw new SyncValidationException("invalid pot coordinates");
        }
        GreenhousePot pot = user.getPotAt(payload.getX(), payload.getY());
        if (pot == null) {
            pot = new GreenhousePot(payload.getX(), payload.getY(), payload.isLocked());
            user.getGreenhousePots().add(pot);
        }
        pot.setLocked(payload.isLocked());
        if (payload.getPlantType() == null || payload.getPlantType().isBlank()) {
            pot.clear();
        } else {
            pot.plant(payload.getPlantType(), payload.isMarigold(), Math.max(0L, payload.getPlantedAtMillis()));
        }
        database().saveGreenhousePot(user, pot);
        return potPayloadFrom(pot);
    }

    public UpdateBoostsPayload applyBoosts(String username, UpdateBoostsPayload payload) {
        User user = requireUser(username);
        Set<String> boosts = new LinkedHashSet<>();
        if (payload.getPlantTypes() != null) {
            for (String plantType : payload.getPlantTypes()) {
                if (plantType != null && !plantType.isBlank()) {
                    boosts.add(plantType);
                }
            }
        }
        user.getStoredBoosts().clear();
        user.getStoredBoosts().addAll(boosts);
        database().saveStoredBoosts(user);
        UpdateBoostsPayload ok = new UpdateBoostsPayload();
        ok.setPlantTypes(new ArrayList<>(user.getStoredBoosts()));
        return ok;
    }

    public UnlockContentPayload applyUnlock(String username, UnlockContentPayload payload) {
        User user = requireUser(username);
        UnlockKind kind = parseUnlockKind(payload.getKind());
        if (payload.getName() == null || payload.getName().isBlank()) {
            throw new SyncValidationException("unlock name required");
        }
        switch (kind) {
            case ZOMBIES -> user.getUnlockedZombies().add(payload.getName());
            case LEVELS -> user.getUnlockedLevels().add(payload.getName());
            case MINIGAMES -> user.getUnlockedMinigames().add(payload.getName());
        }
        database().saveUnlock(user, kind, payload.getName());
        return new UnlockContentPayload(kind.name(), payload.getName());
    }

    public UpdateQuestProgressPayload applyQuestProgress(String username, UpdateQuestProgressPayload payload) {
        User user = requireUser(username);
        List<UpdateQuestProgressPayload.QuestProgressRow> rows = payload.getRows() == null
                ? List.of()
                : payload.getRows();
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            QuestProgressStore.saveExportedRows(conn, user.getId(), rows);
            conn.commit();
        } catch (Exception exception) {
            throw new RuntimeException("Could not save quest progress.", exception);
        }
        UpdateQuestProgressPayload ok = new UpdateQuestProgressPayload();
        ok.setRows(rows);
        return ok;
    }

    public UpdateAdventurePayload applyAdventure(String username, UpdateAdventurePayload payload) {
        User user = requireUser(username);
        ChapterId chapter = ChapterId.fromName(payload.getUnlockedChapter());
        if (chapter == null) {
            throw new SyncValidationException("invalid chapter");
        }
        int difficulty = Math.max(0, Math.min(2, payload.getDifficultyLevel()));
        user.getChapterProgress().setUnlockedChapter(chapter);
        user.setDifficultyLevel(difficulty);
        user.getChapterProgress().clearCompletedLevels();
        AdventureProgressStore.applyCompletedLevels(user.getChapterProgress(), payload.getCompletedLevels());
        database().saveAdventureProgress(user);
        UpdateAdventurePayload ok = new UpdateAdventurePayload();
        ok.setUnlockedChapter(user.getChapterProgress().getUnlockedChapter().getKey());
        ok.setDifficultyLevel(user.getDifficultyLevel());
        ok.setCompletedLevels(AdventureProgressStore.serializeCompleted(user.getChapterProgress()));
        return ok;
    }

    public UpdateMinigameStagesPayload applyMinigameStages(String username, UpdateMinigameStagesPayload payload) {
        User user = requireUser(username);
        List<UpdateMinigameStagesPayload.MinigameStageRow> rows = new ArrayList<>();
        if (payload.getRows() != null) {
            for (UpdateMinigameStagesPayload.MinigameStageRow row : payload.getRows()) {
                if (row == null || row.getMinigameId() == null || row.getMinigameId().isBlank()) {
                    continue;
                }
                if (row.getStageIndex() < 0) {
                    continue;
                }
                MiniGameId id = MiniGameId.fromName(row.getMinigameId());
                if (id == null) {
                    continue;
                }
                rows.add(row);
            }
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            io.github.finalwave.model.user.MiniGameProgressStore.saveStageRows(conn, user.getId(), rows);
            conn.commit();
        } catch (Exception exception) {
            throw new RuntimeException("Could not save minigame stages.", exception);
        }
        UpdateMinigameStagesPayload ok = new UpdateMinigameStagesPayload();
        ok.setRows(rows);
        return ok;
    }

    public UpdateMatchSavePayload applyMatchSave(String username, UpdateMatchSavePayload payload) {
        User user = requireUser(username);
        JsonNode snapshotNode = payload.getSnapshot();
        if (snapshotNode == null || snapshotNode.isNull() || !snapshotNode.isObject()) {
            throw new SyncValidationException("match snapshot must be an object");
        }
        try {
            MatchSaveSnapshot snapshot = MAPPER.treeToValue(snapshotNode, MatchSaveSnapshot.class);
            database().saveMatchSnapshot(user, snapshot);
            UpdateMatchSavePayload ok = new UpdateMatchSavePayload();
            ok.setSnapshot(MAPPER.valueToTree(snapshot));
            return ok;
        } catch (Exception exception) {
            throw new SyncValidationException("invalid match snapshot");
        }
    }

    public void clearMatchSave(String username) {
        User user = requireUser(username);
        database().clearMatchSave(user);
    }

    public UpdateNewsPayload applyNews(String username, UpdateNewsPayload payload) {
        User user = requireUser(username);
        user.getNewsItems().clear();
        if (payload.getRows() != null) {
            for (UpdateNewsPayload.NewsRow row : payload.getRows()) {
                if (row == null || row.getMessage() == null || row.getMessage().isBlank()) {
                    continue;
                }
                NewsType type;
                try {
                    type = NewsType.valueOf(row.getType());
                } catch (Exception exception) {
                    throw new SyncValidationException("invalid news type");
                }
                NewsItem item = new NewsItem(
                        type,
                        row.getSubject(),
                        row.getMessage(),
                        Math.max(0L, row.getCreatedAtMillis()),
                        row.isRead()
                );
                item.setId(row.getId());
                user.getNewsItems().add(item);
            }
        }
        database().saveUserNews(user);
        return newsFrom(user);
    }

    public UpdateSettingsPayload applySettings(String username, UpdateSettingsPayload payload) {
        User user = requireUser(username);
        user.setGameSpeed(payload.getGameSpeed());
        user.setShowLawnGrid(payload.isShowLawnGrid());
        user.setDebugMode(payload.isDebugMode());
        database().saveUserSettings(user);
        UpdateSettingsPayload ok = new UpdateSettingsPayload();
        ok.setGameSpeed(user.getGameSpeed());
        ok.setShowLawnGrid(user.isShowLawnGrid());
        ok.setDebugMode(user.isDebugMode());
        return ok;
    }

    public UpdateScoreGamePayload applyScoreGame(String username, UpdateScoreGamePayload payload) {
        User user = requireUser(username);
        user.setHasPlayed(payload.isHasPlayed());
        int incoming = Math.max(0, payload.getBestMeowPoint());
        if (payload.isHasPlayed()) {
            user.setBestMeowPoint(Math.max(user.getBestMeowPoint(), incoming));
        }
        database().saveBestMeowPoint(user);
        UpdateScoreGamePayload ok = new UpdateScoreGamePayload();
        ok.setHasPlayed(user.hasPlayed());
        ok.setBestMeowPoint(user.getBestMeowPoint());
        return ok;
    }

    private User requireUser(String username) {
        User user = database.getUser(username);
        if (user == null) {
            throw new SyncValidationException("user not found");
        }
        return user;
    }

    private UserDatabase database() {
        return database.delegate();
    }

    private static UpdateWalletPayload clampWallet(UpdateWalletPayload payload) {
        UpdateWalletPayload clamped = new UpdateWalletPayload();
        clamped.setCoins(Math.max(0, payload.getCoins()));
        clamped.setDiamonds(Math.max(0, payload.getDiamonds()));
        clamped.setPlantFood(Math.max(0, payload.getPlantFood()));
        clamped.setGamesPlayed(Math.max(0, payload.getGamesPlayed()));
        clamped.setQuestDay(payload.getQuestDay());
        clamped.setDailyOfferPlant(payload.getDailyOfferPlant());
        clamped.setDailyOfferDate(payload.getDailyOfferDate());
        clamped.setDailyOfferPurchased(payload.isDailyOfferPurchased());
        return clamped;
    }

    private static UpdateWalletPayload walletFrom(User user) {
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

    private static UpdateGreenhousePotPayload potPayloadFrom(GreenhousePot pot) {
        UpdateGreenhousePotPayload payload = new UpdateGreenhousePotPayload();
        payload.setX(pot.getX());
        payload.setY(pot.getY());
        payload.setLocked(pot.isLocked());
        payload.setPlantType(pot.getPlantType());
        payload.setPlantedAtMillis(pot.getPlantedAtMillis());
        payload.setMarigold(pot.isMarigold());
        return payload;
    }

    private static UpdateNewsPayload newsFrom(User user) {
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

    private static UnlockKind parseUnlockKind(String kind) {
        if (kind == null || kind.isBlank()) {
            throw new SyncValidationException("unlock kind required");
        }
        String normalized = kind.trim().toLowerCase();
        if ("zombies".equals(normalized)) {
            return UnlockKind.ZOMBIES;
        }
        if ("levels".equals(normalized)) {
            return UnlockKind.LEVELS;
        }
        if ("minigames".equals(normalized)) {
            return UnlockKind.MINIGAMES;
        }
        throw new SyncValidationException("invalid unlock kind");
    }
}
