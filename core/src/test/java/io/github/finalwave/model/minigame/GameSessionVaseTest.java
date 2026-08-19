package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.minigame.mode.VaseBreakerMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionVaseTest {

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

    private GameSession emptySession() {
        return new GameSession(plantRegistry, new io.github.finalwave.model.game.board.GameBoard(), 0,
                zombieRegistry, 1, new Random(1));
    }

    @Test
    void smashEmptyVaseRemovesIt() {
        GameSession session = emptySession();
        session.addVase(new Vase("v1", 2, 1, Vase.Content.EMPTY, null));
        assertTrue(session.smashVase(2, 1));
        assertNull(session.getVaseAt(2, 1));
        assertTrue(session.areAllVasesSmashed());
        assertEquals(0, session.getZombies().size());
    }

    @Test
    void smashZombieVaseSpawnsZombie() {
        GameSession session = emptySession();
        session.addVase(new Vase("v1", 3, 0, Vase.Content.ZOMBIE, "ZombieDefault"));
        assertTrue(session.smashVase(3, 0));
        assertEquals(1, session.getZombies().size());
        assertEquals("ZombieDefault", session.getZombies().getFirst().getType());
    }

    @Test
    void smashGargantuarVaseSpawnsGargantuar() {
        GameSession session = emptySession();
        session.addVase(new Vase("v1", 4, 2, Vase.Content.GARGANTUAR, "ZombieGargantuar"));
        assertTrue(session.smashVase(4, 2));
        assertEquals(1, session.getZombies().size());
        assertEquals("ZombieGargantuar", session.getZombies().getFirst().getType());
    }

    @Test
    void smashPlantSeedVaseDropsSeedPacket() {
        GameSession session = emptySession();
        session.setSeedPacketExpiryTicks(50);
        session.addVase(new Vase("v1", 1, 1, Vase.Content.PLANT_SEED, "Peashooter"));
        AtomicReference<String> dropped = new AtomicReference<>();
        session.setMatchListener(new MatchListener() {
            @Override
            public void onSeedPacketDropped(String plantName, int col, int row) {
                dropped.set(plantName);
            }
        });
        assertTrue(session.smashVase(1, 1));
        assertEquals("Peashooter", dropped.get());
        assertNotNull(session.getGroundSeedPacketAt(1, 1));
    }

    @Test
    void plantFromSeedPacketPlacesPlantAndConsumesPacket() {
        GameSession session = emptySession();
        session.addGroundSeedPacket("Peashooter", 2, 2);
        assertEquals(PlantPlacementResult.SUCCESS, session.plantFromSeedPacket(2, 2));
        assertNotNull(session.getBoard().getPlantAt(2, 2));
        assertEquals("Peashooter", session.getBoard().getPlantAt(2, 2).getName());
        assertNull(session.getGroundSeedPacketAt(2, 2));
    }

    @Test
    void plantFromSeedPacketByNamePlacesAtClickedCell() {
        GameSession session = emptySession();
        session.addGroundSeedPacket("Peashooter", 1, 1);
        assertEquals(PlantPlacementResult.SUCCESS, session.plantFromSeedPacket("Peashooter", 4, 2));
        assertNotNull(session.getBoard().getPlantAt(4, 2));
        assertEquals("Peashooter", session.getBoard().getPlantAt(4, 2).getName());
        assertNull(session.getGroundSeedPacketAt(1, 1));
        assertNull(session.getBoard().getPlantAt(1, 1));
    }

    @Test
    void plantFromSeedPacketByNameFailsWithoutPacket() {
        GameSession session = emptySession();
        assertEquals(PlantPlacementResult.NO_SEED_PACKET, session.plantFromSeedPacket("Peashooter", 2, 2));
    }

    @Test
    void seedPacketExpiresOnTick() {
        GameSession session = emptySession();
        session.setSeedPacketExpiryTicks(5);
        session.addGroundSeedPacket("Sunflower", 1, 0);
        AtomicBoolean expired = new AtomicBoolean(false);
        session.setMatchListener(new MatchListener() {
            @Override
            public void onSeedPacketExpired(String plantName, int col, int row) {
                expired.set(true);
            }
        });
        session.start();
        session.advanceTicks(5);
        assertTrue(expired.get());
        assertNull(session.getGroundSeedPacketAt(1, 0));
    }

    @Test
    void smashMissingVaseReturnsFalse() {
        GameSession session = emptySession();
        assertFalse(session.smashVase(0, 0));
    }

    @Test
    void vaseBreakerModePlacesConfiguredPots() {
        MiniGameStageConfig stage = MiniGameStageConfig.vaseBreaker(1);
        VaseBreakerMode mode = new VaseBreakerMode(stage, plantRegistry, zombieRegistry, new Random(42));
        GameSession session = mode.createSession();
        assertEquals(stage.getPotCount(), session.getVases().size());
        assertFalse(session.getSkySunSystem().isEnabled());
        assertFalse(session.isWavesAutoStart());
        assertNotNull(session.getActiveMiniGameHandler());
    }
}
