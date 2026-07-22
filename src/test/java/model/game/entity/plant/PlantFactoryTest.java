package model.game.entity.plant;

import model.definition.PlantRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlantFactoryTest {

    private PlantRegistry registry;
    private PlantFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        factory = new PlantFactory();
    }

    @Test
    void allPlantsInstantiateWithoutError() {
        assertEquals(70, registry.size());
        for (var def : registry.getAllDefinitions()) {
            Plant plant = assertDoesNotThrow(
                    () -> factory.createBaseLevel(def, 2, 1),
                    "Failed for " + def.getName());
            assertNotNull(plant);
            assertEquals(def.getName(), plant.getName());
            assertEquals(1, plant.getLevel());
        }
    }

    @Test
    void upgradeSyncsMaxHealth() {
        var def = registry.getDefinition("Peashooter");
        assertNotNull(def);
        Plant plant = factory.create(def, 1, 3, 2);
        int baseHp = plant.getMaxHealth();
        assertTrue(plant.upgrade());
        assertTrue(plant.getMaxHealth() >= baseHp);
    }
}
