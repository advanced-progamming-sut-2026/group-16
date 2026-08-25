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
import io.github.finalwave.model.game.boss.BossVfx;
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
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 3, 1, 1));
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(7), ChapterId.FROSTBITE_CAVES);
        int spawned = arena.freezeColumn(3);
        assertEquals(session.getBoard().getRows(), spawned);
        assertNull(session.getBoard().getPlantAt(3, 1));
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            assertTrue(session.getBoard().getTile(3, row) instanceof IceTile);
        }
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.TILE_BLOCKED, session.tryPlant("Peashooter", 3, 1, 1));
        assertEquals(session.getBoard().getRows(), countFrozenMinionsInColumn(session, 3));
    }

    @Test
    void iceMissileDestroysPlantWithoutGraves() {
        GameSession session = newBossSession(ChapterId.FROSTBITE_CAVES, new Random(12));
        BossHandler handler = new BossHandler(ChapterId.FROSTBITE_CAVES, List.of("Peashooter"), new Random(12));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 2, 1, 1));
        int gravesBefore = countGraves(session);

        BossArena arena = new BossArena(session, handler.getBoss(), new Random(12), ChapterId.FROSTBITE_CAVES);
        BossAttacks.Missile missile = new BossAttacks.Missile(false);
        missile.start(arena);
        int ticks = 0;
        while (!missile.tick(arena) && ticks < 90) {
            ticks++;
        }
        assertNull(session.getBoard().getPlantAt(2, 1));
        assertEquals(gravesBefore, countGraves(session));
    }

    @Test
    void iceWindEncasesPlantsOnTwoRowsAndSkipsFire() {
        GameSession session = newBossSession(ChapterId.FROSTBITE_CAVES, new Random(13));
        BossHandler handler = new BossHandler(
                ChapterId.FROSTBITE_CAVES, List.of("Peashooter", "Pepper-pult"), new Random(13));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.addConveyorBeltPlant("Peashooter");
        session.addConveyorBeltPlant("Peashooter");
        session.addConveyorBeltPlant("Pepper-pult");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 2, 1, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 3, 2, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pepper-pult", 4, 1, 1));

        BossArena arena = new BossArena(session, handler.getBoss(), new Random(13), ChapterId.FROSTBITE_CAVES);
        int hit = arena.applyIceWindOnRows(new int[]{1, 2}, BossCatalog.ICE_WIND_FROST_STACKS);
        assertEquals(2, hit);
        assertTrue(session.getBoard().getPlantAt(2, 1).getHostileIceStacks(null) >= 3
                || hasIceCovering(session, session.getBoard().getPlantAt(2, 1)));
        assertTrue(session.getBoard().getPlantAt(3, 2).getHostileIceStacks(null) >= 3
                || hasIceCovering(session, session.getBoard().getPlantAt(3, 2)));
        assertEquals(0, session.getBoard().getPlantAt(4, 1).getHostileIceStacks(null));
        assertFalse(hasIceCovering(session, session.getBoard().getPlantAt(4, 1)));
    }

    @Test
    void iceWindAttackSweepsTwoAdjacentRows() {
        GameSession session = newBossSession(ChapterId.FROSTBITE_CAVES, new Random(14));
        BossHandler handler = new BossHandler(ChapterId.FROSTBITE_CAVES, List.of("Peashooter"), new Random(14));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            session.addConveyorBeltPlant("Peashooter");
            assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 2, row, 1));
        }
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(14), ChapterId.FROSTBITE_CAVES);
        BossAttacks.IceWind wind = new BossAttacks.IceWind();
        wind.start(arena);
        int windVfx = 0;
        for (BossVfx vfx : session.drainBossVfx()) {
            if (vfx.kind() == BossVfx.Kind.ICE_WIND) {
                windVfx++;
            }
        }
        assertEquals(2, windVfx);
        int ticks = 0;
        while (!wind.tick(arena) && ticks < 50) {
            ticks++;
        }
        int frozenRows = 0;
        int lastFrozen = -2;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            var plant = session.getBoard().getPlantAt(2, row);
            if (plant != null && (plant.getHostileIceStacks(null) >= 3 || hasIceCovering(session, plant))) {
                if (frozenRows > 0) {
                    assertEquals(lastFrozen + 1, row);
                }
                lastFrozen = row;
                frozenRows++;
            }
        }
        assertEquals(2, frozenRows);
    }

    @Test
    void freezeColumnAttackEncasesAColumn() {
        GameSession session = newBossSession(ChapterId.FROSTBITE_CAVES, new Random(15));
        BossHandler handler = new BossHandler(ChapterId.FROSTBITE_CAVES, List.of("Peashooter"), new Random(15));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(15), ChapterId.FROSTBITE_CAVES);
        BossAttacks.FreezeColumn freeze = new BossAttacks.FreezeColumn();
        freeze.start(arena);
        assertTrue(handler.getBoss().getPresentationClip().startsWith("glacier_"));
        int ticks = 0;
        while (!freeze.tick(arena) && ticks < 80) {
            ticks++;
        }
        int icedColumns = 0;
        int icedCol = -1;
        for (int col = 0; col < session.getBoard().getCols(); col++) {
            boolean allIce = true;
            for (int row = 0; row < session.getBoard().getRows(); row++) {
                if (!(session.getBoard().getTile(col, row) instanceof IceTile)) {
                    allIce = false;
                    break;
                }
            }
            if (allIce) {
                icedColumns++;
                icedCol = col;
            }
        }
        assertEquals(1, icedColumns);
        assertEquals(session.getBoard().getRows(), countFrozenMinionsInColumn(session, icedCol));
    }

    @Test
    void frostbiteBossStaysOnTwoRowsWithoutSummon() {
        assertFalse(BossCatalog.allowsSummon(ChapterId.FROSTBITE_CAVES));
        assertFalse(BossCatalog.allowsLaneSwitch(ChapterId.FROSTBITE_CAVES));
        assertEquals(BossCatalog.ICE_INTRO_TICKS, BossCatalog.introTicks(ChapterId.FROSTBITE_CAVES));
        GameSession session = newBossSession(ChapterId.FROSTBITE_CAVES, new Random(16));
        BossHandler handler = new BossHandler(ChapterId.FROSTBITE_CAVES, List.of("Peashooter"), new Random(16));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        Zombie boss = handler.getBoss();
        assertEquals("ZombieIceageZomboss", boss.getType());
        assertTrue(boss.occupiesRow(boss.getRow()));
        assertTrue(boss.occupiesRow(boss.getRow() + 1));
        assertEquals(2, countOccupiedRows(boss, session.getBoard().getRows()));
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

    @Test
    void sharkBiteSendsSeveralSharksAtWaterPlants() {
        GameSession session = newBossSession(ChapterId.BIG_WAVE_BEACH, new Random(8));
        BossHandler handler = new BossHandler(ChapterId.BIG_WAVE_BEACH, List.of("Lily Pad"), new Random(8));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        int waterFrom = session.getBoard().getCols() - 3;
        for (int i = 0; i < 3; i++) {
            session.addConveyorBeltPlant("Lily Pad");
            int col = waterFrom + i;
            assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Lily Pad", col, i, 1));
        }
        int plantsBefore = countWaterPlants(session);
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(8), ChapterId.BIG_WAVE_BEACH);
        BossAttacks.SharkBite bite = new BossAttacks.SharkBite();
        bite.start(arena);
        assertEquals("spawn", handler.getBoss().getPresentationClip());
        int sharks = 0;
        for (BossVfx vfx : session.drainBossVfx()) {
            if (vfx.kind() == BossVfx.Kind.SHARK) {
                sharks++;
            }
        }
        assertTrue(sharks >= 2);
        assertTrue(sharks <= plantsBefore);
        int ticks = 0;
        while (!bite.tick(arena) && ticks < 50) {
            ticks++;
        }
        assertEquals(plantsBefore - sharks, countWaterPlants(session));
    }

    @Test
    void turbinePullsThenClearsOccupiedRows() {
        GameSession session = newBossSession(ChapterId.BIG_WAVE_BEACH, new Random(18));
        BossHandler handler = new BossHandler(
                ChapterId.BIG_WAVE_BEACH, List.of("Peashooter"), new Random(18));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        int[] rows = new BossArena(session, handler.getBoss(), new Random(18), ChapterId.BIG_WAVE_BEACH)
                .occupiedRows();
        session.addConveyorBeltPlant("Peashooter");
        session.addConveyorBeltPlant("Peashooter");
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 2, rows[0], 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 3, rows[1], 1));
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(18), ChapterId.BIG_WAVE_BEACH);
        BossAttacks.Vacuum vacuum = new BossAttacks.Vacuum();
        vacuum.start(arena);
        assertEquals("suction_on", handler.getBoss().getPresentationClip());
        int turbineVfx = 0;
        for (BossVfx vfx : session.drainBossVfx()) {
            if (vfx.kind() == BossVfx.Kind.VACUUM) {
                turbineVfx++;
            }
        }
        assertEquals(2, turbineVfx);
        vacuum.tick(arena);
        vacuum.tick(arena);
        var moved = session.getBoard().getPlantAt(3, rows[0]);
        assertNotNull(moved);
        int ticks = 2;
        while (!vacuum.tick(arena) && ticks < 90) {
            ticks++;
        }
        for (int row : rows) {
            for (var plant : session.getBoard().getAllPlants()) {
                assertNotEquals(row, plant.getRow());
            }
        }
    }

    @Test
    void turbinePullsZombiesIntoTheMouth() {
        GameSession session = newBossSession(ChapterId.BIG_WAVE_BEACH, new Random(19));
        BossHandler handler = new BossHandler(ChapterId.BIG_WAVE_BEACH, List.of("Lily Pad"), new Random(19));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        BossArena arena = new BossArena(session, handler.getBoss(), new Random(19), ChapterId.BIG_WAVE_BEACH);
        int row = arena.occupiedRows()[0];
        Zombie minion = arena.spawnMinion("ZombieDefault", row, 2.5);
        assertNotNull(minion);
        double startX = minion.getX();
        BossAttacks.Vacuum vacuum = new BossAttacks.Vacuum();
        vacuum.start(arena);
        vacuum.tick(arena);
        vacuum.tick(arena);
        assertTrue(minion.getX() > startX);
        int ticks = 2;
        while (!vacuum.tick(arena) && ticks < 90) {
            ticks++;
        }
        assertFalse(minion.isAlive());
    }

    @Test
    void beachBossUsesSharkIntroAndIdle() {
        assertEquals(BossCatalog.BEACH_INTRO_TICKS, BossCatalog.introTicks(ChapterId.BIG_WAVE_BEACH));
        GameSession session = newBossSession(ChapterId.BIG_WAVE_BEACH, new Random(20));
        BossHandler handler = new BossHandler(ChapterId.BIG_WAVE_BEACH, List.of("Lily Pad"), new Random(20));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        Zombie boss = handler.getBoss();
        assertEquals("ZombieBeachZomboss", boss.getType());
        assertTrue(boss.occupiesRow(boss.getRow()));
        assertTrue(boss.occupiesRow(boss.getRow() + 1));
        assertEquals(2, countOccupiedRows(boss, session.getBoard().getRows()));
    }

    private static int countWaterPlants(GameSession session) {
        int count = 0;
        for (var plant : session.getBoard().getAllPlants()) {
            if (plant == null || !plant.isAlive()) {
                continue;
            }
            var tile = session.getBoard().getTile(plant.getCol(), plant.getRow());
            if (tile != null && tile.isWater()) {
                count++;
            }
        }
        return count;
    }

    private static int countOccupiedRows(Zombie boss, int rows) {
        int occupied = 0;
        for (int row = 0; row < rows; row++) {
            if (boss.occupiesRow(row)) {
                occupied++;
            }
        }
        return occupied;
    }

    private static boolean hasIceCovering(GameSession session, io.github.finalwave.model.game.entity.plant.Plant plant) {
        if (plant == null) {
            return false;
        }
        for (var covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getCoveredPlant() == plant
                    && covering.getType() == io.github.finalwave.model.game.entity.plant.PlantCovering.Type.HUNTER_ICE) {
                return true;
            }
        }
        return false;
    }

    private static int countFrozenMinionsInColumn(GameSession session, int col) {
        int frozen = 0;
        double expectedX = col + 0.5;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isBoss() || !zombie.isAlive()) {
                continue;
            }
            if (zombie.getFreezeTicksRemaining() >= 40 && Math.abs(zombie.getX() - expectedX) < 0.6) {
                frozen++;
            }
        }
        return frozen;
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
