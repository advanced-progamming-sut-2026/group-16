package io.github.finalwave.model.quest.condition;

import io.github.finalwave.model.quest.event.GameEvent;

public interface QuestCondition {
    void onEvent(GameEvent event);

    boolean isMet();

    void reset();

    String describe();

    default boolean persistsAcrossSessions() {
        return false;
    }

    default String serializeProgress() {
        return "";
    }

    default void deserializeProgress(String data) {

    }
}
