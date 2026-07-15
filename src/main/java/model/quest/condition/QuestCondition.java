package model.quest.condition;

import model.quest.event.GameEvent;

public interface QuestCondition {
    void onEvent(GameEvent event);

    boolean isMet();

    void reset();

    String describe();
}
