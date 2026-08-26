package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.mode.ZombotanyMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class ZombotanyPlantSelectionController extends PlantSelectionController {

    private final MiniGameStageConfig stage;

    public ZombotanyPlantSelectionController(User user,
                                             UserDatabase userDatabase,
                                             MiniGameStageConfig stage) {
        super(user, userDatabase, egyptChapter(), levelFor(stage));
        this.stage = stage;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    @Override
    protected void handleStartGame() {
        if (selected.isEmpty()) {
            getViewApi().errorLoadoutEmpty();
            return;
        }
        ZombieRegistry zombotanyRegistry = MiniGameHubController.loadZombotanyZombieRegistry();
        ZombotanyMode mode = new ZombotanyMode(stage, plantRegistry, zombotanyRegistry, new Random());
        GameSession session = mode.createSession();
        session.setSelectedLoadout(Set.copyOf(selected));
        getViewApi().showGameStarted();
        ZombotanyController gameplay = new ZombotanyController(
                user, userDatabase, mode, session, stage, boosted);
        navigator.replace(gameplay);
        session.start();
    }

    private static ChapterConfig egyptChapter() {
        return AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
    }

    private static LevelConfig levelFor(MiniGameStageConfig stage) {
        List<String> zombies = new ArrayList<>(new LinkedHashSet<>(stage.getZombiePool()));
        return LevelConfig.normal(
                Math.max(1, stage.getStageIndex()),
                stage.getWaveCount(),
                stage.getStartingSun(),
                stage.getBaseWaveCost(),
                zombies);
    }
}
