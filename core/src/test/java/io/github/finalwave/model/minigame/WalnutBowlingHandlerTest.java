package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.minigame.handler.WalnutBowlingHandler;
import io.github.finalwave.model.minigame.mode.WalnutBowlingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalnutBowlingHandlerTest {

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
    void conveyorDeliversFirstNutOnLevelStart() {
        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(1);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();

        assertTrue(session.isConveyorBeltActive());
        assertEquals(1, session.getConveyorBeltPlants().size());
    }

    @Test
    void plantingBeyondRedLineIsRejected() {
        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(1);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(2));
        GameSession session = mode.createSession();
        session.start();

        String plant = session.getConveyorBeltPlants().getFirst();
        assertEquals(PlantPlacementResult.BEYOND_PLANTING_LINE,
                session.tryPlantBowlingNut(plant, 5, 0));
    }

    @Test
    void plantingOnBeltSpawnsBowlingNut() {
        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(1);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(3));
        GameSession session = mode.createSession();
        session.start();

        String plant = session.getConveyorBeltPlants().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlantBowlingNut(plant, 2, 0));
        assertEquals(1, session.getBowlingNutSystem().getNuts().size());
        assertTrue(session.getConveyorBeltPlants().isEmpty());
    }

    @Test
    void clearingWavesAndZombiesWins() {
        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(1);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(4));
        GameSession session = mode.createSession();
        session.start();
        for (int i = 0; i < 3000 && session.getMatchResult() == MatchResult.IN_PROGRESS; i++) {
            if (!session.getZombies().isEmpty()) {
                session.nukeAllZombies();
            }
            session.advanceTicks(1);
        }
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void lawnMowerExhaustionCausesLoss() {
        GameSession session = new GameSession(
                plantRegistry, new io.github.finalwave.model.game.board.GameBoard(), 0, zombieRegistry, 1, new Random(5));
        session.setActiveMiniGameHandler(new WalnutBowlingHandler(
                MiniGameStageConfig.walnutBowling(1), new Random(5)));
        session.activateWalnutBowling(4);
        session.start();

        var zombie1 = session.spawnZombieOfType("ZombieDefault", 0, 0.1);
        session.handleZombieReachedHouse(zombie1);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());

        var zombie2 = session.spawnZombieOfType("ZombieDefault", 0, 0.1);
        session.handleZombieReachedHouse(zombie2);
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }
}
