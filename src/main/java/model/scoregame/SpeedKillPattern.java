package model.scoregame;

import model.quest.event.GameEvent;

public final class SpeedKillPattern implements MeowPointPattern {
    public static final String ID = "speed-kill";
    private static final double TIME_LIMIT_SECONDS = 3.0;
    private static final int POINTS = 15;

    private int score;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (event instanceof GameEvent.ZombieKilled killed
                && killed.secondsSinceWaveStart() <= TIME_LIMIT_SECONDS) {
            score += POINTS;
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
