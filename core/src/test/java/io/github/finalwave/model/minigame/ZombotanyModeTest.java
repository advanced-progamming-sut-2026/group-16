package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.mode.ZombotanyMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZombotanyModeTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry zombieRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
        zombieRegistry.loadFromJson("src/main/resources/zombotany-zombies.json");
    }

    @Test
    void createSessionConfiguresNormalLevelStyleRules() {
        MiniGameStageConfig stage = MiniGameStageConfig.zombotany(1);
        ZombotanyMode mode = new ZombotanyMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();

        assertTrue(session.getSkySunSystem().isEnabled());
        assertTrue(session.isWavesAutoStart());
        assertEquals(50, session.getSunBalance());
        assertTrue(session.getSelectedLoadout().isEmpty());
        assertNotNull(session.getWaveManager());
        assertEquals(2, session.getWaveManager().getWaveCount());
        assertTrue(session.getWaveManager().getWaves().size() >= 1);
    }

    @Test
    void stageThreeHasMoreWavesAndRicherRoster() {
        MiniGameStageConfig stage = MiniGameStageConfig.zombotany(3);
        ZombotanyMode mode = new ZombotanyMode(stage, plantRegistry, zombieRegistry, new Random(3));
        GameSession session = mode.createSession();

        assertEquals(75, session.getSunBalance());
        assertEquals(4, session.getWaveManager().getWaveCount());
        assertTrue(stage.getZombiePool().contains("ZombieJalapeno"));
        assertTrue(stage.getZombiePool().contains("ZombieSquash"));
        assertTrue(stage.getPlantSeedPool().contains("Melon-pult"));
    }
}
