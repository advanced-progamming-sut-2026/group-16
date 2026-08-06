package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.minigame.mode.BeghouledMode;
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

class BeghouledModeTest {

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
    void createSessionConfiguresBeghouledRules() {
        MiniGameStageConfig stage = MiniGameStageConfig.beghouled(1);
        BeghouledMode mode = new BeghouledMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();

        assertFalse(session.getSkySunSystem().isEnabled());
        assertTrue(session.isWavesAutoStart());
        assertTrue(session.isBeghouledActive());
        assertEquals(8, session.getBeghouledMatchTarget());
        assertNotNull(session.getActiveMiniGameHandler());
        assertNotNull(session.getWaveManager());
    }

    @Test
    void boardIsFilledFromStagePool() {
        MiniGameStageConfig stage = MiniGameStageConfig.beghouled(1);
        BeghouledMode mode = new BeghouledMode(stage, plantRegistry, zombieRegistry, new Random(2));
        GameSession session = mode.createSession();

        Set<String> names = new HashSet<>();
        for (Plant plant : session.getBoard().getAllPlants()) {
            names.add(plant.getName());
            assertTrue(stage.getPlantSeedPool().contains(plant.getName()),
                    "unexpected plant " + plant.getName());
        }
        assertEquals(session.getBoard().getRows() * session.getBoard().getCols(),
                session.getBoard().getAllPlants().size());
        assertFalse(names.isEmpty());
    }

    @Test
    void stageThreeHasHigherMatchTarget() {
        MiniGameStageConfig stage = MiniGameStageConfig.beghouled(3);
        BeghouledMode mode = new BeghouledMode(stage, plantRegistry, zombieRegistry, new Random(3));
        GameSession session = mode.createSession();
        assertEquals(16, session.getBeghouledMatchTarget());
        assertEquals(6, stage.getUpgrades().size());
    }
}
