package model.entity;

import model.definition.ZombieRegistry;
import model.definition.zombie.ZombieDefinition;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieFactory;
import model.game.entity.zombie.behavior.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZombieFactoryTest {

    private ZombieRegistry registry;
    private ZombieFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        registry = new ZombieRegistry();

        // 1. Load the zombies.json from the test resources folder
        registry.loadFromJson("src/test/resources/zombies.json");

        // 2. Load the ArmorTypeData.json from the test resources folder
        // This is required so the factory can resolve "RTID(ConeDefault@ArmorTypes)"
        registry.loadArmorFromJson("src/test/resources/ArmorTypeData.json");

        factory = new ZombieFactory(registry);
    }

    @Test
    void testAllZombiesInJsonCanBeCreated() {
        List<ZombieDefinition> definitions = registry.getAllDefinitions();

        // Verify that the JSON actually loaded entries
        assertFalse(definitions.isEmpty(), "Zombie registry should have loaded definitions from JSON");

        // Iterate through every single JSON entry and ensure the factory can build it
        for (ZombieDefinition def : definitions) {
            Zombie zombie = assertDoesNotThrow(
                    () -> factory.createZombie(def.getAlias()),
                    "Factory failed to create zombie for alias: " + def.getAlias()
            );

            assertNotNull(zombie, "Created zombie should not be null for " + def.getAlias());
            assertEquals(def.getAlias(), zombie.getType(), "Zombie type mismatch");
            assertEquals(def.getHitpoints(), zombie.getMaxHealth(), "HP mismatch for " + def.getAlias());
            assertEquals(def.getSpeed(), zombie.getBaseSpeed(), 0.0001, "Speed mismatch for " + def.getAlias());
            assertEquals(def.getEatDps(), zombie.getDamage(), "Damage mismatch for " + def.getAlias());

            // Every zombie must have at least the MovementBehavior
            assertTrue(
                    zombie.getBehaviors().stream().anyMatch(b -> b instanceof MovementBehavior),
                    "Zombie " + def.getAlias() + " is missing MovementBehavior"
            );
        }
    }

    @Test
    void testGargantuarBehaviors() {
        Zombie gargantuar = factory.createZombie("ZombieGargantuar");

        assertEquals(3600, gargantuar.getMaxHealth());
        assertEquals(0.24, gargantuar.getBaseSpeed());

        // Gargantuar should have SMASH and SUMMON behaviors
        boolean hasSmash = gargantuar.getBehaviors().stream()
                .anyMatch(b -> b instanceof TransformBehavior);
        boolean hasSummon = gargantuar.getBehaviors().stream()
                .anyMatch(b -> b instanceof SummonBehavior);

        assertTrue(hasSmash, "Gargantuar should have a TransformBehavior (Smash)");
        assertTrue(hasSummon, "Gargantuar should have a SummonBehavior");
    }

    @Test
    void testNewspaperZombieBehaviors() {
        Zombie newspaper = factory.createZombie("ZombieNewspaper");

        assertEquals(460, newspaper.getMaxHealth());
        assertEquals(0.22, newspaper.getBaseSpeed());

        // Newspaper should have DamageReaction and Armor behaviors
        boolean hasReaction = newspaper.getBehaviors().stream()
                .anyMatch(b -> b instanceof DamageReactionBehavior);
        boolean hasArmorBehavior = newspaper.getBehaviors().stream()
                .anyMatch(b -> b instanceof ArmorBehavior);

        assertTrue(hasReaction, "Newspaper Zombie should have a DamageReactionBehavior");
        assertTrue(hasArmorBehavior, "Newspaper Zombie should have an ArmorBehavior");
    }

    @Test
    void testArmorZombieStats() {
        // ZombieArmor1 uses the ConeDefault armor
        Zombie armoredZombie = factory.createZombie("ZombieArmor1");

        assertEquals(190, armoredZombie.getMaxHealth(), "Base health should match JSON");

        // Now that ArmorTypeData.json is loaded, this should pass
        assertFalse(armoredZombie.getArmorLayers().isEmpty(), "Armored zombie should have armor layers attached");
        assertTrue(armoredZombie.hasArmor(), "hasArmor() should return true");
    }

    @Test
    void createsAtRequestedSpawnPositionAndValidatesDifficulty() {
        Zombie zombie = factory.createZombie("ZombieGargantuar", 8.5, 3, 2);

        assertEquals(8.5, zombie.getX(), 0.0001);
        assertEquals(3, zombie.getRow());
        assertEquals(3960, zombie.getMaxHealth());
        assertThrows(IllegalArgumentException.class,
                () -> factory.createZombie("ZombieGargantuar", 8.5, 3, 0));
        assertThrows(IllegalArgumentException.class,
                () -> factory.createZombie("ZombieGargantuar", Double.NaN, 3, 1));
        assertThrows(IllegalArgumentException.class,
                () -> factory.createZombie("ZombieGargantuar", 8.5, -1, 1));
    }
}