package io.github.finalwave.model.minigame.beghouled;

import java.util.List;

public record BeghouledMatch(String plantName, List<BeghouledCell> cells) {

    public BeghouledMatch {
        cells = cells == null ? List.of() : List.copyOf(cells);
    }

    public int size() {
        return cells.size();
    }
}
