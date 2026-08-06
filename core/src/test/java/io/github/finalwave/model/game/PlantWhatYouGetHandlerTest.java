package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class PlantWhatYouGetHandlerTest {

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

    private GameSession newPlantWhatYouGetSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.DARK_AGES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(3), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setSelectedLoadout(Set.of("Peashooter", "Wall-nut"));
        session.addSunBalance(1000);
        return session;
    }

    private int firstPlantableCol(GameSession session, int row) {
        for (int col = 0; col < session.getBoard().getCols(); col++) {
            if (session.getBoard().getPlantAt(col, row) == null
                    && !session.getBoard().getTile(col, row).isGrave()) {
                return col;
            }
        }
        throw new AssertionError("No free tile in row " + row);
    }

    @Test
    void onLevelStartSetsSunDisablesSkyAndPrep() {
        GameSession session = newPlantWhatYouGetSession(new Random(1));
        PlantWhatYouGetHandler handler = new PlantWhatYouGetHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertEquals(500, session.getSunBalance());
        assertFalse(session.getSkySunSystem().isEnabled());
        assertTrue(session.isPlantWhatYouGetActive());
        assertTrue(session.isPrepPhaseActive());
        assertFalse(session.isWavesAutoStart());
    }

    @Test
    void prepPhaseAllowsImmediateReplantSameType() {
        GameSession session = newPlantWhatYouGetSession(new Random(2));
        PlantWhatYouGetHandler handler = new PlantWhatYouGetHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        int col1 = firstPlantableCol(session, 0);
        int col2 = firstPlantableCol(session, 1);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", col1, 0, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", col2, 1, 1));
    }

    @Test
    void afterWavesStartCooldownApplies() {
        GameSession session = newPlantWhatYouGetSession(new Random(3));
        PlantWhatYouGetHandler handler = new PlantWhatYouGetHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.startZombieWaves();

        int col1 = firstPlantableCol(session, 0);
        int col2 = firstPlantableCol(session, 1);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", col1, 0, 1));
        assertEquals(PlantPlacementResult.ON_COOLDOWN, session.tryPlant("Peashooter", col2, 1, 1));
    }

    @Test
    void startZombieWavesEndsPrepAndNotifies() {
        GameSession session = newPlantWhatYouGetSession(new Random(4));
        AtomicBoolean notified = new AtomicBoolean(false);
        session.setMatchListener(new MatchListener() {
            @Override
            public void onPlantWhatYouGetWavesStarted() {
                notified.set(true);
            }
        });

        PlantWhatYouGetHandler handler = new PlantWhatYouGetHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.startZombieWaves();

        assertFalse(session.isPrepPhaseActive());
        assertTrue(notified.get());
        assertTrue(session.getWaveManager().areWavesStarted());
    }
}
