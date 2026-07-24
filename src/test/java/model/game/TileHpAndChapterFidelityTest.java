package model.game;

import model.adventure.AdventureRegistry;
import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.adventure.ChapterRules;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.board.PlantPlacementResult;
import model.game.board.tile.GraveTile;
import model.game.board.tile.IceTile;
import model.game.board.tile.NecromancyTile;
import model.game.board.tile.NormalTile;
import model.game.entity.zombie.Zombie;
import model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TileHpAndChapterFidelityTest {

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
    void graveDamageClearsAtZeroAndGrantsLoot() {
        GameSession session = new GameSession(plantRegistry, 10);
        session.getBoard().setTile(3, 1, new GraveTile(GraveTile.Loot.SUN_50));
        assertTrue(session.damageGraveAt(3, 1, GraveTile.MAX_HEALTH - 1));
        assertTrue(session.getBoard().getTile(3, 1).isGrave());
        assertEquals(1, ((GraveTile) session.getBoard().getTile(3, 1)).getHealth());

        assertTrue(session.damageGraveAt(3, 1, 1));
        assertFalse(session.getBoard().getTile(3, 1).isGrave());
        assertInstanceOf(NormalTile.class, session.getBoard().getTile(3, 1));
        assertEquals(60, session.getSunBalance());
    }

    @Test
    void iceTakesNormalDamageAndClearsAtZero() {
        GameSession session = new GameSession(plantRegistry, 50);
        IceTile ice = new IceTile();
        session.getBoard().setTile(2, 2, ice);
        assertTrue(session.damageIceAt(2, 2, 100));
        assertEquals(IceTile.MAX_HEALTH - 100, ice.getHealth());
        assertTrue(session.damageIceAt(2, 2, IceTile.MAX_HEALTH));
        assertFalse(session.getBoard().getTile(2, 2).isIce());
    }

    @Test
    void adjacentFirePlantMeltsIceAtSixtyPerSecond() {
        GameSession session = new GameSession(plantRegistry, 500);
        session.start();
        IceTile ice = new IceTile();
        session.getBoard().setTile(3, 1, ice);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Torchwood", 4, 1, 1));

        int healthBefore = ice.getHealth();
        session.advanceTicks(10);
        assertEquals(healthBefore - IceTile.ADJACENT_FIRE_DAMAGE_PER_TICK * 10, ice.getHealth());
    }

    @Test
    void necromancyTileInheritsGraveHealth() {
        NecromancyTile tile = new NecromancyTile(GraveTile.Loot.PLANT_FOOD);
        assertEquals(GraveTile.MAX_HEALTH, tile.getHealth());
        assertTrue(tile.blocksProjectiles());
        tile.takeDamage(50);
        assertEquals(GraveTile.MAX_HEALTH - 50, tile.getHealth());
    }

    @Test
    void beachRulesEnableLowBeachEmerge() {
        assertTrue(ChapterRules.bigWaveBeach().hasLowBeachEmerge());
        assertFalse(ChapterRules.ancientEgypt().hasLowBeachEmerge());
    }

    @Test
    void beachWaveSpawnsEmergesFromWater() {
        ChapterConfig beach = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        AdventureMode mode = new AdventureMode(
                beach, beach.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(42));
        GameSession session = mode.createSession();
        session.start();
        int before = session.getZombies().size();
        mode.onWaveStarted(session, 1);
        assertTrue(session.getZombies().size() > before);
    }

    @Test
    void darkAgesNecromancySpawnsNearGraveOnWave() {
        ChapterConfig dark = AdventureRegistry.getInstance().getChapter(ChapterId.DARK_AGES);
        AdventureMode mode = new AdventureMode(
                dark, dark.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(7));
        GameSession session = mode.createSession();
        session.start();
        session.getBoard().setTile(4, 2, new NecromancyTile());
        int before = session.getZombies().size();
        mode.onWaveStarted(session, 1);
        assertTrue(session.getZombies().size() > before);
        boolean foundNearGrave = false;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getRow() == 2 && Math.abs(zombie.getX() - 4.5) < 0.01) {
                foundNearGrave = true;
                break;
            }
        }
        assertTrue(foundNearGrave);
    }

    @Test
    void tileStatusIncludesGraveAndIceHealth() {
        GameSession session = new GameSession(plantRegistry, 50);
        session.getBoard().setTile(1, 1, new GraveTile());
        session.getBoard().setTile(2, 1, new IceTile());
        String graveStatus = session.renderTileStatus(1, 1);
        String iceStatus = session.renderTileStatus(2, 1);
        assertTrue(graveStatus.contains("HP=" + GraveTile.MAX_HEALTH));
        assertTrue(iceStatus.contains("HP=" + IceTile.MAX_HEALTH));
    }
}
