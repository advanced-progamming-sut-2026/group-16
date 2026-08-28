package io.github.finalwave.model.game.board;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;

import java.util.ArrayList;
import java.util.List;

public final class GameBoard {

    public static final int DEFAULT_ROWS = 5;
    public static final int DEFAULT_COLS = 9;

    private final int rows;
    private final int cols;
    private final Tile[][] tiles;
    private final PlantCell[][] cells;
    private boolean sandboxAquaticOnLand;

    public GameBoard() {
        this(DEFAULT_ROWS, DEFAULT_COLS);
    }

    public GameBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.tiles = new Tile[rows][cols];
        this.cells = new PlantCell[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tiles[row][col] = new NormalTile();
                cells[row][col] = new PlantCell();
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Tile getTile(int col, int row) {
        if (!inBounds(col, row)) {
            return null;
        }
        return tiles[row][col];
    }

    public void setTile(int col, int row, Tile tile) {
        if (inBounds(col, row) && tile != null) {
            tiles[row][col] = tile;
        }
    }

    public Plant getPlantAt(int col, int row) {
        if (!inBounds(col, row)) {
            return null;
        }
        return cells[row][col].primaryPlant();
    }

    public Plant getGroundPlantAt(int col, int row) {
        if (!inBounds(col, row)) {
            return null;
        }
        return cells[row][col].getGround();
    }

    public Plant getOverlayPlantAt(int col, int row) {
        if (!inBounds(col, row)) {
            return null;
        }
        return cells[row][col].getOverlay();
    }

    public Plant getPlantInFront(double zombieX, int row) {
        if (row < 0 || row >= rows) {
            return null;
        }
        int col = (int) Math.floor(zombieX);
        if (col < 0 || col >= cols) {
            return null;
        }
        return cells[row][col].plantInFront(zombieX);
    }

    public PlantPlacementResult
    canPlace(PlantDefinition definition, int col, int row) {
        if (!inBounds(col, row)) {
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        Tile tile = tiles[row][col];
        if (tile.blocksPlanting()) {
            if (tile.isGrave() && "Grave Buster".equals(definition.getName())) {
                return PlantPlacementResult.SUCCESS;
            }
            if (tile.isIce() && "Hot Potato".equals(definition.getName())) {
                return PlantPlacementResult.SUCCESS;
            }
            return PlantPlacementResult.TILE_BLOCKED;
        }
        if (definition.hasTag("WATER") && !tile.isWater()) {
            if (!sandboxAquaticOnLand) {
                return PlantPlacementResult.REQUIRES_WATER;
            }
        }
        PlantCell cell = cells[row][col];
        if (tile.isWater()) {
            if (cell.isEmpty()) {
                return definition.hasTag("WATER")
                        ? PlantPlacementResult.SUCCESS
                        : PlantPlacementResult.WATER_REQUIRES_AQUATIC;
            }
            boolean hasAquaticBase = cell.getGround() != null
                    && cell.getGround().hasTag(PlantTag.WATER) && cell.getGround().hasTag(PlantTag.STACK);
            if (!hasAquaticBase || cell.getOverlay() != null) {
                return PlantPlacementResult.OVERLAY_OCCUPIED;
            }
            return definition.hasTag("WATER")
                    ? PlantPlacementResult.GROUND_OCCUPIED : PlantPlacementResult.SUCCESS;
        }
        boolean peaPod = Plant.isPeaPod(definition.getName());
        if (peaPod) {
            Plant ground = cell.getGround();
            if (ground == null) {
                if (!tile.canPlant(definition)) {
                    return PlantPlacementResult.TILE_BLOCKED;
                }
                return PlantPlacementResult.SUCCESS;
            }
            if (Plant.isPeaPod(ground.getName()) && ground.getStackCount() < 5) {
                return PlantPlacementResult.SUCCESS;
            }
            return PlantPlacementResult.GROUND_OCCUPIED;
        }
        boolean stack = definition.hasTag("STACK");
        if (stack) {
            if (cell.getGround() == null) {
                return PlantPlacementResult.NEEDS_GROUND_PLANT;
            }
            if (cell.getOverlay() != null) {
                return PlantPlacementResult.OVERLAY_OCCUPIED;
            }
            return PlantPlacementResult.SUCCESS;
        }
        if (cell.getGround() != null) {
            return PlantPlacementResult.GROUND_OCCUPIED;
        }
        if (!tile.canPlant(definition)) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        return PlantPlacementResult.SUCCESS;
    }

    public void placePlant(Plant plant) {
        if (plant == null) {
            throw new IllegalArgumentException("plant must not be null");
        }
        int col = plant.getCol();
        int row = plant.getRow();
        PlantPlacementResult result = canPlace(plant.getDefinition(), col, row);
        if (result != PlantPlacementResult.SUCCESS) {
            throw new IllegalArgumentException("Invalid plant placement: " + result);
        }
        PlantCell cell = cells[row][col];
        if (tiles[row][col].isWater() && cell.getGround() == null
                && plant.hasTag(PlantTag.WATER)) {
            cell.setGround(plant);
        } else if (!Plant.isPeaPod(plant.getName()) && (plant.hasTag(PlantTag.STACK)
                || (tiles[row][col].isWater() && cell.getGround() != null))) {
            cell.setOverlay(plant);
        } else {
            cell.setGround(plant);
        }
    }

    public void removePlant(Plant plant) {
        if (plant == null) {
            return;
        }
        int row = plant.getRow();
        int col = plant.getCol();
        if (inBounds(col, row)) {
            cells[row][col].remove(plant);
        }
    }

    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Plant ground = cells[row][col].getGround();
                Plant overlay = cells[row][col].getOverlay();
                if (ground != null && ground.isAlive()) {
                    plants.add(ground);
                }
                if (overlay != null && overlay.isAlive()) {
                    plants.add(overlay);
                }
            }
        }
        return plants;
    }

    public boolean inBounds(int col, int row) {
        return col >= 0 && col < cols && row >= 0 && row < rows;
    }

    public void setSandboxAquaticOnLand(boolean sandboxAquaticOnLand) {
        this.sandboxAquaticOnLand = sandboxAquaticOnLand;
    }
}
