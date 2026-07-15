package model.game.board.tile;

import model.definition.plant.PlantDefinition;

public class LowBeachTile extends Tile {

    @Override
    public boolean isWater() {
        return true;
    }

    @Override
    public boolean canPlant(PlantDefinition definition) {
        return definition.hasTag("WATER");
    }

    @Override
    public boolean blocksPlanting() {
        return false;
    }
}
