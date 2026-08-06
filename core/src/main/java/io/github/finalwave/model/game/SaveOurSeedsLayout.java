package io.github.finalwave.model.game;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SaveOurSeedsLayout {

    private final List<SeedPlacement> placements;

    public SaveOurSeedsLayout(List<SeedPlacement> placements) {
        this.placements = placements == null ? List.of() : List.copyOf(placements);
    }

    public List<SeedPlacement> getPlacements() {
        return placements;
    }

    public List<Integer> getDangerRows() {
        Set<Integer> rows = new LinkedHashSet<>();
        for (SeedPlacement placement : placements) {
            rows.add(placement.getRow());
        }
        return List.copyOf(rows);
    }

    public boolean isEmpty() {
        return placements.isEmpty();
    }
}
