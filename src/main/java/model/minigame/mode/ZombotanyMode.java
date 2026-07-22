package model.minigame.mode;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.WaveManager;
import model.game.board.GameBoard;
import model.game.mode.GameMode;
import model.minigame.MiniGameStageConfig;

import java.util.HashSet;
import java.util.Random;

public class ZombotanyMode extends GameMode {

    private final MiniGameStageConfig stage;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;

    public ZombotanyMode(MiniGameStageConfig stage,
                         PlantRegistry plantRegistry,
                         ZombieRegistry zombieRegistry,
                         Random random) {
        if (stage == null || plantRegistry == null || zombieRegistry == null) {
            throw new IllegalArgumentException("zombotany mode dependencies must not be null");
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
        session.setLevelId("zombotany-S" + stage.getStageIndex());
        session.setWavesAutoStart(true);
        session.setSelectedLoadout(new HashSet<>(stage.getPlantSeedPool()));

        WaveManager waveManager = new WaveManager(
                stage.getWaveCount(), stage.getBaseWaveCost(), stage.getZombiePool(), random);
        session.setWaveManager(waveManager);
        return session;
    }
}
