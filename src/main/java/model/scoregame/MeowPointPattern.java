package model.scoregame;

import model.quest.event.GameEvent;

public interface MeowPointPattern {
    String id();

    void onEvent(GameEvent event);

    int score();

    void reset();
}
