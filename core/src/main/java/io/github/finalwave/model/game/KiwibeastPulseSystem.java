package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.plant.ability.KiwibeastPulseMark;

import java.util.ArrayList;
import java.util.List;


public final class KiwibeastPulseSystem {

    private final List<KiwibeastPulseMark> pending = new ArrayList<>();

    public void enqueue(KiwibeastPulseMark mark) {
        if (mark != null) {
            pending.add(mark);
        }
    }

    public List<KiwibeastPulseMark> drainPulseMarks() {
        if (pending.isEmpty()) {
            return List.of();
        }
        List<KiwibeastPulseMark> drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }

    public void clear() {
        pending.clear();
    }
}
