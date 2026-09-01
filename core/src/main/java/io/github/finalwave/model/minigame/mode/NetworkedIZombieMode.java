package io.github.finalwave.model.minigame.mode;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.definition.zombie.ZombieDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.mode.GameMode;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    public PlantRegistry plantRegistry() {
        return plantRegistry;
    }

    public ZombieRegistry zombieRegistry() {
        return zombieRegistry;
    }

    public List<String> allZombieAliases() {
        List<String> aliases = new ArrayList<>();
        for (ZombieDefinition definition : zombieRegistry.getAllDefinitions()) {
            if (definition != null && definition.getAlias() != null && !definition.getAlias().isBlank()) {
                aliases.add(definition.getAlias());
            }
        }
        return List.copyOf(aliases);
    }

    public GameSession createHostSession() {
        GameBoard board = new GameBoard(stage.getRows(), stage.getCols());
        GameSession session = new GameSession(
                plantRegistry, board, IZombieDuelCatalog.PLANT_START_SUN, zombieRegistry, 1, random);
        session.setChapterId("minigame");
        session.setLevelId("i-zombie-network-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(true);
        session.setWavesAutoStart(false);
        session.setSelectedLoadout(Set.of());
        session.activateIZombie(
                stage.getRedLineColumn(),
                List.of(),
                Map.of());
        session.setIZombieSunBalance(IZombieDuelCatalog.ZOMBIE_START_SUN);
        NetworkedIZombieHandler handler = new NetworkedIZombieHandler(stage);
        session.setActiveMiniGameHandler(handler);
        handler.onLevelStart(session);
        return session;
    }

    public GameSession createGuestSession() {
        GameBoard board = new GameBoard(stage.getRows(), stage.getCols());
        GameSession session = new GameSession(
                plantRegistry, board, 0, zombieRegistry, 1, random);
        session.setChapterId("minigame");
        session.setLevelId("i-zombie-network-guest-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(false);
        session.setWavesAutoStart(false);
        session.setSelectedLoadout(Set.of());
        session.activateIZombie(
                stage.getRedLineColumn(),
                List.of(),
                Map.of());
        session.setSunBalance(0);
        session.setIZombieSunBalance(IZombieDuelCatalog.ZOMBIE_START_SUN);
        return session;
    }

    public void applyPicks(GameSession session, List<String> plantPicks, List<String> zombiePicks) {
        if (session == null) {
            return;
        }
        List<String> plants = sanitizePlants(plantPicks);
        List<String> zombies = sanitizeZombies(zombiePicks);
        Set<String> loadout = new LinkedHashSet<>(plants);
        session.setSelectedLoadout(loadout);
        session.setIZombieRoster(zombies, IZombieDuelCatalog.costsFor(zombies));
        session.setIZombieSunBalance(IZombieDuelCatalog.ZOMBIE_START_SUN);
        if (session.getActiveMiniGameHandler() instanceof NetworkedIZombieHandler handler) {
            handler.beginPlay(session);
        }
    }

    private List<String> sanitizePlants(List<String> picks) {
        if (picks == null || picks.isEmpty()) {
            return IZombieDuelCatalog.DEFAULT_PLANTS;
        }
        LinkedHashSet<String> selected = new LinkedHashSet<>();
        for (String name : picks) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String trimmed = name.trim();
            if (plantRegistry.getDefinition(trimmed) != null) {
                selected.add(trimmed);
            }
            if (selected.size() >= IZombieDuelCatalog.PLANT_SLOTS) {
                break;
            }
        }
        if (selected.isEmpty()) {
            return IZombieDuelCatalog.DEFAULT_PLANTS;
        }
        return List.copyOf(selected);
    }

    private List<String> sanitizeZombies(List<String> picks) {
        if (picks == null || picks.isEmpty()) {
            return IZombieDuelCatalog.DEFAULT_ZOMBIES;
        }
        LinkedHashSet<String> selected = new LinkedHashSet<>();
        for (String name : picks) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String trimmed = name.trim();
            if (zombieRegistry.getDefinition(trimmed) != null) {
                selected.add(trimmed);
            }
            if (selected.size() >= IZombieDuelCatalog.ZOMBIE_SLOTS) {
                break;
            }
        }
        if (selected.isEmpty()) {
            return IZombieDuelCatalog.DEFAULT_ZOMBIES;
        }
        return List.copyOf(selected);
    }
}
