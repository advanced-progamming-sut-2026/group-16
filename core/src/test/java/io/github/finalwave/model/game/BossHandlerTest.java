package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.board.tile.FireTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.boss.BossArena;
import io.github.finalwave.model.game.boss.BossAttacks;
import io.github.finalwave.model.game.boss.BossCatalog;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BossHandlerTest {

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

    private GameSession newBossSession(ChapterId chapterId, Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(chapterId);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(4), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    @Test
    void occupiesTwoRowsAndSkipsStunDamage() {
        Zombie boss = new Zombie.Builder("ZombieEgyptZomboss")
                .maxHealth(BossCatalog.MAX_HEALTH)
                .position(8, 1)
                .build();
        boss.configureAsBoss(2);
        assertTrue(boss.isBoss());
        assertTrue(boss.occupiesRow(1));
        assertTrue(boss.occupiesRow(2));
        assertFalse(boss.occupiesRow(0));
        assertFalse(boss.occupiesRow(3));

        int phaseHp = BossCatalog.MAX_HEALTH / 3;
        boss.takeDamage(phaseHp);
        assertEquals(2, boss.getBossPhase());
        assertTrue(boss.isStunned());
        int healthAfterBreak = boss.getHealth();
        boss.takeDamage(400);
        assertEquals(healthAfterBreak, boss.getHealth());
        while (boss.isStunned()) {
            boss.onTickUpdate(null);
        }
        boss.takeDamage(10);
        assertEquals(healthAfterBreak - 10, boss.getHealth());
    }

    @Test
    void startSpawnsBossActivatesConveyorAndSkipsWaves() {
        GameSession session = newBossSession(ChapterId.ANCIENT_EGYPT, new Random(1));
        BossHandler handler = new BossHandler(ChapterId.ANCIENT_EGYPT, List.of("Peashooter"), new Random(1));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertTrue(session.isBossActive());
        assertTrue(session.isConveyorBeltActive());
        assertFalse(session.isWavesAutoStart());
        assertNotNull(handler.getBoss());
        assertTrue(handler.getBoss().isBoss());
        assertEquals(1, handler.getBoss().getBossPhase());
        assertEquals(BossCatalog.MAX_HEALTH, session.getBossMaxHealth());
    }

    @Test
    void missileDestroysPlantAndRaisesGraves() {
        GameSession session = newBossSession(ChapterId.ANCIENT_EGYPT, new Random(2));
        BossHandler handler = new BossHandler(ChapterId.ANCIENT_EGYPT, List.of("Peashooter"), new Random(2));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 2, 1, 1));
        assertNotNull(session.getBoard().getPlantAt(2, 1));
        int gravesBefore = countGraves(session);

        BossArena arena = new BossArena(session, handler.getBoss(), new Random(2), ChapterId.ANCIENT_EGYPT);
        BossAttacks.Missile missile = new BossAttacks.Missile(true);
        missile.start(arena);
        int ticks = 0;
        while (!missile.tick(arena) && ticks < 80) {
            ticks++;
        }
        int gravesAfter = countGraves(session);
        assertEquals(gravesBefore + 2, gravesAfter);
        assertNull(session.getBoard().getPlantAt(2, 1));
    }

    @Test
    void chargeClearsBothOccupiedRowsThenReturnsHome() {
        GameSession session = newBossSession(ChapterId.ANCIENT_EGYPT, new Random(3));
        BossHandler handler = new BossHandler(ChapterId.ANCIENT_EGYPT,
                List.of("Peashooter", "Wall-nut"), new Random(3));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.addConveyorBeltPlant("Peashooter");
        session.addConveyorBeltPlant("Wall-nut");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 3, 1, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Wall-nut", 4, 2, 1));

        Zombie boss = handler.getBoss();
        double home = boss.getX();
        BossArena arena = new BossArena(session, boss, new Random(3), ChapterId.ANCIENT_EGYPT);
        BossAttacks.Charge charge = new BossAttacks.Charge();
        charge.start(arena);
        int ticks = 0;
        while (!charge.tick(arena) && ticks < 120) {
            ticks++;
        }
        assertEquals(home, boss.getX(), 0.05);
        assertNull(session.getBoard().getPlantAt(3, 1));
        assertNull(session.getBoard().getPlantAt(4, 2));
    }

    @Test
    void bossDeathWinsMatch() {
        GameSession session = newBossSession(ChapterId.ANCIENT_EGYPT, new Random(4));
        BossHandler handler = new BossHandler(ChapterId.ANCIENT_EGYPT, List.of("Peashooter"), new Random(4));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        Zombie boss = handler.getBoss();
        while (boss.isAlive()) {
            if (boss.isStunned()) {
                boss.onTickUpdate(null);
                continue;
            }
            boss.takeDamage(boss.getHealth());
        }
        handler.onTick(session);
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void mowerDoesNotKillBoss() {
        GameSession session = newBossSession(ChapterId.ANCIENT_EGYPT, new Random(5));
        BossHandler handler = new BossHandler(ChapterId.ANCIENT_EGYPT, List.of("Peashooter"), new Random(5));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        Zombie boss = handler.getBoss();
        session.handleZombieReachedHouse(boss);
        assertTrue(boss.isAlive());
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void fireTileExpiresAfterFourSeconds() {
        GameSession session = newBossSession(ChapterId.DARK_AGES, new Random(6));
        session.getBoard().setTile(2, 1, new FireTile());
        session.start();
        session.advanceTicks(FireTile.DURATION_TICKS - 1);
        assertTrue(session.getBoard().getTile(2, 1).isFire());
        session.advanceTicks(1);
        assertFalse(session.getBoard().getTile(2, 1).isFire());
    }

    @Test
    void plantingOnFireTileIsBlocked() {
        GameSession session = newBossSession(ChapterId.DARK_AGES, new Random(9));
        BossHandler handler = new BossHandler(ChapterId.DARK_AGES, List.of("Peashooter"), new Random(9));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.getBoard().setTile(2, 1, new FireTile());
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.TILE_BLOCKED, session.tryPlant("Peashooter", 2, 1, 1));
    }

    @Test
    void fireballBurnsPlantedCellAndSpawnsImpOnIt() {
        GameSession session = newBossSession(ChapterId.DARK_AGES, new Random(10));
        BossHandler handler = new BossHandler(ChapterId.DARK_AGES, List.of("Peashooter"), new Random(10));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 2, 1, 1));
        assertNotNull(session.getBoard().getPlantAt(2, 1));

        BossArena arena = new BossArena(session, handler.getBoss(), new Random(10), ChapterId.DARK_AGES);
        BossAttacks.Fireball fireball = new BossAttacks.Fireball();
        fireball.start(arena);
        int ticks = 0;
        while (!fireball.tick(arena) && ticks < 80) {
            ticks++;
        }
        assertNull(session.getBoard().getPlantAt(2, 1));
        assertTrue(session.getBoard().getTile(2, 1).isFire());
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.TILE_BLOCKED, session.tryPlant("Peashooter", 2, 1, 1));
        assertTrue(hasImpOnCell(session, 2, 1));
    }

    @Test
    void dragonFireScorchesBothOccupiedRows() {
        GameSession session = newBossSession(ChapterId.DARK_AGES, new Random(11));
        BossHandler handler = new BossHandler(
                ChapterId.DARK_AGES, List.of("Peashooter", "Wall-nut"), new Random(11));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.addConveyorBeltPlant("Peashooter");
        session.addConveyorBeltPlant("Wall-nut");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 3, 1, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Wall-nut", 4, 2, 1));

        BossArena arena = new BossArena(session, handler.getBoss(), new Random(11), ChapterId.DARK_AGES);
        int[] rows = arena.occupiedRows();
        BossAttacks.DragonFire dragonFire = new BossAttacks.DragonFire();
        dragonFire.start(arena);
        int ticks = 0;
        while (!dragonFire.tick(arena) && ticks < 80) {
            ticks++;
        }
        assertNull(session.getBoard().getPlantAt(3, 1));
        assertNull(session.getBoard().getPlantAt(4, 2));
        for (int row : rows) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                assertTrue(session.getBoard().getTile(col, row).isFire(),
                        "expected fire at " + col + "," + row);
            }
        }
    }

    @Test
    void freezeColumnPlacesIceAndFrozenZombies() {
        GameSession session = newBossSession(ChapterId.FROSTBITE_CAVES, new Random(7));
        BossHandler handler = new BossHandler(ChapterId.FROSTBITE_CAVES, List.of("Peashooter"), new Random(7));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(7), ChapterId.FROSTBITE_CAVES);
        int spawned = arena.freezeColumn(3);
        assertEquals(session.getBoard().getRows(), spawned);
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            assertTrue(session.getBoard().getTile(3, row) instanceof IceTile);
        }
    }

    @Test
    void sharkSwallowsWaterPlant() {
        GameSession session = newBossSession(ChapterId.BIG_WAVE_BEACH, new Random(8));
        BossHandler handler = new BossHandler(ChapterId.BIG_WAVE_BEACH, List.of("Lily Pad"), new Random(8));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.getBoard().setTile(4, 2, new io.github.finalwave.model.game.board.tile.LowBeachTile());
        session.addConveyorBeltPlant("Lily Pad");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Lily Pad", 4, 2, 1));
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(8), ChapterId.BIG_WAVE_BEACH);
        int[] cell = new int[2];
        assertTrue(arena.swallowWaterPlant(cell));
        assertNull(session.getBoard().getPlantAt(4, 2));
    }

    private static boolean hasImpOnCell(GameSession session, int col, int row) {
        double expectedX = col + 0.5;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isBoss() || !zombie.isAlive()) {
                continue;
            }
            boolean dragon = "ZombieDarkImpDragon".equals(zombie.getType());
            boolean imp = "ZombieImp".equals(zombie.getType());
            if ((dragon || imp) && zombie.getRow() == row && Math.abs(zombie.getX() - expectedX) < 0.6) {
                return true;
            }
        }
        return false;
    }

    private static int countGraves(GameSession session) {
        int graves = 0;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                if (session.getBoard().getTile(col, row).isGrave()) {
                    graves++;
                }
            }
        }
        return graves;
    }
}
