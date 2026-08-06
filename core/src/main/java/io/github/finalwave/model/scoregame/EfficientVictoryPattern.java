package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.quest.event.GameEvent;

public final class EfficientVictoryPattern implements MeowPointPattern {
    public static final String ID = "efficient-victory";
    private static final int ZERO_LOSS_BONUS = 50;

    private int score;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (!(event instanceof GameEvent.GameFinished finished) || !finished.won()) {
            return;
        }
        score += finished.sunRemaining() / 5;
        if (finished.plantsLost() == 0) {
            score += ZERO_LOSS_BONUS;
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
