package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.mode.IZombieMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IZombieHandlerTest {

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

    private GameSession createStage1() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(1);
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();
        session.start();
        return session;
    }

    @Test
    void eatingAllBrainsWins() {
        GameSession session = createStage1();
        for (int row = 0; row < 5; row++) {
            Zombie zombie = session.spawnZombieOfType("ZombieDefault", row, 0.1);
            session.handleZombieReachedHouse(zombie);
            assertTrue(session.isIZombieBrainEaten(row));
        }
        session.getActiveMiniGameHandler().onTick(session);
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void reachingHouseEatsBrainWithoutLosing() {
        GameSession session = createStage1();
        Zombie zombie = session.spawnZombieOfType("ZombieDefault", 0, 0.1);
        session.handleZombieReachedHouse(zombie);

        assertTrue(session.isIZombieBrainEaten(0));
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
        assertTrue(zombie.isDead());
    }

    @Test
    void brokeAndNoZombiesLoses() {
        GameSession session = createStage1();
        session.setSunBalance(0);
        session.nukeAllZombies();
        session.getActiveMiniGameHandler().onTick(session);

        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void hasSunButNoZombiesDoesNotLose() {
        GameSession session = createStage1();
        session.setSunBalance(150);
        session.nukeAllZombies();
        session.getActiveMiniGameHandler().onTick(session);

        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void placeZombieRejectsLeftOfLine() {
        GameSession session = createStage1();
        assertEquals(PlantPlacementResult.BEYOND_PLANTING_LINE,
                session.tryPlaceZombie("ZombieDefault", 2, 0));
        assertEquals(PlantPlacementResult.BEYOND_PLANTING_LINE,
                session.tryPlaceZombie("ZombieDefault", 1, 0));
    }

    @Test
    void placeZombieRejectsOutsideRoster() {
        GameSession session = createStage1();
        assertEquals(PlantPlacementResult.NOT_IN_LOADOUT,
                session.tryPlaceZombie("ZombieGargantuar", 5, 0));
    }

    @Test
    void placeZombieRejectsInsufficientSun() {
        GameSession session = createStage1();
        session.setSunBalance(10);
        assertEquals(PlantPlacementResult.INSUFFICIENT_SUN,
                session.tryPlaceZombie("ZombieDefault", 5, 0));
    }

    @Test
    void placeZombieSucceedsAndDeductsSun() {
        GameSession session = createStage1();
        int before = session.getSunBalance();
        int livingBefore = countLiving(session);

        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlaceZombie("ZombieImp", 5, 0));
        assertEquals(before - 25, session.getSunBalance());
        assertEquals(livingBefore + 1, countLiving(session));
    }

    @Test
    void sunProducersGenerateSunOverTime() {
        GameSession session = createStage1();
        int before = session.getSunBalance();
        session.advanceTicks(GameSession.TICKS_PER_SECOND);
        assertTrue(session.getSunBalance() > before);
    }

    @Test
    void mapShowsBrainsAndPlacementLine() {
        GameSession session = createStage1();
        String map = session.renderMap();
        assertTrue(map.contains("Placement line: column 2"));
        assertTrue(map.contains("Brains: 0/5"));
        assertTrue(map.contains("[Brain]"));
        assertFalse(map.contains("[Mower]"));
    }

    private static int countLiving(GameSession session) {
        int count = 0;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) {
                count++;
            }
        }
        return count;
    }
}
