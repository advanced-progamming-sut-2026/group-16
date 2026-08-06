package io.github.finalwave.view.api;

import io.github.finalwave.model.game.TimedWarMode;

public interface TimedWarView extends SpecialLevelView {

    void showTimedWarStatus(TimedWarMode mode, int remainingSeconds, int durationSeconds, int progress, int goal);

    void showTimedWarTimeUp();

    void showTimedWarGoalReached(TimedWarMode mode, int progress);
}
