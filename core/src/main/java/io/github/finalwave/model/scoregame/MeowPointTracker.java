package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.quest.event.GameEventBus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MeowPointTracker {
    private final List<MeowPointPattern> patterns;
    private final GameEventBus.Subscriber listener = this::onEvent;
    private GameEventBus bus;
    private boolean finalized;
    private MeowPointBreakdown lastBreakdown = MeowPointBreakdown.empty();

    public MeowPointTracker(List<MeowPointPattern> patterns) {
        this.patterns = new ArrayList<>(patterns == null ? List.of() : patterns);
    }

    public static MeowPointTracker createDefault() {
        return new MeowPointTracker(List.of(
                new PierceMultiKillPattern(),
                new SpeedKillPattern(),
                new SimultaneousKillPattern(),
                new MowerSweepPattern(),
                new EfficientVictoryPattern()));
    }

    public void registerOn(GameEventBus eventBus) {
        unregister();
        this.bus = eventBus;
        if (bus != null) {
            bus.subscribe(listener);
        }
        reset();
    }

    public void unregister() {
        if (bus != null) {
            bus.unsubscribe(listener);
            bus = null;
        }
    }

    public void reset() {
        finalized = false;
        lastBreakdown = MeowPointBreakdown.empty();
        for (MeowPointPattern pattern : patterns) {
            pattern.reset();
        }
    }

    private void onEvent(GameEvent event) {
        if (finalized) {
            return;
        }
        for (MeowPointPattern pattern : patterns) {
            pattern.onEvent(event);
        }
        if (event instanceof GameEvent.GameFinished) {
            lastBreakdown = snapshot();
            finalized = true;
        }
    }

    public MeowPointBreakdown snapshot() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        int total = 0;
        for (MeowPointPattern pattern : patterns) {
            int value = pattern.score();
            scores.put(pattern.id(), value);
            total += value;
        }
        return new MeowPointBreakdown(total, scores);
    }

    public MeowPointBreakdown getBreakdown() {
        return finalized ? lastBreakdown : snapshot();
    }

    public int total() {
        return getBreakdown().total();
    }
}
