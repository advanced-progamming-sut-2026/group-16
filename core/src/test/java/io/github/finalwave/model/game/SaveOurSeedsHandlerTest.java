package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SaveOurSeedsHandlerTest {

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

    private GameSession newSosSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(2), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    @Test
    void onLevelStartPlacesProtectedSeedsOnBoard() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig level = chapter.getLevel(2);
        SaveOurSeedsLayout layout = SaveOurSeedsLayoutFactory.create(chapter, level);
        GameSession session = newSosSession(new Random(1));
        SaveOurSeedsHandler handler = new SaveOurSeedsHandler(layout);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertEquals(5, session.getProtectedSeedPlacements().size());
        for (int row = 0; row < 5; row++) {
            Plant seed = session.getBoard().getPlantAt(2, row);
            assertNotNull(seed);
            assertEquals("Wall-nut", seed.getName());
            assertTrue(session.isProtectedSeed(seed));
        }
        assertEquals(List.of(0, 1, 2, 3, 4), session.getDangerRows());
    }

    @Test
    void destroyingProtectedSeedCausesImmediateLoss() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        SaveOurSeedsLayout layout = SaveOurSeedsLayoutFactory.create(chapter, chapter.getLevel(2));
        GameSession session = newSosSession(new Random(2));
        SaveOurSeedsHandler handler = new SaveOurSeedsHandler(layout);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        Plant seed = session.getBoard().getPlantAt(2, 1);
        assertNotNull(seed);
        assertTrue(session.removePlantFromBoard(seed, true));
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void destroyingNonProtectedPlantDoesNotLose() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        SaveOurSeedsLayout layout = SaveOurSeedsLayoutFactory.create(chapter, chapter.getLevel(2));
        GameSession session = newSosSession(new Random(3));
        session.setSelectedLoadout(Set.of("Peashooter"));
        SaveOurSeedsHandler handler = new SaveOurSeedsHandler(layout);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
        Plant playerPlant = session.getBoard().getPlantAt(0, 0);
        assertNotNull(playerPlant);
        assertFalse(session.isProtectedSeed(playerPlant));
        assertTrue(session.removePlantFromBoard(playerPlant, true));
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void pluckProtectedSeedFailsAndPlantRemains() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        SaveOurSeedsLayout layout = SaveOurSeedsLayoutFactory.create(chapter, chapter.getLevel(2));
        GameSession session = newSosSession(new Random(4));
        SaveOurSeedsHandler handler = new SaveOurSeedsHandler(layout);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertFalse(session.pluckPlant(2, 1));
        assertNotNull(session.getBoard().getPlantAt(2, 1));
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void emptyLayoutPlacesNothingAndDoesNotLoseOnPlantDeath() {
        GameSession session = newSosSession(new Random(5));
        session.setSelectedLoadout(Set.of("Peashooter"));
        SaveOurSeedsHandler handler = new SaveOurSeedsHandler(new SaveOurSeedsLayout(List.of()));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertTrue(session.getProtectedSeedPlacements().isEmpty());
        assertNull(session.getBoard().getPlantAt(2, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
        Plant plant = session.getBoard().getPlantAt(0, 0);
        assertTrue(session.removePlantFromBoard(plant, true));
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }
}
