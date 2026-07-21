package model.game;

import model.adventure.AdventureRegistry;
import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.board.PlantPlacementResult;
import model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class NightOpsHandlerTest {

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

    private GameSession newNightOpsSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(2), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    private GameSession newBeachNormalSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(1), plantRegistry, zombieRegistry, 1, random);
        return mode.createSession();
    }

    @Test
    void onLevelStartDisablesSkySun() {
        GameSession session = newBeachNormalSession(new Random(1));
        assertTrue(session.getSkySunSystem().isEnabled());

        NightOpsHandler handler = new NightOpsHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertFalse(session.getSkySunSystem().isEnabled());
    }

    @Test
    void noSkySunAfterManyTicks() {
        GameSession session = newNightOpsSession(new Random(2));
        NightOpsHandler handler = new NightOpsHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(0, session.getSunItems().size());
        session.advanceTicks(600);
        assertEquals(0, session.getSunItems().size());
    }

    @Test
    void sessionIsNightLevel() {
        GameSession session = newNightOpsSession(new Random(3));
        assertTrue(session.isNightLevel());
    }

    @Test
    void plantSunProductionStillWorks() {
        GameSession session = newNightOpsSession(new Random(4));
        NightOpsHandler handler = new NightOpsHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Sunflower", 2, 2, 1));
        int before = session.getSunItems().size();
        session.advanceTicks(240);
        assertTrue(session.getSunItems().size() > before);

        int balanceBefore = session.getSunBalance();
        assertTrue(session.collectSun(session.getSunItems().getFirst()));
        assertTrue(session.getSunBalance() > balanceBefore);
    }

    @Test
    void beachLevel1StillHasSkySun() {
        GameSession session = newBeachNormalSession(new Random(5));
        assertFalse(session.isNightLevel());
        assertTrue(session.getSkySunSystem().isEnabled());
    }

    @Test
    void nightOpsSessionCreatedWithSkySunDisabled() {
        GameSession session = newNightOpsSession(new Random(6));
        assertFalse(session.getSkySunSystem().isEnabled());
    }
}
