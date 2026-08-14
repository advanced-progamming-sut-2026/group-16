package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;


public final class AlertBanner extends Table {
    private static final float WIDTH = 720f;
    private static final float DURATION = 3.4f;

    private final Label label;

    public AlertBanner(Skin skin) {
        setFillParent(true);
        top();
        padTop(28f);
        setVisible(false);
        label = new Label("", skin, "medium");
        label.setAlignment(Align.center);
        label.setColor(Color.WHITE);
        label.setWrap(true);
        Table banner = new Table(skin);
        banner.setColor(0.72f, 0.12f, 0.12f, 0.92f);
        if (skin.has("bundle_reward_multiplier", Label.LabelStyle.class)
                && skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background != null) {
            banner.setBackground(skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background);
        }
        banner.pad(14f, 28f, 14f, 28f);
        banner.add(label).width(WIDTH - 56f);
        add(banner).width(WIDTH);
    }

    public void show(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        label.setText(message);
        setVisible(true);
        clearActions();
        getColor().a = 0f;
        addAction(Actions.sequence(
                Actions.fadeIn(0.15f),
                Actions.delay(DURATION),
                Actions.fadeOut(0.25f),
                Actions.run(() -> setVisible(false))
        ));
    }
}
