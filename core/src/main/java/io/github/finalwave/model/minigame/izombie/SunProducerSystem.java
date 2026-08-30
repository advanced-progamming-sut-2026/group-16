package io.github.finalwave.model.minigame.izombie;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SunProducerSystem {

    public static final int BASE_SUN_PER_SECOND = 1;
    public static final int GROWTH_INTERVAL_SECONDS = 15;
    public static final int MAX_SUN_PER_SECOND = 15;

    private final List<ProducerState> producers = new ArrayList<>();

    public void register(Zombie producer, int row) {
        if (producer == null) {
            return;
        }
        producers.add(new ProducerState(producer));
    }

    public int getProducerCount() {
        return producers.size();
    }

    public void tick(GameSession session) {
        if (session == null) {
            return;
        }
        Iterator<ProducerState> iterator = producers.iterator();
        while (iterator.hasNext()) {
            ProducerState state = iterator.next();
            if (!state.producer.isAlive()) {
                iterator.remove();
                continue;
            }
            state.aliveTicks++;
            if (state.aliveTicks % GameSession.TICKS_PER_SECOND != 0) {
                continue;
            }
            int secondsAlive = state.aliveTicks / GameSession.TICKS_PER_SECOND;
            int amount = Math.min(
                    MAX_SUN_PER_SECOND,
                    BASE_SUN_PER_SECOND + secondsAlive / GROWTH_INTERVAL_SECONDS);
            if (session.isIZombieActive()) {
                session.addIZombieSunBalance(amount);
            } else {
                session.addSunBalance(amount);
            }
        }
    }

    private static final class ProducerState {
        private final Zombie producer;
        private int aliveTicks;

        private ProducerState(Zombie producer) {
            this.producer = producer;
        }
    }
}
