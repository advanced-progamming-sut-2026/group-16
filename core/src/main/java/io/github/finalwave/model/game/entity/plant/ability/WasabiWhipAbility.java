package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.WasabiWhipMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class WasabiWhipAbility implements PlantAbility {

    public enum WhipStyle {
        RIGHT,
        LEFT,
        UP_RIGHT,
        UP_LEFT,
        DOWN_RIGHT,
        DOWN_LEFT
    }

    private int windupRemaining;
    private WhipStyle pendingStyle = WhipStyle.RIGHT;

    public WhipStyle whipStyle() {
        return pendingStyle;
    }

    public void cancelWindup() {
        windupRemaining = 0;
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (windupRemaining > 0) {
            windupRemaining--;
            if (windupRemaining == 0) {
                executeWhip(plant, context);
                plant.setAttacking(false);
                return true;
            }
            return false;
        }
        WhipStyle style = resolveStyle(plant, context);
        if (style == null) {
            return false;
        }
        pendingStyle = style;
        windupRemaining = WasabiWhipMuzzles.windupTicks(style);
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return WasabiWhipMuzzles.windupTicks(pendingStyle);
    }

    private void executeWhip(Plant plant, GameContext context) {
        context.dealWasabiWhipPunch(plant, pendingStyle, plant.getStats().damage());
    }

    static WhipStyle resolveStyle(Plant plant, GameContext context) {
        int range = WasabiWhipMuzzles.rangeTiles(plant);
        boolean ownBehind = false;
        boolean ownAhead = false;
        boolean upLeft = false;
        boolean downLeft = false;
        boolean upRight = false;
        boolean downRight = false;
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            WhipStyle hit = classify(zombie.getRow() - plantRow, zCol - plantCol, range);
            ownBehind |= hit == WhipStyle.LEFT;
            ownAhead |= hit == WhipStyle.RIGHT;
            upLeft |= hit == WhipStyle.UP_LEFT;
            downLeft |= hit == WhipStyle.DOWN_LEFT;
            upRight |= hit == WhipStyle.UP_RIGHT;
            downRight |= hit == WhipStyle.DOWN_RIGHT;
        }
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -range; dCol <= range; dCol++) {
                if (dCol == 0 && dRow == 0) {
                    continue;
                }
                Tile tile = context.getTileAt(plantCol + dCol, plantRow + dRow);
                if (tile == null || (!tile.isGrave() && !tile.isIce())) {
                    continue;
                }
                WhipStyle hit = classify(dRow, dCol, range);
                ownBehind |= hit == WhipStyle.LEFT;
                ownAhead |= hit == WhipStyle.RIGHT;
                upLeft |= hit == WhipStyle.UP_LEFT;
                downLeft |= hit == WhipStyle.DOWN_LEFT;
                upRight |= hit == WhipStyle.UP_RIGHT;
                downRight |= hit == WhipStyle.DOWN_RIGHT;
            }
        }
        if (ownBehind) {
            return WhipStyle.LEFT;
        }
        if (ownAhead) {
            return WhipStyle.RIGHT;
        }
        if (upLeft) {
            return WhipStyle.UP_LEFT;
        }
        if (downLeft) {
            return WhipStyle.DOWN_LEFT;
        }
        if (upRight) {
            return WhipStyle.UP_RIGHT;
        }
        if (downRight) {
            return WhipStyle.DOWN_RIGHT;
        }
        return null;
    }

    private static WhipStyle classify(int dRow, int dCol, int range) {
        if (dRow == 0 && dCol == 0) {
            dCol = 1;
        }
        if (Math.abs(dCol) > range) {
            return null;
        }
        if (dRow == 0) {
            return dCol < 0 ? WhipStyle.LEFT : WhipStyle.RIGHT;
        }
        if (Math.abs(dRow) != 1 || dCol == 0) {
            return null;
        }
        if (dCol < 0) {
            return dRow < 0 ? WhipStyle.UP_LEFT : WhipStyle.DOWN_LEFT;
        }
        return dRow < 0 ? WhipStyle.UP_RIGHT : WhipStyle.DOWN_RIGHT;
    }
}
