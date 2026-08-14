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
    private float anchorX = 0.5f;
    private float anchorY = 0.32f;
    private String followUpClip;
    private boolean followUpLoop = true;
    private Runnable onIntroFinished;

    public PamActor(PamPlayer player) {
        this.player = player;
    }

    public void setClip(PlantAnimationCatalog.ClipSpec spec, float scale) {
        setClip(spec, scale, true);
    }

    public void setClip(PlantAnimationCatalog.ClipSpec spec, float scale, boolean loop) {
        if (spec == null) {
            setClip((String) null, null, scale, loop);
            return;
        }
        setClip(spec.path(), spec.clip(), scale, loop);
    }

    public void setClip(String pamPath, String clipName, float scale, boolean loop) {
        this.pamPath = pamPath;
        this.clipName = clipName;
        this.drawScale = scale;
        this.loop = loop;
        this.stateTime = 0f;
        this.followUpClip = null;
        this.followUpLoop = true;
        this.onIntroFinished = null;
        if (pamPath != null) {
            player.loadAsync(pamPath, () -> {
            });
        }
    }

    public void playThen(String pamPath, String clipName, float scale, String nextClip, boolean nextLoop, Runnable onFinished) {
        setClip(pamPath, clipName, scale, false);
        this.followUpClip = nextClip;
        this.followUpLoop = nextLoop;
        this.onIntroFinished = onFinished;
    }

    public void setAnchor(float anchorX, float anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
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
        if (!playing || pamPath == null) {
            return;
        }
        stateTime += delta;
        if (loop || followUpClip == null) {
            return;
        }
        float duration = player.clipDurationSeconds(pamPath, clipName);
        if (stateTime < duration) {
            return;
        }
        String next = followUpClip;
        boolean nextLoop = followUpLoop;
        Runnable finished = onIntroFinished;
        followUpClip = null;
        onIntroFinished = null;
        setClip(pamPath, next, drawScale, nextLoop);
        if (finished != null) {
            finished.run();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (pamPath == null || clipName == null) {
            return;
        }
        float cx = getX() + getWidth() * anchorX + offsetX;
        float cy = getY() + getHeight() * anchorY + offsetY;
        player.draw(batch, pamPath, clipName, stateTime, cx, cy, drawScale, drawScale, loop);
    }
}
