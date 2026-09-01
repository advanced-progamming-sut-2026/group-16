package io.github.finalwave.model.game.board.tile;

import io.github.finalwave.model.definition.plant.PlantDefinition;


public class LowBeachTile extends Tile {
    private final boolean flooded;

    public LowBeachTile() {
        this(true);
    }

    public LowBeachTile(boolean flooded) {
        this.flooded = flooded;
    }

    public boolean isFlooded() {
        return flooded;
    }

    @Override
    public boolean isWater() {
        return flooded;
    }

    @Override
    public boolean canPlant(PlantDefinition definition) {
        if (flooded) {
            return definition.hasTag("WATER");
        }
        return true;
    }

    @Override
    public boolean blocksPlanting() {
        return false;
    }
}
