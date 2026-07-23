package model.scoregame;

import model.quest.event.GameEvent;

public final class MowerSweepPattern implements MeowPointPattern {
    public static final String ID = "mower-sweep";
    private static final int POINTS_PER_ZOMBIE = 20;

    private int score;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (event instanceof GameEvent.LawnMowerTriggered mower && mower.zombiesKilled() >= 2) {
            score += POINTS_PER_ZOMBIE * mower.zombiesKilled();
        }
    }

    @Override
    public int score() {
        return score;
    }

    @Override
    public void reset() {
        score = 0;
    }
}
