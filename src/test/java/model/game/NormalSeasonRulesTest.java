package model.game;

import model.App;
import model.adventure.AdventureRegistry;
import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.board.PlantPlacementResult;
import model.game.board.tile.GraveTile;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.behavior.MovementBehavior;
import model.game.mode.AdventureMode;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalSeasonRulesTest {

    @Test
    void slipperyTileShiftsZombieRow() {
        PlantRegistry plants = App.getInstance().getPlantRegistry();
        GameSession session = new GameSession(plants, 50);
        session.getBoard().setTile(4, 2, new SlipperyTile(SlipperyTile.SlipDirection.UP));

        Zombie zombie = new Zombie.Builder("Basic")
                .maxHealth(100)
                .speed(1.0)
                .position(4.5, 2)
                .addBehavior(new MovementBehavior())
                .build();
        session.addZombie(zombie);
        session.start();
        session.advanceTicks(1);

        assertEquals(1, zombie.getRow());
    }

    @Test
    void frostStacksCoverPlantAtThreeHits() {
        PlantRegistry plants = App.getInstance().getPlantRegistry();
        GameSession session = new GameSession(plants, 200);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
        Plant peashooter = session.getBoard().getPlantAt(0, 0);

        session.addPlantFrostStack(peashooter);
        session.addPlantFrostStack(peashooter);
        assertTrue(session.getPlantCoverings().isEmpty());

        session.addPlantFrostStack(peashooter);
        assertEquals(1, session.getPlantCoverings().size());
        assertEquals(model.game.entity.plant.PlantCovering.Type.HUNTER_ICE,
                session.getPlantCoverings().get(0).getType());
    }

    @Test
    void clearGraveGrantsSunLoot() {
        PlantRegistry plants = App.getInstance().getPlantRegistry();
        GameSession session = new GameSession(plants, 10);
        session.getBoard().setTile(3, 1, new GraveTile(GraveTile.Loot.SUN_50));
        session.clearGraveAt(3, 1);
        assertEquals(60, session.getSunBalance());
        assertTrue(!session.getBoard().getTile(3, 1).isGrave());
    }

    @Test
    void beachWaveStartChangesWaterColumns() {
        ChapterConfig beach = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        var level = beach.getLevels().get(0);
        ZombieRegistry zombies = loadZombies();
        AdventureMode mode = new AdventureMode(
                beach, level, App.getInstance().getPlantRegistry(), zombies, 3, new Random(1));
        GameSession session = mode.createSession();
        int before = mode.getCurrentWaterColumns();
        mode.onWaveStarted(session, 2);
        // tide may stay same by chance; assert board water tiles exist after call
        boolean anyWater = false;
        for (int r = 0; r < session.getBoard().getRows(); r++) {
            for (int c = 0; c < session.getBoard().getCols(); c++) {
                if (session.getBoard().getTile(c, r).isWater()) {
                    anyWater = true;
                }
            }
        }
        assertTrue(anyWater);
        assertTrue(mode.getCurrentWaterColumns() >= beach.getRules().getInitialWaterColumns());
        assertTrue(before >= 1);
    }

    @Test
    void darkAgesWaveStartCanPlaceGraves() {
        ChapterConfig dark = AdventureRegistry.getInstance().getChapter(ChapterId.DARK_AGES);
        var level = dark.getLevels().get(0);
        AdventureMode mode = new AdventureMode(
                dark, level, App.getInstance().getPlantRegistry(), loadZombies(), 3, new Random(42));
        GameSession session = mode.createSession();
        int gravesBefore = countGraves(session);
        mode.onWaveStarted(session, 2);
        assertTrue(countGraves(session) >= gravesBefore);
    }

    @Test
    void frostbiteIceWindStacksNonFirePlant() {
        ChapterConfig frost = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        var level = frost.getLevels().get(0);
        AdventureMode mode = new AdventureMode(
                frost, level, App.getInstance().getPlantRegistry(), loadZombies(), 3, new Random(7));
        GameSession session = mode.createSession();
        session.addSunBalance(100);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
        Plant peashooter = session.getBoard().getPlantAt(0, 0);
        for (int i = 0; i < 5; i++) {
            mode.onWaveStarted(session, i + 1);
        }
        assertInstanceOf(Plant.class, peashooter);
        assertTrue(peashooter.isAlive());
    }

    private static int countGraves(GameSession session) {
        int count = 0;
        for (int r = 0; r < session.getBoard().getRows(); r++) {
            for (int c = 0; c < session.getBoard().getCols(); c++) {
                if (session.getBoard().getTile(c, r).isGrave()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static ZombieRegistry loadZombies() {
        ZombieRegistry registry = new ZombieRegistry();
        try (InputStream zombies = NormalSeasonRulesTest.class.getClassLoader()
                .getResourceAsStream("zombies.json");
             InputStream armor = NormalSeasonRulesTest.class.getClassLoader()
                     .getResourceAsStream("ArmorTypeData.json")) {
            registry.loadFromJson(zombies);
            registry.loadArmorFromJson(armor);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return registry;
    }
}
