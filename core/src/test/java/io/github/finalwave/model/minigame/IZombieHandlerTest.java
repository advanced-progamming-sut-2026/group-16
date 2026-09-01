package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
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
        assertTrue(zombie.isAlive());
        assertTrue(zombie.isStationary());
    }

    @Test
    void walkingAnEmptyLaneEatsTheBrainWithoutTakingDamage() {
        GameSession session = createStage1();
        for (Plant plant : session.getBoard().getAllPlants()) {
            session.removePlantFromBoard(plant, false);
        }
        Zombie zombie = session.spawnZombieOfType("ZombieDefault", 2, 2.0);
        int health = zombie.getHealth();
        session.advanceTicks(GameSession.TICKS_PER_SECOND * 20);

        assertTrue(zombie.isAlive());
        assertEquals(health, zombie.getHealth());
        assertTrue(session.isIZombieBrainEaten(2));
        assertTrue(zombie.isStationary());
        assertEquals(ZombieState.MOVING, zombie.getState());
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void brokeAndNoZombiesLoses() {
        GameSession session = createStage1();
        session.setIZombieSunBalance(0);
        session.nukeAllZombies();
        session.getActiveMiniGameHandler().onTick(session);

        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void hasSunButNoZombiesDoesNotLose() {
        GameSession session = createStage1();
        session.setIZombieSunBalance(150);
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
        session.setIZombieSunBalance(10);
        assertEquals(PlantPlacementResult.INSUFFICIENT_SUN,
                session.tryPlaceZombie("ZombieDefault", 5, 0));
    }

    @Test
    void placeZombieSucceedsAndDeductsSun() {
        GameSession session = createStage1();
        int before = session.getIZombieSunBalance();
        int livingBefore = countLiving(session);

        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlaceZombie("ZombieImp", 5, 0));
        assertEquals(before - 25, session.getIZombieSunBalance());
        assertEquals(livingBefore + 1, countLiving(session));
    }

    @Test
    void placeZombieAppliesPacketCooldown() {
        GameSession session = createStage1();
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlaceZombie("ZombieDefault", 5, 0));
        assertEquals(PlantPlacementResult.ON_COOLDOWN,
                session.tryPlaceZombie("ZombieDefault", 5, 1));
    }

    @Test
    void sunProducersGenerateSunOverTime() {
        GameSession session = createStage1();
        int before = session.getIZombieSunBalance();
        session.advanceTicks(GameSession.TICKS_PER_SECOND);
        assertTrue(session.getIZombieSunBalance() > before);
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

    @Test
    void placedZombiesStayInTheirLane() {
        GameSession session = createStage1();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlaceZombie("ZombieDefault", 5, 0));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlaceZombie("ZombieImp", 6, 3));
        session.spawnZombieOfType("ZombiePiano", 2, 7);
        Zombie walker = walkerOnRow(session, 0);
        Zombie imp = walkerOnRow(session, 3);
        session.advanceTicks(120);
        assertEquals(0, walker.getRow());
        assertEquals(3, imp.getRow());
    }

    private static Zombie walkerOnRow(GameSession session, int row) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && !zombie.isStationary() && zombie.getRow() == row) {
                return zombie;
            }
        }
        throw new AssertionError("No walker on row " + row);
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
