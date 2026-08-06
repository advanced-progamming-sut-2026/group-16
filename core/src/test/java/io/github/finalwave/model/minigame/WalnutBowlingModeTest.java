package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.mode.WalnutBowlingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalnutBowlingModeTest {

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
    void createSessionConfiguresWalnutBowlingRules() {
        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(1);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();

        assertFalse(session.getSkySunSystem().isEnabled());
        assertTrue(session.isWalnutBowlingActive());
        assertEquals(4, session.getWalnutBowlingRedLineColumn());
        assertNotNull(session.getActiveMiniGameHandler());
        assertNotNull(session.getWaveManager());
        assertEquals(2, session.getWaveManager().getWaveCount());
        assertTrue(session.isConveyorBeltActive());
        assertEquals(1, session.getConveyorBeltPlants().size());
    }

    @Test
    void stageThreeHasTighterRedLineAndMoreWaves() {
        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(3);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(2));
        GameSession session = mode.createSession();

        assertEquals(3, session.getWalnutBowlingRedLineColumn());
        assertEquals(4, session.getWaveManager().getWaveCount());
    }
}
