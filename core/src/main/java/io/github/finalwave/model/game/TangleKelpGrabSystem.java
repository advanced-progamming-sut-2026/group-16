package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.plant.ability.TangleKelpGrabMark;

import java.util.ArrayList;
import java.util.List;


public final class TangleKelpGrabSystem {

    private final List<TangleKelpGrabMark> pending = new ArrayList<>();

    public void enqueue(int col, int row) {
        pending.add(new TangleKelpGrabMark(row, col));
    }

    public List<TangleKelpGrabMark> drainGrabMarks() {
        if (pending.isEmpty()) {
            return List.of();
        }
        List<TangleKelpGrabMark> drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }

    public void clear() {
        pending.clear();
    }
}
