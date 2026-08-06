package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.quest.event.GameEvent;

public interface MeowPointPattern {
    String id();

    void onEvent(GameEvent event);

    int score();

    void reset();
}
