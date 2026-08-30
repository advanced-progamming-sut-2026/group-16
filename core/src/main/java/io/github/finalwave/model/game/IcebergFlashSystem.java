package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.plant.ability.IcebergFlashMark;

import java.util.ArrayList;
import java.util.List;


public final class IcebergFlashSystem {

    private final List<IcebergFlashMark> pending = new ArrayList<>();

    public void enqueue() {
        pending.add(new IcebergFlashMark());
    }

    public List<IcebergFlashMark> drainFlashMarks() {
        if (pending.isEmpty()) {
            return List.of();
        }
        List<IcebergFlashMark> drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }

    public void clear() {
        pending.clear();
    }
}
