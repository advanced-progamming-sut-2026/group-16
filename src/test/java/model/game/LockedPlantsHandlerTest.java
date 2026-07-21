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
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LockedPlantsHandlerTest {

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

    private GameSession newLockedPlantsSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(3), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    @Test
    void onLevelStartActivatesLockedSetOnSession() {
        LockedPlantsRules rules = new LockedPlantsRules(
                LockedPlantsMode.FAMILY, Set.of("Twin Sunflower", "Repeater"), Set.of("Sunflower", "Peashooter"));
        GameSession session = newLockedPlantsSession(new Random(1));
        LockedPlantsHandler handler = new LockedPlantsHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertEquals(Set.of("Twin Sunflower", "Repeater"), session.getLevelLockedPlants());
    }

    @Test
    void tryPlantReturnsLevelPlantLockedForLockedPlantInLoadout() {
        LockedPlantsRules rules = new LockedPlantsRules(
                LockedPlantsMode.FAMILY, Set.of("Repeater"), Set.of("Peashooter"));
        GameSession session = newLockedPlantsSession(new Random(2));
        session.setSelectedLoadout(Set.of("Peashooter", "Repeater"));
        LockedPlantsHandler handler = new LockedPlantsHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(PlantPlacementResult.LEVEL_PLANT_LOCKED, session.tryPlant("Repeater", 0, 0, 1));
    }

    @Test
    void tryPlantSucceedsForAllowedPlantInLoadout() {
        LockedPlantsRules rules = new LockedPlantsRules(
                LockedPlantsMode.FAMILY, Set.of("Repeater"), Set.of("Peashooter"));
        GameSession session = newLockedPlantsSession(new Random(3));
        session.setSelectedLoadout(Set.of("Peashooter"));
        LockedPlantsHandler handler = new LockedPlantsHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
    }

    @Test
    void lockedPlantRejectedEvenIfStillInLoadout() {
        LockedPlantsRules rules = new LockedPlantsRules(
                LockedPlantsMode.SPECIFIC, Set.of("Sunflower"), Set.of());
        GameSession session = newLockedPlantsSession(new Random(4));
        session.setSelectedLoadout(Set.of("Sunflower", "Peashooter"));
        LockedPlantsHandler handler = new LockedPlantsHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertTrue(session.getSelectedLoadout().contains("Sunflower"));
        assertEquals(PlantPlacementResult.LEVEL_PLANT_LOCKED, session.tryPlant("Sunflower", 0, 0, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 1, 0, 1));
    }
}
