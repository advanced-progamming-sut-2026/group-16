package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;

public final class ZombossHealthMeter extends WidgetGroup {
    private static final float WIDTH = 420f;
    private static final float HEIGHT = 48f;
    private static final float NATIVE_W = 313f;
    private static final float NATIVE_H = 33f;
    private static final float NATIVE_HEAD_W = 47f;
    private static final float NATIVE_HEAD_H = 53f;
    private static final float NATIVE_TROUGH_X = 40f;
    private static final float NATIVE_TROUGH_W = 265f;
    private static final float NATIVE_TROUGH_Y = 8f;
    private static final float NATIVE_TROUGH_H = 17f;
    private static final float NATIVE_NOTCH_W = 5f;
    private static final int SEGMENTS = 3;

    private final Image meter;
    private final Image head;
    private final Image[] fills = new Image[SEGMENTS];
    private final Image[] notches = new Image[SEGMENTS - 1];

    public ZombossHealthMeter(GameAssets assets) {
        setSize(WIDTH, HEIGHT);
        meter = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.ZOMBOSS_METER)));
        meter.setScaling(Scaling.stretch);
        head = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.ZOMBOSS_HEAD)));
        head.setScaling(Scaling.fit);
        addActor(meter);
        for (int i = 0; i < fills.length; i++) {
            Image fill = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.ZOMBOSS_FILL)));
            fill.setScaling(Scaling.stretch);
            fills[i] = fill;
            addActor(fill);
        }
        for (int i = 0; i < notches.length; i++) {
            Image notch = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.ZOMBOSS_NOTCH)));
            notch.setScaling(Scaling.stretch);
            notches[i] = notch;
            addActor(notch);
        }
        addActor(head);
        layoutFill(1f);
    }

    @Override
    public float getPrefWidth() {
        return WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return HEIGHT;
    }

    public void refresh(GameSession session) {
        float ratio = 1f;
        if (session != null && session.getBossMaxHealth() > 0) {
            ratio = session.getBossHealth() / (float) session.getBossMaxHealth();
        }
        layoutFill(Math.max(0f, Math.min(1f, ratio)));
    }

    private void layoutFill(float ratio) {
        float scale = WIDTH / NATIVE_W;
        float meterH = NATIVE_H * scale;
        float meterY = (HEIGHT - meterH) / 2f;
        meter.setSize(WIDTH, meterH);
        meter.setPosition(0f, meterY);

        float troughX = NATIVE_TROUGH_X * scale;
        float troughW = NATIVE_TROUGH_W * scale;
        float troughY = meterY + NATIVE_TROUGH_Y * scale;
        float troughH = NATIVE_TROUGH_H * scale;
        float gap = NATIVE_NOTCH_W * scale;
        float inner = Math.max(1f, troughW - gap * notches.length);
        float segmentW = inner / SEGMENTS;
        for (int i = 0; i < fills.length; i++) {
            float start = i / (float) SEGMENTS;
            float local = Math.max(0f, Math.min(1f, (ratio - start) * SEGMENTS));
            float width = segmentW * local;
            fills[i].setVisible(width > 0.5f);
            fills[i].setSize(width, troughH);
            fills[i].setPosition(troughX + i * (segmentW + gap), troughY);
        }
        for (int i = 0; i < notches.length; i++) {
            notches[i].setSize(gap, troughH);
            notches[i].setPosition(troughX + (i + 1) * segmentW + i * gap, troughY);
            notches[i].toFront();
        }
        float headW = NATIVE_HEAD_W * scale;
        float headH = NATIVE_HEAD_H * scale;
        head.setSize(headW, headH);
        head.setPosition(meterH * 0.08f - (headW - meterH) * 0.35f, (HEIGHT - headH) / 2f);
        head.toFront();
    }
}
