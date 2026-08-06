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
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ConveyBeltHandlerTest {

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

    private GameSession newConveyorBeltSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(2), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    @Test
    void firstPlantArrivesImmediatelyOnLevelStart() {
        GameSession session = newConveyorBeltSession(new Random(1));
        ConveyBeltHandler handler = new ConveyBeltHandler(List.of("Peashooter", "Sunflower"), new Random(1));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertTrue(session.isConveyorBeltActive());
        assertEquals(1, session.getConveyorBeltPlants().size());
    }

    @Test
    void plantNotYetOnBeltCannotBePlanted() {
        GameSession session = newConveyorBeltSession(new Random(2));
        ConveyBeltHandler handler = new ConveyBeltHandler(List.of("Peashooter"), new Random(2));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(PlantPlacementResult.NOT_ON_CONVEYOR_BELT,
                session.tryPlant("Sunflower", 0, 0, 1));
    }

    @Test
    void secondPlantArrivesAfter12Seconds() {
        GameSession session = newConveyorBeltSession(new Random(3));
        ConveyBeltHandler handler = new ConveyBeltHandler(List.of("Peashooter", "Sunflower"), new Random(3));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(1, session.getConveyorBeltPlants().size());
        session.advanceTicks(ConveyBeltHandler.DROP_INTERVAL_TICKS - 1);
        assertEquals(1, session.getConveyorBeltPlants().size());
        session.advanceTicks(1);
        assertEquals(2, session.getConveyorBeltPlants().size());
    }

    @Test
    void plantingBeltPlantSucceedsAndConsumesIt() {
        GameSession session = newConveyorBeltSession(new Random(4));
        ConveyBeltHandler handler = new ConveyBeltHandler(List.of("Peashooter"), new Random(4));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertEquals(List.of("Peashooter"), session.getConveyorBeltPlants());
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
        assertTrue(session.getConveyorBeltPlants().isEmpty());
        assertEquals(PlantPlacementResult.NOT_ON_CONVEYOR_BELT,
                session.tryPlant("Peashooter", 1, 0, 1));
    }

    @Test
    void emptyPlantPoolLeavesBeltInactive() {
        GameSession session = newConveyorBeltSession(new Random(5));
        ConveyBeltHandler handler = new ConveyBeltHandler(List.of(), new Random(5));
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertFalse(session.isConveyorBeltActive());
        assertTrue(session.getConveyorBeltPlants().isEmpty());
        session.advanceTicks(ConveyBeltHandler.DROP_INTERVAL_TICKS * 2);
        assertTrue(session.getConveyorBeltPlants().isEmpty());
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 0, 0, 1));
    }
}
