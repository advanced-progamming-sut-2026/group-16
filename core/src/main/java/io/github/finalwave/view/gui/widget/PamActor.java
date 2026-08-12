package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import pvz.libpvz.pam.PamPlayer;


public final class PamActor extends Actor {
    private final PamPlayer player;
    private String pamPath;
    private String clipName;
    private float stateTime;
    private boolean playing = true;
    private boolean loop = true;
    private float drawScale = 1f;
    private float offsetX;
    private float offsetY;

    public PamActor(PamPlayer player) {
        this.player = player;
    }

    public void setClip(PlantAnimationCatalog.ClipSpec spec, float scale) {
        setClip(spec, scale, true);
    }

    public void setClip(PlantAnimationCatalog.ClipSpec spec, float scale, boolean loop) {
        if (spec == null) {
            this.pamPath = null;
            this.clipName = null;
            return;
        }
        this.pamPath = spec.path();
        this.clipName = spec.clip();
        this.drawScale = scale;
        this.loop = loop;
        this.stateTime = 0f;
        player.loadAsync(pamPath, () -> {
        });
    }

    public float stateTime() {
        return stateTime;
    }

    public void setDrawOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (playing && pamPath != null) {
            stateTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (pamPath == null || clipName == null) {
            return;
        }
        float cx = getX() + getWidth() * 0.5f + offsetX;
        float cy = getY() + getHeight() * 0.32f + offsetY;
        player.draw(batch, pamPath, clipName, stateTime, cx, cy, drawScale, drawScale, loop);
    }
}
