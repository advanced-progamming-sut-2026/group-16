package view.cli;

import model.game.TimedWarMode;
import view.api.TimedWarView;

public class TimedWarLevelViewCli extends SpecialLevelViewCli implements TimedWarView {

    @Override
    public void showTimedWarStatus(TimedWarMode mode, int remainingSeconds, int durationSeconds,
                                   int progress, int goal) {
        String modeLabel = mode == TimedWarMode.SUN ? "sun mode" : "kill mode";
        String unit = mode == TimedWarMode.SUN ? "sun" : "zombies";
        displayMessage("Timed War (" + modeLabel + "): " + goal + " " + unit + " in " + durationSeconds + "s");
        displayMessage("Time left: " + remainingSeconds + "s | Progress: " + progress + " / " + goal);
    }

    @Override
    public void showTimedWarTimeUp() {
        displayMessage("Timed War: time is up! You lose.");
    }

    @Override
    public void showTimedWarGoalReached(TimedWarMode mode, int progress) {
        String unit = mode == TimedWarMode.SUN ? "sun produced" : "zombies defeated";
        displayMessage("Timed War goal reached (" + progress + " " + unit + ")! You win.");
    }
}
