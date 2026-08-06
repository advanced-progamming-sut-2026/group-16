package io.github.finalwave.model.game;

public final class TimedWarRules {

    private final TimedWarMode mode;
    private final int durationTicks;
    private final int goalAmount;

    public TimedWarRules(TimedWarMode mode, int durationTicks, int goalAmount) {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        if (durationTicks < 0) {
            throw new IllegalArgumentException("durationTicks must be non-negative");
        }
        if (goalAmount < 0) {
            throw new IllegalArgumentException("goalAmount must be non-negative");
        }
        this.mode = mode;
        this.durationTicks = durationTicks;
        this.goalAmount = goalAmount;
    }

    public TimedWarMode getMode() {
        return mode;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getGoalAmount() {
        return goalAmount;
    }

    public int getDurationSeconds() {
        return durationTicks / GameSession.TICKS_PER_SECOND;
    }

    public boolean isGoalMet(int progress) {
        return progress >= goalAmount;
    }

    public boolean isActiveRules() {
        return durationTicks > 0 && goalAmount > 0;
    }
}
