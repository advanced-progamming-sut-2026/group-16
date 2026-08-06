package io.github.finalwave.model.quest.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GameEventBus {

    @FunctionalInterface
    public interface Subscriber {
        void onEvent(GameEvent event);
    }

    private final List<Subscriber> subscribers = new ArrayList<>();

    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void publish(GameEvent event) {
        for (Subscriber s : subscribers) {
            s.onEvent(event);
        }
    }
}
