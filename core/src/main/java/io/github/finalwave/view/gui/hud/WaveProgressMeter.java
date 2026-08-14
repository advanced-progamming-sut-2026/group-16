package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.Wave;
import io.github.finalwave.model.game.WaveManager;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;

import java.util.ArrayList;
import java.util.List;


public final class WaveProgressMeter extends WidgetGroup {
    private static final float WIDTH = 420f;
    private static final float HEIGHT = 48f;

    private final Image meter;
    private final Image fill;
    private final Image head;
    private final GameAssets assets;
    private final List<Image> flags = new ArrayList<>();
    private int flagCount = -1;

    public WaveProgressMeter(GameAssets assets) {
        this.assets = assets;
        setSize(WIDTH, HEIGHT);
        meter = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PROGRESS_METER)));
        meter.setScaling(Scaling.stretch);
        meter.setSize(WIDTH, HEIGHT);
        fill = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PROGRESS_FILL)));
        fill.setScaling(Scaling.stretch);
        head = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PROGRESS_ZOMBIE_HEAD)));
        head.setScaling(Scaling.fit);
        addActor(meter);
        addActor(fill);
        addActor(head);
        layoutFill(0f);
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
        float progress = progressOf(session);
        ensureFlags(session);
        layoutFill(progress);
    }

    private void ensureFlags(GameSession session) {
        WaveManager waves = session == null ? null : session.getWaveManager();
        int count = waves == null ? 0 : waves.getWaveCount();
        if (count == flagCount) {
            return;
        }
        flagCount = count;
        for (Image flag : flags) {
            flag.remove();
        }
        flags.clear();
        if (waves == null) {
            return;
        }
        for (Wave wave : waves.getWaves()) {
            if (!wave.isFlagWave()) {
                continue;
            }
            Image flag = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PROGRESS_FLAG)));
            flag.setScaling(Scaling.fit);
            float x = WIDTH * (wave.getNumber() / (float) Math.max(1, waves.getWaveCount())) - 14f;
            flag.setSize(28f, 36f);
            flag.setPosition(Math.max(0f, Math.min(WIDTH - 28f, x)), 10f);
            flags.add(flag);
            addActor(flag);
        }
    }

    private void layoutFill(float progress) {
        float clamped = Math.max(0f, Math.min(1f, progress));
        float fillWidth = Math.max(8f, WIDTH * clamped);
        fill.setSize(fillWidth, HEIGHT * 0.55f);
        fill.setPosition(0f, (HEIGHT - fill.getHeight()) / 2f);
        head.setSize(36f, 36f);
        head.setPosition(Math.max(0f, fillWidth - 18f), (HEIGHT - 36f) / 2f);
        head.toFront();
    }

    private static float progressOf(GameSession session) {
        if (session == null || session.getWaveManager() == null) {
            return 0f;
        }
        WaveManager waves = session.getWaveManager();
        if (!waves.areWavesStarted() || waves.getWaveCount() <= 0) {
            return 0f;
        }
        Wave current = waves.getCurrentWave();
        double destroyed = current == null ? 0d : current.getDestroyedHealthRatio();
        double value = (waves.getCurrentWaveNumber() - 1 + destroyed) / waves.getWaveCount();
        return (float) Math.max(0d, Math.min(1d, value));
    }
}
