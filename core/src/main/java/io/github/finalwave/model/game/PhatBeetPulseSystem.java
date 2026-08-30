package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.plant.ability.PhatBeetPulseMark;

import java.util.ArrayList;
import java.util.List;


public final class PhatBeetPulseSystem {

    private final List<PhatBeetPulseMark> pending = new ArrayList<>();

    public void enqueue(PhatBeetPulseMark mark) {
        if (mark != null) {
            pending.add(mark);
        }
    }

    public List<PhatBeetPulseMark> drainPulseMarks() {
        if (pending.isEmpty()) {
            return List.of();
        }
        List<PhatBeetPulseMark> drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }

    public void clear() {
        pending.clear();
    }
}
