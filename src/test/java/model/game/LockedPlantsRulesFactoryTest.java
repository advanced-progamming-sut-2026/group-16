package model.game;

import model.adventure.ChapterId;
import model.adventure.LevelConfig;
import model.adventure.LevelType;
import model.definition.PlantRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LockedPlantsRulesFactoryTest {

    private PlantRegistry plantRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
    }

    @Test
    void familyModeLocksAllButOnePerCategoryWhenMultipleOwned() {
        LevelConfig level = LevelConfig.special(3, LevelType.LOCKED_PLANTS, "locked");
        List<String> owned = List.of("Sunflower", "Twin Sunflower", "Peashooter", "Repeater", "Wall-nut");
        LockedPlantsRules rules = LockedPlantsRulesFactory.create(
                level, ChapterId.ANCIENT_EGYPT, plantRegistry, owned, new Random(42));

        assertEquals(LockedPlantsMode.FAMILY, rules.getMode());
        assertEquals(2, rules.getLockedPlants().size());
        assertEquals(3, rules.getAllowedPlants().size());
        assertTrue(rules.isSelectable("Sunflower", true) ^ rules.isSelectable("Twin Sunflower", true));
        assertTrue(rules.isSelectable("Peashooter", true) ^ rules.isSelectable("Repeater", true));
        assertTrue(rules.isSelectable("Wall-nut", true));
    }

    @Test
    void familyModeDoesNotLockSingleOwnedPlantInCategory() {
        LevelConfig level = LevelConfig.special(3, LevelType.LOCKED_PLANTS, "locked");
        List<String> owned = List.of("Sunflower", "Peashooter", "Wall-nut");
        LockedPlantsRules rules = LockedPlantsRulesFactory.create(
                level, ChapterId.ANCIENT_EGYPT, plantRegistry, owned, new Random(7));

        assertTrue(rules.getLockedPlants().isEmpty());
        assertTrue(rules.isSelectable("Sunflower", true));
        assertTrue(rules.isSelectable("Peashooter", true));
        assertTrue(rules.isSelectable("Wall-nut", true));
    }

    @Test
    void specificModeLocksConfiguredOwnedPlants() {
        LevelConfig level = LevelConfig.special(3, LevelType.LOCKED_PLANTS, "locked-specific");
        List<String> owned = List.of("Sunflower", "Peashooter", "Wall-nut", "Cherry Bomb");
        LockedPlantsRules rules = LockedPlantsRulesFactory.create(
                level, ChapterId.ANCIENT_EGYPT, plantRegistry, owned, new Random(1));

        assertEquals(LockedPlantsMode.SPECIFIC, rules.getMode());
        assertEquals(Set.of("Wall-nut", "Cherry Bomb"), rules.getLockedPlants());
        assertTrue(rules.isSelectable("Sunflower", true));
        assertTrue(rules.isSelectable("Peashooter", true));
        assertFalse(rules.isSelectable("Wall-nut", true));
        assertFalse(rules.isSelectable("Cherry Bomb", true));
    }

    @Test
    void unknownHandlerKeyThrows() {
        LevelConfig level = new LevelConfig(
                3, LevelType.LOCKED_PLANTS, 3, 8, 50, 300, List.of("ZombieDefault"), "locked-unknown");
        assertThrows(IllegalArgumentException.class, () -> LockedPlantsRulesFactory.create(
                level, ChapterId.ANCIENT_EGYPT, plantRegistry, List.of("Sunflower"), new Random(1)));
    }
}
