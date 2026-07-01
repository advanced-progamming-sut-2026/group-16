package model.game.entity.plant;

import model.definition.plant.PlantDefinition;
import model.definition.PlantRegistry;

import java.util.concurrent.atomic.AtomicLong;

public final class PlantFactory {

    private final AtomicLong sequence = new AtomicLong();
    private final PlantRegistry registry;

    public PlantFactory() {
        this(null);
    }

    public PlantFactory(PlantRegistry registry) {
        this.registry = registry;
    }

    public Plant create(String plantName, int level, int col, int row) {
        if (registry == null) {
            throw new IllegalStateException("PlantFactory has no registry");
        }
        PlantDefinition definition = registry.getDefinition(plantName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown plant: " + plantName);
        }
        return create(definition, level, col, row);
    }

    public Plant create(PlantDefinition definition, int level, int col, int row) {
        String id = definition.getName() + "-" + sequence.incrementAndGet();
        return new Plant(id, definition, level, col, row);
    }

    public Plant createBaseLevel(PlantDefinition definition, int col, int row) {
        return create(definition, 1, col, row);
    }
}
