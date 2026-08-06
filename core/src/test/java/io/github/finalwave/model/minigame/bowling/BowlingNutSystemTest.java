package io.github.finalwave.model.minigame.bowling;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BowlingNutSystemTest {

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

    private GameSession newSession() {
        GameSession session = new GameSession(
                plantRegistry, new GameBoard(5, 9), 0, zombieRegistry, 1);
        session.activateWalnutBowling(4);
        session.getBowlingNutSystem().configureDamageFromRegistry(zombieRegistry);
        session.start();
        return session;
    }

    @Test
    void standardNutKillsNormalZombieWithMaxHpDamage() {
        GameSession session = newSession();
        Zombie zombie = session.spawnZombieOfType("ZombieDefault", 0, 3.0);
        int hpBefore = zombie.getHealth();

        BowlingNut nut = new BowlingNut(BowlingNutType.STANDARD, 2.5, 0);
        session.getBowlingNutSystem().spawn(nut);
        session.advanceTicks(1);

        assertTrue(zombie.isDead() || zombie.getHealth() < hpBefore);
        assertEquals(190, hpBefore);
    }

    @Test
    void explosiveNutDetonatesOnFirstZombieHit() {
        GameSession session = newSession();
        Zombie zombie = session.spawnZombieOfType("ZombieDefault", 0, 3.0);
        session.getBowlingNutSystem().spawn(new BowlingNut(BowlingNutType.EXPLOSIVE, 2.5, 0));

        session.advanceTicks(1);

        assertTrue(zombie.isDead());
        assertTrue(session.getBowlingNutSystem().getNuts().isEmpty());
    }

    @Test
    void giantNutPushesZombieForward() {
        GameSession session = newSession();
        Zombie zombie = session.spawnZombieOfType("ZombieDefault", 0, 3.0);
        double xBefore = zombie.getX();

        session.getBowlingNutSystem().spawn(new BowlingNut(BowlingNutType.GIANT, 2.5, 0));
        session.advanceTicks(1);

        assertTrue(zombie.getX() > xBefore);
        assertFalse(session.getBowlingNutSystem().getNuts().isEmpty());
    }

    @Test
    void standardNutDeflectsAfterZombieHit() {
        GameSession session = newSession();
        session.spawnZombieOfType("ZombieDefault", 0, 3.0);
        BowlingNut nut = new BowlingNut(BowlingNutType.STANDARD, 2.5, 0);
        double angleBefore = nut.getAngleRadians();
        session.getBowlingNutSystem().spawn(nut);

        session.advanceTicks(1);

        assertEquals(1, nut.getZombieHitCount());
        assertTrue(Math.abs(nut.getAngleRadians() - angleBefore) > 0.01);
    }

    @Test
    void nutRemovedWhenLeavingBoard() {
        GameSession session = newSession();
        BowlingNut nut = new BowlingNut(BowlingNutType.STANDARD, 8.5, 0);
        session.getBowlingNutSystem().spawn(nut);

        session.advanceTicks(30);

        assertTrue(session.getBowlingNutSystem().getNuts().isEmpty());
    }
}
