package io.github.finalwave.model.game;

import io.github.finalwave.model.game.board.tile.Tile;

public final class SlipperyTile extends Tile {

    public enum SlipDirection {
        UP,
        DOWN
    }

    private final SlipDirection direction;

    public SlipperyTile(SlipDirection direction) {
        this.direction = direction == null ? SlipDirection.UP : direction;
    }

    public SlipDirection getDirection() {
        return direction;
    }

    @Override
    public boolean blocksPlanting() {
        return true;
    }
}
