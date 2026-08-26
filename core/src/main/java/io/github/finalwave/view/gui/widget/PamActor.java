package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.render.HitFlashShader;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public final class PamActor extends Actor {
    private static final float HIT_FLASH_SECONDS = 0.18f;

    private final PamPlayer player;
    private final HitFlashShader hitFlash;
    private final Color tint = new Color(Color.WHITE);
    private final Color oldBatchColor = new Color();
    private final Color drawColor = new Color();
    private final Matrix4 scaledTransform = new Matrix4();
    private final Matrix4 previousTransform = new Matrix4();
    private final HashMap<String, Boolean> visibility = new HashMap<>();
    private String pamPath;
    private String clipName;
    private float stateTime;
    private boolean playing = true;
    private float playbackSpeed = 1f;
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
    private boolean clipFinished;
    private boolean visibilityActive;
    private String drawPart;
    private float rotateOffsetX;
    private float rotateOffsetY;
    private float hitFlashRemaining;
    private boolean drawFailed;

    public PamActor(PamPlayer player) {
        this(player, null);
    }

    public PamActor(PamPlayer player, HitFlashShader hitFlash) {
        this.player = player;
        this.hitFlash = hitFlash;
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
        this.clipFinished = false;
        this.drawFailed = false;
        if (pamPath != null) {
            player.loadAsync(pamPath, () -> {
            });
        }
    }

    public float drawScale() {
        return drawScale;
    }

    public void setDrawScale(float scale) {
        this.drawScale = Math.abs(scale);
    }

    public void playThen(String pamPath, String clipName, float scale, String nextClip, boolean nextLoop, Runnable onFinished) {
        setClip(pamPath, clipName, scale, false);
        this.followUpClip = nextClip;
        this.followUpLoop = nextLoop;
        this.onIntroFinished = onFinished;
    }

    public void playOnce(String pamPath, String clipName, float scale, Runnable onFinished) {
        setClip(pamPath, clipName, scale, false);
        this.onIntroFinished = onFinished;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public String clipName() {
        return clipName;
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

    public void setDrawPart(String part) {
        this.drawPart = part;
    }

    public void setRotateOffset(float rotateOffsetX, float rotateOffsetY) {
        this.rotateOffsetX = rotateOffsetX;
        this.rotateOffsetY = rotateOffsetY;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = Math.max(0f, playbackSpeed);
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

    public void flashHit() {
        flashHit(HIT_FLASH_SECONDS);
    }

    public void flashHit(float seconds) {
        hitFlashRemaining = Math.max(hitFlashRemaining, seconds);
    }

    @Override
    public void act(float delta) {
        if (!playing) {
            return;
        }
        super.act(delta);
        if (hitFlashRemaining > 0f) {
            hitFlashRemaining = Math.max(0f, hitFlashRemaining - delta);
        }
        if (pamPath == null) {
            return;
        }
        stateTime += delta * playbackSpeed;
        if (loop || clipFinished) {
            return;
        }
        float duration;
        try {
            duration = player.clipDurationSeconds(pamPath, clipName);
        } catch (RuntimeException e) {
            logDrawFailure(e);
            return;
        }
        if (stateTime < duration) {
            return;
        }
        clipFinished = true;
        String next = followUpClip;
        boolean nextLoop = followUpLoop;
        Runnable finished = onIntroFinished;
        followUpClip = null;
        onIntroFinished = null;
        if (next != null) {
            setClip(pamPath, next, drawScale, nextLoop);
        }
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
        boolean flashing = hitFlashRemaining > 0f && hitFlash != null && hitFlash.isReady();
        if (flashing) {
            hitFlash.bind(batch, hitFlashRemaining / HIT_FLASH_SECONDS);
        }
        Map<String, Boolean> vis = visibilityActive ? visibility : null;
        boolean rotated = getRotation() != 0f;
        boolean scaled = scaleX != 1f || scaleY != 1f;
        boolean part = drawPart != null;
        boolean restoreTransform = false;
        try {
            if (!part && vis == null && !rotated) {
                player.draw(batch, pamPath, clipName, stateTime, cx, cy, scaleX, scaleY, loop);
                return;
            }
            if (!part && vis != null && !scaled && !rotated) {
                player.draw(batch, pamPath, clipName, stateTime, cx, cy, loop, vis);
                return;
            }
            if (part && !scaled && !rotated) {
                player.drawPart(batch, pamPath, clipName, stateTime, cx, cy, drawPart);
                return;
            }
            float ox = scaleX * rotateOffsetX;
            float oy = scaleY * rotateOffsetY;
            float rx = cx + ox;
            float ry = cy + oy;
            previousTransform.set(batch.getTransformMatrix());
            scaledTransform.set(previousTransform);
            scaledTransform.translate(rx, ry, 0f);
            if (rotated) {
                scaledTransform.rotate(0f, 0f, 1f, getRotation());
            }
            scaledTransform.translate(-ox, -oy, 0f);
            scaledTransform.scale(scaleX, scaleY, 1f);
            scaledTransform.translate(-cx, -cy, 0f);
            batch.setTransformMatrix(scaledTransform);
            restoreTransform = true;
            if (part) {
                player.drawPart(batch, pamPath, clipName, stateTime, cx, cy, drawPart);
            } else if (vis == null) {
                player.draw(batch, pamPath, clipName, stateTime, cx, cy, 1f, 1f, loop);
            } else {
                player.draw(batch, pamPath, clipName, stateTime, cx, cy, loop, vis);
            }
        } catch (RuntimeException e) {
            logDrawFailure(e);
        } finally {
            if (restoreTransform) {
                batch.setTransformMatrix(previousTransform);
            }
            if (flashing) {
                hitFlash.unbind(batch);
            }
            batch.setColor(oldBatchColor);
        }
    }

    private void logDrawFailure(RuntimeException e) {
        if (drawFailed) {
            return;
        }
        drawFailed = true;
        Gdx.app.error("PamActor", "Failed to draw " + pamPath + " clip " + clipName, e);
    }
}
