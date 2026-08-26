package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LawnMower;
import io.github.finalwave.model.game.WaveManager;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.quest.QuestTracker;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class MatchResume {
    private MatchResume() {
    }

    public static GamePlayController open(User user, UserDatabase userDatabase, MatchSaveSnapshot snap) {
        if (user == null || userDatabase == null || snap == null) {
            return null;
        }
        ChapterId chapterId = ChapterId.fromName(snap.chapterKey);
        if (chapterId == null) {
            return null;
        }
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(chapterId);
        if (chapter == null) {
            return null;
        }
        LevelConfig level = chapter.getLevel(snap.levelIndex);
        if (level == null) {
            return null;
        }
        PlantRegistry plants = App.getInstance().getPlantRegistry();
        ZombieRegistry zombies = PlantSelectionController.loadZombieRegistry();
        AdventureMode mode = new AdventureMode(
                chapter, level, plants, zombies, user.getDifficultyLevel(), new Random());
        GameSession session = mode.createSession();
        Set<String> loadout = new LinkedHashSet<>();
        if (snap.loadout != null) {
            loadout.addAll(snap.loadout);
        }
        session.setSelectedLoadout(loadout);
        Set<String> boosted = new LinkedHashSet<>();
        if (snap.boosted != null) {
            boosted.addAll(snap.boosted);
        }
        QuestTracker tracker = user.ensureQuestTracker();
        tracker.registerOn(session.getEventBus());
        tracker.beginSession(session);
        session.attachQuestTracker(tracker);
        GamePlayController gameplay = level.getType() == LevelType.NORMAL
                ? new GamePlayController(user, userDatabase, mode, session, chapter, level, boosted)
                : SpecialLevelControllerFactory.create(
                        level.getType(), user, userDatabase, mode, session, chapter, level, boosted);
        apply(session, snap);
        return gameplay;
    }

    static void apply(GameSession session, MatchSaveSnapshot snap) {
        if (session == null || snap == null) {
            return;
        }
        session.clearLivingUnits();
        List<Zombie> restored = new ArrayList<>();
        if (snap.plants != null) {
            for (MatchSaveSnapshot.PlantSnap plant : snap.plants) {
                if (plant == null || plant.name == null || plant.name.isBlank()) {
                    continue;
                }
                session.restorePlant(plant.name, plant.level, plant.col, plant.row, plant.health, plant.armed);
            }
        }
        if (snap.zombies != null) {
            for (MatchSaveSnapshot.ZombieSnap zombie : snap.zombies) {
                if (zombie == null || zombie.alias == null || zombie.alias.isBlank()) {
                    continue;
                }
                Zombie live = session.restoreZombie(
                        zombie.alias, zombie.row, zombie.x, zombie.health, zombie.freezeTicks);
                if (live != null) {
                    restored.add(live);
                }
            }
        }
        if (snap.usedMowerRows != null) {
            for (LawnMower mower : session.getLawnMowers()) {
                if (mower != null && snap.usedMowerRows.contains(mower.getRow())) {
                    mower.markSpent();
                }
            }
        }
        session.restoreProgress(snap.currentTick, snap.plantsLost, snap.sun, snap.plantFood);
        session.setWavesAutoStart(snap.wavesAutoStart);
        WaveManager waves = session.getWaveManager();
        if (waves != null) {
            waves.restoreProgress(snap.currentWaveIndex, snap.wavesStarted, snap.allWavesSpawned, restored);
        }
        if (session.isConveyorBeltActive() && snap.conveyor != null) {
            session.restoreConveyorBelt(snap.conveyor);
        }
    }
}
