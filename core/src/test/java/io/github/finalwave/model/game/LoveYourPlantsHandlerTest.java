package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoveYourPlantsHandlerTest {

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

    private GameSession newLoveYourPlantsSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.DARK_AGES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(2), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    private Plant plantSunflower(GameSession session, int index) {
        session.getCooldownTracker().resetAll();
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                if (session.getBoard().getPlantAt(col, row) != null) {
                    continue;
                }
                PlantPlacementResult result = session.tryPlant("Sunflower", col, row, 1);
                if (result == PlantPlacementResult.SUCCESS) {
                    return session.getBoard().getPlantAt(col, row);
                }
                session.getCooldownTracker().resetAll();
            }
        }
        throw new AssertionError("Could not find free tile for plant #" + index);
    }

    @Test
    void onLevelStartActivatesLoveYourPlantsOnSession() {
        GameSession session = newLoveYourPlantsSession(new Random(1));
        LoveYourPlantsHandler handler = new LoveYourPlantsHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertTrue(session.isLoveYourPlantsActive());
        assertEquals(5, session.getLoveYourPlantsMaxLoss());
        assertEquals(5, session.getLoveYourPlantsRemaining());
    }

    @Test
    void fourPlantLossesDoNotEndMatch() {
        GameSession session = newLoveYourPlantsSession(new Random(2));
        LoveYourPlantsHandler handler = new LoveYourPlantsHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        for (int i = 0; i < 4; i++) {
            Plant plant = plantSunflower(session, i);
            assertTrue(session.removePlantFromBoard(plant, true));
        }

        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
        assertEquals(4, session.getPlantsLost());
        assertEquals(1, session.getLoveYourPlantsRemaining());
    }

    @Test
    void fifthPlantLossTriggersLoss() {
        GameSession session = newLoveYourPlantsSession(new Random(3));
        LoveYourPlantsHandler handler = new LoveYourPlantsHandler();
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        for (int i = 0; i < 5; i++) {
            Plant plant = plantSunflower(session, i);
            assertTrue(session.removePlantFromBoard(plant, true));
        }

        assertEquals(MatchResult.LOST, session.getMatchResult());
        assertEquals(5, session.getPlantsLost());
    }

    @Test
    void shovelDoesNotCountTowardLimit() {
        GameSession session = newLoveYourPlantsSession(new Random(4));
        LoveYourPlantsHandler handler = new LoveYourPlantsHandler(1);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        Plant plant = plantSunflower(session, 0);
        assertTrue(session.removePlantFromBoard(plant, false));

        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
        assertEquals(0, session.getPlantsLost());
    }

    @Test
    void limitReachedNotifiesMatchListener() {
        GameSession session = newLoveYourPlantsSession(new Random(5));
        AtomicBoolean notified = new AtomicBoolean(false);
        AtomicInteger lost = new AtomicInteger();
        AtomicInteger max = new AtomicInteger();
        session.setMatchListener(new MatchListener() {
            @Override
            public void onLoveYourPlantsLimitReached(int plantsLost, int maxAllowed) {
                notified.set(true);
                lost.set(plantsLost);
                max.set(maxAllowed);
            }
        });

        LoveYourPlantsHandler handler = new LoveYourPlantsHandler(2);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        for (int i = 0; i < 2; i++) {
            Plant plant = plantSunflower(session, i);
            assertTrue(session.removePlantFromBoard(plant, true));
        }

        assertTrue(notified.get());
        assertEquals(2, lost.get());
        assertEquals(2, max.get());
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }
}
