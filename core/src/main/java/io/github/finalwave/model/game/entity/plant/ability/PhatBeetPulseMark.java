package io.github.finalwave.model.game.entity.plant.ability;

import java.util.List;


public record PhatBeetPulseMark(int plantCol, int plantRow, boolean plantFood, List<HitTile> hits) {

    public PhatBeetPulseMark {
        hits = hits == null ? List.of() : List.copyOf(hits);
    }

    public record HitTile(int col, int row) {
    }
}
