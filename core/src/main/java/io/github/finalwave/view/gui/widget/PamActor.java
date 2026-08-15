package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public final class PamActor extends Actor {
    private final PamPlayer player;
    private final Color tint = new Color(Color.WHITE);
    private final Color oldBatchColor = new Color();
    private final Color drawColor = new Color();
    private final Matrix4 scaledTransform = new Matrix4();
    private final HashMap<String, Boolean> visibility = new HashMap<>();
    private String pamPath;
    private String clipName;
    private float stateTime;
    private boolean playing = true;
    private boolean loop = true;
    private float drawScale = 1f;
    private boolean flipX;
    private float offsetX;
    private float offsetY;
    private float anchorX = 0.5f;
    private float anchorY = 0.32f;
    private String followUpClip;
    private boolean followUpLoop = true;
    private Runnable onIntroFinished;
    private boolean visibilityActive;

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
        this.drawScale = scale;
        this.loop = loop;
        boolean same = Objects.equals(this.pamPath, pamPath) && Objects.equals(this.clipName, clipName);
        if (same) {
            return;
        }
        this.pamPath = pamPath;
        this.clipName = clipName;
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

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public void setVisibility(Map<String, Boolean> parts) {
        visibility.clear();
        visibilityActive = parts != null;
        if (parts != null) {
            visibility.putAll(parts);
        }
    }

    public void setTint(Color tint) {
        if (tint == null) {
            this.tint.set(Color.WHITE);
            return;
        }
        this.tint.set(tint);
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
        float scaleX = flipX ? -Math.abs(drawScale) : Math.abs(drawScale);
        float scaleY = Math.abs(drawScale);
        oldBatchColor.set(batch.getColor());
        drawColor.set(getColor()).mul(tint);
        drawColor.a *= parentAlpha;
        batch.setColor(drawColor);
        Map<String, Boolean> vis = visibilityActive ? visibility : null;
        try {
            if (vis == null) {
                player.draw(batch, pamPath, clipName, stateTime, cx, cy, scaleX, scaleY, loop);
                return;
            }
            if (scaleX == 1f && scaleY == 1f) {
                player.draw(batch, pamPath, clipName, stateTime, cx, cy, loop, vis);
                return;
            }
            Matrix4 previous = batch.getTransformMatrix();
            scaledTransform.set(previous);
            scaledTransform.translate(cx, cy, 0f);
            scaledTransform.scale(scaleX, scaleY, 1f);
            scaledTransform.translate(-cx, -cy, 0f);
            batch.setTransformMatrix(scaledTransform);
            player.draw(batch, pamPath, clipName, stateTime, cx, cy, loop, vis);
            batch.setTransformMatrix(previous);
        } finally {
            batch.setColor(oldBatchColor);
        }
    }
}
