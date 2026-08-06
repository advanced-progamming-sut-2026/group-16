package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.quest.event.GameEvent;

import java.util.HashMap;
import java.util.Map;

public final class SimultaneousKillPattern implements MeowPointPattern {
    public static final String ID = "simultaneous-kill";
    private static final int POINTS_PER_EXTRA = 25;

    private final Map<Long, Integer> killsByTick = new HashMap<>();
    private int score;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (!(event instanceof GameEvent.ZombieKilled killed)) {
            return;
        }
        long tick = killed.tick();
        int count = killsByTick.merge(tick, 1, Integer::sum);
        if (count > 1) {
            score += POINTS_PER_EXTRA;
        }
    }

    @Override
    public int score() {
        return score;
    }

    @Override
    public void reset() {
        killsByTick.clear();
        score = 0;
    }
}
