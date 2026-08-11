package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;


public final class ToastArea extends Table {
    private static final float TOAST_WIDTH = 560f;
    private static final float TOAST_DURATION = 3.2f;
    private static final int MAX_TOASTS = 3;

    private final Skin skin;
    private int activeCount;

    public ToastArea(Skin skin) {
        this.skin = skin;
        setFillParent(true);
        top();
        padTop(40);
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
        if (activeCount >= MAX_TOASTS && getChildren().size > 0) {
            getChildren().first().remove();
            activeCount = Math.max(0, activeCount - 1);
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

        toast.getColor().a = 0f;
        toast.addAction(Actions.sequence(
                Actions.fadeIn(0.2f),
                Actions.delay(TOAST_DURATION),
                Actions.fadeOut(0.3f),
                Actions.run(() -> activeCount = Math.max(0, activeCount - 1)),
                Actions.removeActor()
        ));

        add(toast).width(TOAST_WIDTH).padBottom(10).row();
        activeCount++;
    }
}
