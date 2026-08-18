package io.github.finalwave.view.gui.match;

import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.render.BattlefieldGroup;


public final class MatchClock {
    private static final float MAX_FRAME_DELTA = 0.05f;
    private static final int MAX_TICKS_PER_FRAME = 1;

    private final GamePlayController controller;
    private final User user;
    private float accumulator;
    private float tickDuration = 1f / GameSession.TICKS_PER_SECOND;
    private boolean paused;
    private boolean resultShowing;
    private boolean frozenActors = true;

    public MatchClock(GamePlayController controller, User user) {
        this.controller = controller;
        this.user = user;
    }

    public void update(float deltaSeconds, BattlefieldGroup battlefield) {
        boolean freeze = shouldFreeze();
        if (battlefield != null) {
            boolean environmentPlaying = !paused && !resultShowing;
            battlefield.setPlaying(!freeze, environmentPlaying);
            frozenActors = freeze;
        }
        if (freeze) {
            accumulator = 0f;
            return;
        }
        tickDuration = 1f / (GameSession.TICKS_PER_SECOND * speed());
        accumulator += Math.min(MAX_FRAME_DELTA, Math.max(0f, deltaSeconds));
        int steps = 0;
        while (accumulator >= tickDuration && steps < MAX_TICKS_PER_FRAME) {
            controller.advance(1);
            accumulator -= tickDuration;
            steps++;
            if (shouldFreeze()) {
                accumulator = 0f;
                break;
            }
        }
        if (accumulator > tickDuration) {
            accumulator = tickDuration;
        }
    }

    public float tickFraction() {
        if (tickDuration <= 0f || shouldFreeze()) {
            return 0f;
        }
        return Math.min(1f, Math.max(0f, accumulator / tickDuration));
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setResultShowing(boolean resultShowing) {
        this.resultShowing = resultShowing;
    }

    public void cycleSpeed() {
        int next = speed() % 3 + 1;
        if (user != null) {
            user.setGameSpeed(next);
        }
    }

    public int speed() {
        if (user == null) {
            return 1;
        }
        return Math.max(1, Math.min(3, user.getGameSpeed()));
    }

    public boolean shouldFreeze() {
        if (paused || resultShowing) {
            return true;
        }
        if (controller == null || controller.session() == null) {
            return true;
        }
        return controller.session().getMatchResult() != MatchResult.IN_PROGRESS;
    }
}
