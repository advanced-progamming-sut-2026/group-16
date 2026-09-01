package io.github.finalwave.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.collection.OwnedPlant;
import io.github.finalwave.model.collection.PlayerPlantProgress;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.model.quest.QuestService;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserProgressInitializer;
import io.github.finalwave.network.auth.LoginOkPayload;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ProfileApplier {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProfileApplier() {
    }

    public static User apply(LoginOkPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is required");
        }
        Gender gender = Gender.fromString(payload.getGender());
        User user = new User(
                payload.getUsername(),
                "",
                payload.getNickname(),
                payload.getEmail(),
                gender
        );
        user.setId(payload.getUserId());
        user.setCoins(payload.getCoins());
        user.setDiamonds(payload.getDiamonds());
        user.setPlantFood(payload.getPlantFood());
        user.setDailyOfferPlant(payload.getDailyOfferPlant());
        if (payload.getDailyOfferDate() != null && !payload.getDailyOfferDate().isBlank()) {
            user.setDailyOfferDate(LocalDate.parse(payload.getDailyOfferDate()));
        }
        user.setDailyOfferPurchased(payload.isDailyOfferPurchased());
        user.setGamesPlayed(payload.getGamesPlayed());
        if (payload.getQuestDay() != null && !payload.getQuestDay().isBlank()) {
            user.setQuestDay(LocalDate.parse(payload.getQuestDay()));
        }

        List<OwnedPlant> plants = new ArrayList<>();
        if (payload.getPlants() != null) {
            for (LoginOkPayload.PlantEntry plant : payload.getPlants()) {
                plants.add(new OwnedPlant(
                        plant.getPlantName(),
                        plant.getLevel(),
                        plant.isUnlocked(),
                        plant.getSeedPackets()
                ));
            }
        }
        user.setPlantProgress(PlayerPlantProgress.fromOwnedPlants(plants));

        user.getStoredBoosts().clear();
        if (payload.getStoredBoosts() != null) {
            user.getStoredBoosts().addAll(payload.getStoredBoosts());
        }

        user.getGreenhousePots().clear();
        if (payload.getGreenhousePots() != null) {
            for (LoginOkPayload.GreenhousePotEntry potEntry : payload.getGreenhousePots()) {
                GreenhousePot pot = new GreenhousePot(potEntry.getX(), potEntry.getY(), potEntry.isLocked());
                if (potEntry.getPlantType() != null) {
                    pot.plant(potEntry.getPlantType(), potEntry.isMarigold(), potEntry.getPlantedAtMillis());
                }
                user.getGreenhousePots().add(pot);
            }
        }
        UserProgressInitializer.ensureGreenhousePots(user);

        user.getUnlockedZombies().clear();
        if (payload.getUnlockedZombies() != null) {
            user.getUnlockedZombies().addAll(payload.getUnlockedZombies());
        }
        user.getUnlockedLevels().clear();
        if (payload.getUnlockedLevels() != null) {
            user.getUnlockedLevels().addAll(payload.getUnlockedLevels());
        }
        user.getUnlockedMinigames().clear();
        if (payload.getUnlockedMinigames() != null) {
            user.getUnlockedMinigames().addAll(payload.getUnlockedMinigames());
        }

        if (payload.getAdventure() != null) {
            LoginOkPayload.AdventureEntry adventure = payload.getAdventure();
            ChapterId unlocked = ChapterId.fromName(adventure.getUnlockedChapter());
            if (unlocked != null) {
                user.getChapterProgress().setUnlockedChapter(unlocked);
            }
            user.setDifficultyLevel(adventure.getDifficultyLevel());
            parseCompletedLevels(adventure.getCompletedLevels(), user);
        }

        if (payload.getMinigameStages() != null) {
            for (LoginOkPayload.MinigameStageEntry stage : payload.getMinigameStages()) {
                MiniGameId id = MiniGameId.fromName(stage.getMinigameId());
                if (id != null) {
                    user.getMiniGameProgress().restoreCompletedStage(id, stage.getStageIndex());
                }
            }
        }

        if (payload.getSettings() != null) {
            user.setGameSpeed(payload.getSettings().getGameSpeed());
            user.setShowLawnGrid(payload.getSettings().isShowLawnGrid());
            user.setDebugMode(payload.getSettings().isDebugMode());
        }

        if (payload.getScoreGame() != null) {
            user.setHasPlayed(payload.getScoreGame().isHasPlayed());
            if (payload.getScoreGame().isHasPlayed()) {
                user.setBestMeowPoint(payload.getScoreGame().getBestMeowPoint());
            }
        }

        QuestService.createTrackerFor(user, null);
        if (payload.getQuestProgress() != null && user.getQuestTracker() != null) {
            for (LoginOkPayload.QuestProgressEntry row : payload.getQuestProgress()) {
                for (Quest quest : user.getQuestTracker().getQuests()) {
                    if (quest.getId().equals(row.getQuestId())) {
                        quest.restoreState(row.isCompleted(), row.isClaimed(), row.getProgressBlob());
                        break;
                    }
                }
            }
        }

        if (payload.getMatchSave() != null && !payload.getMatchSave().isNull()) {
            try {
                user.setMatchSaveSnapshot(MAPPER.treeToValue(payload.getMatchSave(), MatchSaveSnapshot.class));
            } catch (Exception ignored) {
                user.setMatchSaveSnapshot(null);
            }
        }
        return user;
    }

    private static void parseCompletedLevels(String blob, User user) {
        if (blob == null || blob.isBlank()) {
            return;
        }
        for (String part : blob.split(";")) {
            if (part.isBlank()) {
                continue;
            }
            int colon = part.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            ChapterId chapter = ChapterId.fromName(part.substring(0, colon));
            if (chapter == null) {
                continue;
            }
            String levels = part.substring(colon + 1);
            if (levels.isBlank()) {
                continue;
            }
            for (String level : levels.split(",")) {
                try {
                    user.getChapterProgress().restoreCompletedLevel(chapter, Integer.parseInt(level.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
