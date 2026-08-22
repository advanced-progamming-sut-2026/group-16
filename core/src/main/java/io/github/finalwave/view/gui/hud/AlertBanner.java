package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


public final class AlertBanner extends WidgetGroup {
    private static final Color BANNER_RED = new Color(0.86f, 0.07f, 0.07f, 1f);
    private static final float TEXT_WIDTH = 1500f;
    private static final float WORD_SCALE = 1.28f;
    private static final float LINE_SCALE = 0.78f;
    private static final float POP_IN = 0.12f;
    private static final float SETTLE = 0.10f;
    private static final float HOLD_WORD = 0.58f;
    private static final float HOLD_LINE = 1.55f;
    private static final float HOLD_PLANT = 0.95f;
    private static final float FADE_OUT = 0.18f;
    private static final float GAP = 0.08f;

    private final Label label;
    private final Deque<String> queue = new ArrayDeque<>();
    private boolean playing;
    private Runnable onQueueEmpty;

    public AlertBanner(Skin skin) {
        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.disabled);
        label = new Label("", skin, styleName(skin));
        label.setAlignment(Align.center);
        label.setColor(BANNER_RED);
        label.setWrap(true);
        label.setOrigin(Align.center);
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(label).width(TEXT_WIDTH);
        addActor(table);
    }

    public void show(String message) {
        enqueue(message, null);
    }

    public void showSequence(List<String> messages, Runnable onFinished) {
        if (messages == null || messages.isEmpty()) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        onQueueEmpty = onFinished;
        for (String message : messages) {
            if (message != null && !message.isBlank()) {
                queue.addLast(message);
            }
        }
        if (!playing) {
            playNext();
        }
    }

    public void reset() {
        queue.clear();
        onQueueEmpty = null;
        playing = false;
        label.clearActions();
        label.getColor().a = 0f;
        setVisible(false);
    }

    private void enqueue(String message, Runnable onFinished) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (onFinished != null) {
            onQueueEmpty = onFinished;
        }
        queue.addLast(message);
        if (!playing) {
            playNext();
        }
    }

    private void playNext() {
        if (queue.isEmpty()) {
            playing = false;
            setVisible(false);
            Runnable finished = onQueueEmpty;
            onQueueEmpty = null;
            if (finished != null) {
                finished.run();
            }
            return;
        }
        playing = true;
        setVisible(true);
        toFront();
        String text = queue.removeFirst();
        boolean word = text.length() <= 8;
        float hold = "PLANT!".equals(text) ? HOLD_PLANT : (word ? HOLD_WORD : HOLD_LINE);
        float scale = word ? WORD_SCALE : LINE_SCALE;
        label.setText(text);
        label.setFontScale(scale);
        label.pack();
        label.setWidth(TEXT_WIDTH);
        label.invalidate();
        label.validate();
        label.setOrigin(label.getWidth() * 0.5f, label.getHeight() * 0.5f);
        label.setScale(0.42f);
        label.getColor().a = 0f;
        label.clearActions();
        label.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeIn(POP_IN),
                        Actions.scaleTo(1.18f, 1.18f, POP_IN, Interpolation.sineOut)),
                Actions.scaleTo(1f, 1f, SETTLE, Interpolation.sine),
                Actions.delay(hold),
                Actions.parallel(
                        Actions.fadeOut(FADE_OUT),
                        Actions.scaleTo(1.08f, 1.08f, FADE_OUT, Interpolation.sineIn)),
                Actions.delay(GAP),
                Actions.run(this::playNext)
        ));
    }

    private static String styleName(Skin skin) {
        if (skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        if (skin.has("big", Label.LabelStyle.class)) {
            return "big";
        }
        return "medium";
    }
}
