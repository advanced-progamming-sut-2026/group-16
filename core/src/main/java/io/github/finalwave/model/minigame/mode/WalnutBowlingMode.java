package io.github.finalwave.model.minigame.mode;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.WaveManager;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.mode.GameMode;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.handler.WalnutBowlingHandler;

import java.util.Random;

public class WalnutBowlingMode extends GameMode {

    private final MiniGameStageConfig stage;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;

    public WalnutBowlingMode(MiniGameStageConfig stage,
                             PlantRegistry plantRegistry,
                             ZombieRegistry zombieRegistry,
                             Random random) {
        if (stage == null || plantRegistry == null || zombieRegistry == null) {
            throw new IllegalArgumentException("walnut bowling mode dependencies must not be null");
        }
        this.stage = stage;
        this.plantRegistry = plantRegistry;
        this.zombieRegistry = zombieRegistry;
        this.random = random == null ? new Random() : random;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createSession() {
        GameBoard board = new GameBoard(stage.getRows(), stage.getCols());
        GameSession session = new GameSession(
                plantRegistry, board, stage.getStartingSun(), zombieRegistry, 1, random);
        session.setChapterId("minigame");
        session.setLevelId("walnut-bowling-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(false);
        session.setWavesAutoStart(true);

        WaveManager waveManager = new WaveManager(
                stage.getWaveCount(), stage.getBaseWaveCost(), stage.getZombiePool(), random);
        session.setWaveManager(waveManager);

        WalnutBowlingHandler handler = new WalnutBowlingHandler(stage, random);
        session.setActiveMiniGameHandler(handler);
        session.activateWalnutBowling(stage.getRedLineColumn());
        session.getBowlingNutSystem().configureDamageFromRegistry(zombieRegistry);

        handler.onLevelStart(session);
        return session;
    }
}
