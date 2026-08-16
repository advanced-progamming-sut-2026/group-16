package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;


public final class ActorFades {
    public static final float HOLD_SECONDS = 0.3f;
    public static final float FADE_SECONDS = 0.45f;

    private ActorFades() {
    }

    public static Action holdThenFade() {
        return holdThenFade(HOLD_SECONDS, FADE_SECONDS, null);
    }

    public static Action holdThenFade(Runnable onComplete) {
        return holdThenFade(HOLD_SECONDS, FADE_SECONDS, onComplete);
    }

    public static Action holdThenFade(float holdSeconds, float fadeSeconds) {
        return holdThenFade(holdSeconds, fadeSeconds, null);
    }

    public static Action holdThenFade(float holdSeconds, float fadeSeconds, Runnable onComplete) {
        Action fade = Actions.sequence(
                Actions.delay(holdSeconds),
                Actions.fadeOut(fadeSeconds, Interpolation.fade),
                Actions.removeActor());
        if (onComplete == null) {
            return fade;
        }
        return Actions.sequence(
                Actions.delay(holdSeconds),
                Actions.fadeOut(fadeSeconds, Interpolation.fade),
                Actions.run(onComplete),
                Actions.removeActor());
    }
}
