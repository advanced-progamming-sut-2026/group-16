package model.minigame.mode;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.board.GameBoard;
import model.game.mode.GameMode;
import model.minigame.MiniGameStageConfig;
import model.minigame.izombie.IZombieHandler;

import java.util.Random;

public class IZombieMode extends GameMode {

    private final MiniGameStageConfig stage;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;

    public IZombieMode(MiniGameStageConfig stage,
                       PlantRegistry plantRegistry,
                       ZombieRegistry zombieRegistry,
                       Random random) {
        if (stage == null || plantRegistry == null || zombieRegistry == null) {
            throw new IllegalArgumentException("I, Zombie mode dependencies must not be null");
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
        session.setLevelId("i-zombie-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(false);
        session.setWavesAutoStart(false);
        session.activateIZombie(
                stage.getRedLineColumn(), stage.getZombiePool(), stage.getZombieSunCosts());

        IZombieHandler handler = new IZombieHandler(stage, random);
        session.setActiveMiniGameHandler(handler);
        handler.onLevelStart(session);
        return session;
    }
}
