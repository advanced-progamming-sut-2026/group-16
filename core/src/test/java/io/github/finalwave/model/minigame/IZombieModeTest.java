package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.izombie.IZombieHandler;
import io.github.finalwave.model.minigame.mode.IZombieMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IZombieModeTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry zombieRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
    }

    @Test
    void createSessionConfiguresIZombieRules() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(1);
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();

        assertFalse(session.getSkySunSystem().isEnabled());
        assertFalse(session.isWavesAutoStart());
        assertTrue(session.isIZombieActive());
        assertEquals(2, session.getIZombiePlacementColumn());
        assertEquals(150, session.getSunBalance());
        assertNotNull(session.getActiveMiniGameHandler());
        assertEquals(5, session.getIZombieZombiePool().size());
    }

    @Test
    void stageStartSpawnsOneStationaryProducerPerRow() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(1);
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random(2));
        GameSession session = mode.createSession();

        Set<Integer> producerRows = new HashSet<>();
        int producerCount = 0;
        for (Zombie zombie : session.getZombies()) {
            if (IZombieHandler.SUN_PRODUCER_ALIAS.equals(zombie.getType()) && zombie.isStationary()) {
                producerCount++;
                producerRows.add(zombie.getRow());
                assertEquals(session.getBoard().getCols() - 1, (int) Math.floor(zombie.getX()));
            }
        }
        assertEquals(5, producerCount);
        assertEquals(5, producerRows.size());
    }

    @Test
    void prePlantedPlantsAreLeftOfPlacementLine() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(1);
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random(3));
        GameSession session = mode.createSession();

        var plants = session.getBoard().getAllPlants();
        assertEquals(stage.getPrePlantedPlantCount(), plants.size());
        for (Plant plant : plants) {
            assertTrue(plant.getCol() <= stage.getRedLineColumn(),
                    "plant at col " + plant.getCol() + " should be <= red line");
        }
    }

    @Test
    void stageThreeHasMoreDefensePlants() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(3);
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random(4));
        GameSession session = mode.createSession();

        assertEquals(3, session.getIZombiePlacementColumn());
        assertEquals(12, session.getBoard().getAllPlants().size());
    }

    @Test
    void tenUniqueZombiesAcrossAllStages() {
        Set<String> all = new HashSet<>();
        for (int stage = 1; stage <= 3; stage++) {
            all.addAll(MiniGameStageConfig.iZombie(stage).getZombiePool());
        }
        assertEquals(10, all.size());
        assertFalse(all.contains(IZombieHandler.SUN_PRODUCER_ALIAS));
    }
}
