package io.github.finalwave.model.minigame.mode;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.mode.GameMode;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public final class NetworkedIZombieMode extends GameMode {

    private final MiniGameStageConfig stage;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;

    public NetworkedIZombieMode(MiniGameStageConfig stage,
                                PlantRegistry plantRegistry,
                                ZombieRegistry zombieRegistry,
                                Random random) {
        if (stage == null || plantRegistry == null || zombieRegistry == null) {
            throw new IllegalArgumentException("Networked I, Zombie mode dependencies must not be null");
        }
        this.stage = stage;
        this.plantRegistry = plantRegistry;
        this.zombieRegistry = zombieRegistry;
        this.random = random == null ? new Random() : random;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createHostSession() {
        GameBoard board = new GameBoard(stage.getRows(), stage.getCols());
        int hostSun = Math.max(stage.getStartingSun(), 250);
        GameSession session = new GameSession(
                plantRegistry, board, hostSun, zombieRegistry, 1, random);
        session.setChapterId("minigame");
        session.setLevelId("i-zombie-network-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(true);
        session.setWavesAutoStart(false);
        Set<String> loadout = new LinkedHashSet<>(stage.getPlantSeedPool());
        session.setSelectedLoadout(loadout);
        session.activateIZombie(
                stage.getRedLineColumn(), stage.getZombiePool(), stage.getZombieSunCosts());
        session.setIZombieSunBalance(stage.getStartingSun());
        NetworkedIZombieHandler handler = new NetworkedIZombieHandler(stage);
        session.setActiveMiniGameHandler(handler);
        handler.onLevelStart(session);
        return session;
    }

    public GameSession createGuestSession() {
        GameBoard board = new GameBoard(stage.getRows(), stage.getCols());
        GameSession session = new GameSession(
                plantRegistry, board, stage.getStartingSun(), zombieRegistry, 1, random);
        session.setChapterId("minigame");
        session.setLevelId("i-zombie-network-guest-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(false);
        session.setWavesAutoStart(false);
        session.activateIZombie(
                stage.getRedLineColumn(), stage.getZombiePool(), stage.getZombieSunCosts());
        session.setSunBalance(0);
        session.setIZombieSunBalance(stage.getStartingSun());
        return session;
    }
}
