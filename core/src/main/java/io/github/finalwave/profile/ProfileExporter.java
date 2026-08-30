package io.github.finalwave.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.collection.OwnedPlant;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.QuestProgressStore;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public final class ProfileExporter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProfileExporter() {
    }

    public static LoginOkPayload export(User user, UserDatabase database) {
        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }
        LoginOkPayload payload = new LoginOkPayload();
        payload.setUserId(user.getId());
        payload.setUsername(user.getUsername());
        payload.setNickname(user.getNickname());
        payload.setEmail(user.getEmail());
        payload.setGender(user.getGender().name());
        payload.setCoins(user.getCoins());
        payload.setDiamonds(user.getDiamonds());
        payload.setPlantFood(user.getPlantFood());
        payload.setDailyOfferPlant(user.getDailyOfferPlant());
        payload.setDailyOfferDate(user.getDailyOfferDate() == null ? null : user.getDailyOfferDate().toString());
        payload.setDailyOfferPurchased(user.isDailyOfferPurchased());
        payload.setGamesPlayed(user.getGamesPlayed());
        payload.setQuestDay(user.getQuestDay() == null ? null : user.getQuestDay().toString());

        List<LoginOkPayload.PlantEntry> plants = new ArrayList<>();
        for (OwnedPlant plant : user.getPlantProgress().getOwnedPlants().values()) {
            plants.add(new LoginOkPayload.PlantEntry(
                    plant.getPlantName(),
                    plant.getLevel(),
                    plant.isUnlocked(),
                    plant.getSeedPackets()
            ));
        }
        payload.setPlants(plants);
        payload.setStoredBoosts(new ArrayList<>(user.getStoredBoosts()));
        List<LoginOkPayload.GreenhousePotEntry> pots = new ArrayList<>();
        for (GreenhousePot pot : user.getGreenhousePots()) {
            LoginOkPayload.GreenhousePotEntry entry = new LoginOkPayload.GreenhousePotEntry();
            entry.setX(pot.getX());
            entry.setY(pot.getY());
            entry.setLocked(pot.isLocked());
            entry.setPlantType(pot.getPlantType());
            entry.setPlantedAtMillis(pot.getPlantedAtMillis());
            entry.setMarigold(pot.isMarigold());
            pots.add(entry);
        }
        payload.setGreenhousePots(pots);
        payload.setUnlockedZombies(new ArrayList<>(user.getUnlockedZombies()));
        payload.setUnlockedLevels(new ArrayList<>(user.getUnlockedLevels()));
        payload.setUnlockedMinigames(new ArrayList<>(user.getUnlockedMinigames()));

        LoginOkPayload.AdventureEntry adventure = new LoginOkPayload.AdventureEntry();
        adventure.setUnlockedChapter(user.getChapterProgress().getUnlockedChapter().getKey());
        adventure.setDifficultyLevel(user.getDifficultyLevel());
        adventure.setCompletedLevels(serializeCompletedLevels(user.getChapterProgress()));
        payload.setAdventure(adventure);

        List<LoginOkPayload.MinigameStageEntry> stages = new ArrayList<>();
        for (Map.Entry<MiniGameId, Set<Integer>> entry : user.getMiniGameProgress().getAllCompletedStages().entrySet()) {
            for (Integer stageIndex : entry.getValue()) {
                stages.add(new LoginOkPayload.MinigameStageEntry(entry.getKey().getKey(), stageIndex));
            }
        }
        payload.setMinigameStages(stages);

        LoginOkPayload.SettingsEntry settings = new LoginOkPayload.SettingsEntry();
        settings.setGameSpeed(user.getGameSpeed());
        settings.setShowLawnGrid(user.isShowLawnGrid());
        settings.setDebugMode(user.isDebugMode());
        payload.setSettings(settings);

        LoginOkPayload.ScoreGameEntry scoreGame = new LoginOkPayload.ScoreGameEntry();
        scoreGame.setHasPlayed(user.hasPlayed());
        scoreGame.setBestMeowPoint(user.getBestMeowPoint());
        payload.setScoreGame(scoreGame);

        try (Connection conn = DatabaseUtil.getConnection()) {
            payload.setQuestProgress(QuestProgressStore.exportRows(conn, user.getId()));
        } catch (Exception exception) {
            throw new RuntimeException("Could not export quest progress.", exception);
        }

        MatchSaveSnapshot matchSave = database.loadMatchSnapshot(user);
        if (matchSave != null) {
            payload.setMatchSave(MAPPER.valueToTree(matchSave));
        }
        return payload;
    }

    private static String serializeCompletedLevels(ChapterProgress progress) {
        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<ChapterId, Set<Integer>> entry : progress.getAllCompletedLevels().entrySet()) {
            StringJoiner levels = new StringJoiner(",");
            for (Integer level : entry.getValue()) {
                levels.add(Integer.toString(level));
            }
            joiner.add(entry.getKey().getKey() + ":" + levels);
        }
        return joiner.toString();
    }
}
