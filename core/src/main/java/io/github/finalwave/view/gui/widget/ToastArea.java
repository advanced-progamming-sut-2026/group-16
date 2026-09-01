package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;


public final class ToastArea extends VerticalGroup {
    private static final float TOAST_WIDTH = 560f;
    private static final float TOAST_DURATION = 3.2f;
    private static final float EVICT_DURATION = 0.28f;
    private static final float EVICT_SLIDE = 48f;
    private static final int MAX_TOASTS = 3;

    private final Skin skin;
    private final Vector2 stagePos = new Vector2();
    private final Vector2 localPos = new Vector2();

    public ToastArea(Skin skin) {
        this.skin = skin;
        setFillParent(true);
        padTop(40);
        space(10);
        align(Align.top | Align.center);
    }

    public void showMessage(String message) {
        show(message, PanelLabels.panelText(skin));
    }

    public void showError(String message) {

        Color error = PanelLabels.panelText(skin);
        error.r = Math.min(1f, error.r + 0.25f);
        show(message, error);
    }

    private void show(String message, Color color) {
        if (getChildren().size >= MAX_TOASTS) {
            evictOldest();
        }

        Label label = new Label(message, skin, "medium");
        label.setWrap(true);
        label.setAlignment(Align.center);
        label.setColor(color);

        Table toast = new Table(skin);
        if (skin.has("bundle_reward_multiplier", Label.LabelStyle.class)
                && skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background != null) {
            toast.setBackground(skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background);
        }
        toast.pad(16, 24, 16, 24);
        toast.add(label).width(TOAST_WIDTH - 48f);
        toast.setWidth(TOAST_WIDTH);

        toast.getColor().a = 0f;
        toast.addAction(Actions.sequence(
                Actions.fadeIn(0.2f),
                Actions.delay(TOAST_DURATION),
                Actions.fadeOut(0.3f),
                Actions.removeActor()
        ));

        addActor(toast);
    }

    private void evictOldest() {
        if (getChildren().size == 0) {
            return;
        }
        Actor oldest = getChildren().get(0);
        oldest.clearActions();
        Actor host = getParent();
        if (!(host instanceof Group group)) {
            oldest.remove();
            return;
        }
        oldest.localToStageCoordinates(stagePos.setZero());
        oldest.remove();
        group.addActor(oldest);
        group.stageToLocalCoordinates(localPos.set(stagePos));
        oldest.setPosition(localPos.x, localPos.y);
        oldest.toFront();
        oldest.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeOut(EVICT_DURATION),
                        Actions.moveBy(0, EVICT_SLIDE, EVICT_DURATION)
                ),
                Actions.removeActor()
        ));
    }
}
