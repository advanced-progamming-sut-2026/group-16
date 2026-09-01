package io.github.finalwave.model.entity;

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

class GargantuarImpThrowBehaviorTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry registry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        registry = new ZombieRegistry();
        registry.loadFromJson("src/test/resources/zombies.json");
        registry.loadArmorFromJson("src/test/resources/ArmorTypeData.json");
    }

    @Test
    void gargantuarThrowsImpAfterFireAnimation() {
        GameSession session = new GameSession(plantRegistry, new GameBoard(), 50, registry, 1);
        Zombie garg = session.spawnZombieOfType("ZombieGargantuar", 1, 5);
        garg.takeDirectDamage(garg.getMaxHealth() / 2 + 1);

        session.start();
        session.tick();
        assertTrue(
                "cannon_fire".equals(garg.getPresentationClip())
                        || "fire".equals(garg.getPresentationClip()));
        assertTrue(garg.hasPendingGargantuarImpThrow());
        assertFalse(garg.isGargantuarImpSpent());
        assertEquals(1, session.getZombies().size());

        session.tick();
        assertTrue(garg.isGargantuarImpSpent());
        assertEquals(2, session.getZombies().size());

        Zombie imp = session.getZombies().stream()
                .filter(z -> z != garg)
                .findFirst()
                .orElseThrow();
        assertEquals("ZombieImp", imp.getType());
        assertTrue(imp.getX() < garg.getX() - 0.15, "imp should launch from garg basket");
        assertEquals(garg.getX() - imp.getX(), 0.645, 0.1);
        assertEquals("fly", imp.getPresentationClip());
        assertTrue(imp.arcLiftForX(imp.getX()) > 0.68, "imp should start from elevated basket");
        assertTrue(imp.arcLiftAt(0.5) > imp.arcLiftAt(0.0));
        assertTrue(imp.arcLiftAt(0.5) > imp.arcLiftAt(1.0));
        assertTrue(imp.arcLiftForX(imp.getX() - 1.0) > imp.arcLiftForX(imp.getX()));
        assertTrue(imp.arcLiftAt(1.0) < 0.05, "imp should land on the ground");
    }

    @Test
    void gargantuarDoesNotThrowImpWhenTooCloseToHouse() {
        GameSession session = new GameSession(plantRegistry, new GameBoard(), 50, registry, 1);
        Zombie garg = session.spawnZombieOfType("ZombieGargantuar", 1, 2);
        garg.takeDirectDamage(garg.getMaxHealth() / 2 + 1);

        session.start();
        session.advanceTicks(20);
        assertEquals(1, session.getZombies().size());
    }
}
