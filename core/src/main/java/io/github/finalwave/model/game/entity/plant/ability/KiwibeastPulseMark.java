package io.github.finalwave.model.game.entity.plant.ability;

import java.util.List;


public record KiwibeastPulseMark(int plantCol, int plantRow, boolean plantFood, List<HitTile> hits) {

    public KiwibeastPulseMark {
        hits = hits == null ? List.of() : List.copyOf(hits);
    }

    public record HitTile(int col, int row) {
    }
}
