package io.github.finalwave.model.game;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.LowBeachTile;
import io.github.finalwave.model.game.entity.plant.Plant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {

    private PlantRegistry registry;
    private GameBoard board;
    private GameSession session;

    @BeforeEach
    void setUp() throws IOException {
        registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        board = new GameBoard();
        session = new GameSession(registry, board, 500);
        session.start();
    }

    @Test
    void placesShooterOnNormalTile() {
        PlantPlacementResult result = session.tryPlant("Peashooter", 2, 2, 1);
        assertEquals(PlantPlacementResult.SUCCESS, result);
        Plant plant = board.getPlantAt(2, 2);
        assertNotNull(plant);
        assertEquals("Peashooter", plant.getName());
    }

    @Test
    void rejectsWhenInsufficientSun() {
        GameSession poorSession = new GameSession(registry, new GameBoard(), 0);
        poorSession.start();
        assertEquals(PlantPlacementResult.INSUFFICIENT_SUN,
                poorSession.tryPlant("Peashooter", 1, 1, 1));
    }

    @Test
    void stacksPumpkinOverShooter() {
        session.tryPlant("Peashooter", 3, 1, 1);
        PlantPlacementResult stackResult = session.tryPlant("Pumpkin", 3, 1, 1);
        assertEquals(PlantPlacementResult.SUCCESS, stackResult);
        assertNotNull(board.getGroundPlantAt(3, 1));
        assertNotNull(board.getOverlayPlantAt(3, 1));
    }

    @Test
    void peaPodStacksOnItself() {
        session.setSunBalance(9990);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pea Pod", 2, 2, 1));
        Plant first = board.getGroundPlantAt(2, 2);
        assertNotNull(first);
        assertEquals(1, first.getStackCount());
        session.getCooldownTracker().resetAll();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pea Pod", 2, 2, 1));
        assertEquals(2, first.getStackCount());
        assertNull(board.getOverlayPlantAt(2, 2));
        for (int i = 0; i < 3; i++) {
            session.getCooldownTracker().resetAll();
            assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pea Pod", 2, 2, 1));
        }
        assertEquals(5, first.getStackCount());
        session.getCooldownTracker().resetAll();
        assertEquals(PlantPlacementResult.GROUND_OCCUPIED, session.tryPlant("Pea Pod", 2, 2, 1));
    }

    @Test
    void deductsSunOnPlacement() {
        int before = session.getSunBalance();
        session.tryPlant("Sunflower", 1, 0, 1);
        assertTrue(session.getSunBalance() < before);
    }

    @Test
    void supportsAquaticBaseAndPlantOverlay() {
        board.setTile(4, 2, new LowBeachTile());
        assertEquals(PlantPlacementResult.REQUIRES_WATER,
                session.tryPlant("Lily Pad", 3, 2, 1));
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Lily Pad", 4, 2, 1));
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Peashooter", 4, 2, 1));
        assertEquals("Lily Pad", board.getGroundPlantAt(4, 2).getName());
        assertEquals("Peashooter", board.getOverlayPlantAt(4, 2).getName());
    }

    @Test
    void enforcesSelectedLoadoutAndRecharge() {
        session.setSelectedLoadout(java.util.Set.of("Peashooter"));
        assertEquals(PlantPlacementResult.NOT_IN_LOADOUT,
                session.tryPlant("Sunflower", 0, 0, 1));
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Peashooter", 0, 0, 1));
        assertEquals(PlantPlacementResult.ON_COOLDOWN,
                session.tryPlant("Peashooter", 1, 0, 1));
    }

    @Test
    void utilityPlantsClearBlockedTiles() {
        board.setTile(2, 2, new GraveTile());
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Grave Buster", 2, 2, 1));
        assertTrue(board.getTile(2, 2).isGrave());
        session.advanceTicks(50);
        assertFalse(board.getTile(2, 2).isGrave());

        board.setTile(3, 2, new IceTile());
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Hot Potato", 3, 2, 1));
        assertTrue(board.getTile(3, 2).isIce());
        session.advanceTicks(5);
        assertFalse(board.getTile(3, 2).isIce());
    }

    @Test
    void rejectsInvalidLevelsAndUncheckedOverwrites() {
        assertEquals(PlantPlacementResult.INVALID_LEVEL,
                session.tryPlant("Peashooter", 0, 0, 0));
        assertEquals(PlantPlacementResult.INVALID_LEVEL,
                session.tryPlant("Peashooter", 0, 0, 5));

        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Peashooter", 0, 0, 1));
        Plant duplicate = new io.github.finalwave.model.game.entity.plant.PlantFactory()
                .createBaseLevel(registry.getDefinition("Sunflower"), 0, 0);
        assertThrows(IllegalArgumentException.class, () -> board.placePlant(duplicate));
    }

    @Test
    void sandboxAllowsAquaticPlantsOnNormalGround() {
        assertEquals(PlantPlacementResult.REQUIRES_WATER,
                board.canPlace(registry.getDefinition("Sea-shroom"), 2, 2));
        board.setSandboxAquaticOnLand(true);
        assertEquals(PlantPlacementResult.SUCCESS,
                board.canPlace(registry.getDefinition("Sea-shroom"), 2, 2));
        session.enableSandboxPractice();
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Sea-shroom", 2, 2, 1));
        assertNotNull(board.getPlantAt(2, 2));
    }
}
