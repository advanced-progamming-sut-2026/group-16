package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LawnBurst;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ExplosionLooks;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public final class LawnBurstSync {
    private static final float LASER_SKULL_OFFSET_X = 0.62f;
    private static final float LASER_FROM_ORIGIN_Y = -0.58f;
    private static final float SCORCH_TILE_WIDTH = 1.85f;
    private static final float SCORCH_HOLD = 0.4f;
    private static final float SCORCH_FADE = 0.55f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final List<Actor> playing = new ArrayList<>();
    private BiConsumer<Float, Float> onShake;

    public LawnBurstSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void setOnShake(BiConsumer<Float, Float> onShake) {
        this.onShake = onShake;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        for (LawnBurst burst : session.drainLawnBursts()) {
            spawn(burst);
        }
        Iterator<Actor> iterator = playing.iterator();
        while (iterator.hasNext()) {
            Actor actor = iterator.next();
            if (actor.getStage() == null) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        for (Actor actor : playing) {
            actor.remove();
        }
        playing.clear();
    }

    private void spawn(LawnBurst burst) {
        if (burst == null) {
            return;
        }
        LawnBurst.Kind kind = burst.kind() == null ? LawnBurst.Kind.GENERIC : burst.kind();
        if (kind == LawnBurst.Kind.LASER) {
            spawnLaser(burst);
            return;
        }
        Vector2 center = layout.cellCenter(burst.col(), burst.row());
        float scale = ExplosionLooks.scale(kind);
        String clip = ExplosionLooks.clip(kind);
        if (ExplosionLooks.hasScorch(kind)) {
            spawnScorch(center, burst.row());
        }
        String rear = ExplosionLooks.rearPath(kind);
        if (rear != null) {
            playBurst(center, burst.row(), rear, clip, scale, kind);
        }
        playBurst(center, burst.row(), ExplosionLooks.path(kind), clip, scale, kind);
        assets.audio().playExplosion();
        if (onShake != null && ExplosionLooks.shakeSeconds(kind) > 0f) {
            onShake.accept(ExplosionLooks.shakeSeconds(kind), ExplosionLooks.shakePixels(kind));
        }
    }

    private void spawnLaser(LawnBurst burst) {
        int span = Math.max(1, burst.span());
        float rightX = layout.worldX(burst.originX() - LASER_SKULL_OFFSET_X);
        float width = layout.tileWidth() * span;
        float height = layout.tileHeight();
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(1f, 0.42f);
        actor.setSize(width, height);
        actor.setPosition(rightX - width,
                layout.worldYForRow(burst.row())
                        + layout.tileHeight() * (LawnLayout.ZOMBIE_ANCHOR_Y + LASER_FROM_ORIGIN_Y));
        actor.setUserObject(burst.row());
        layer.addActor(actor);
        playing.add(actor);
        actor.playOnce(ExplosionLooks.path(LawnBurst.Kind.LASER), ExplosionLooks.clip(LawnBurst.Kind.LASER),
                1.0f, actor::remove);
    }

    private void spawnScorch(Vector2 center, int row) {
        TextureRegion region = assets.region(ExplosionLooks.SCORCH_IMAGE);
        if (region == null) {
            return;
        }
        float width = layout.tileWidth() * SCORCH_TILE_WIDTH;
        float height = width * region.getRegionHeight() / (float) Math.max(1, region.getRegionWidth());
        Image scorch = new Image(new TextureRegionDrawable(region));
        scorch.setTouchable(Touchable.disabled);
        scorch.setSize(width, height);
        scorch.setPosition(center.x - width / 2f, center.y - height / 2f);
        scorch.setUserObject(row);
        layer.addActor(scorch);
        playing.add(scorch);
        scorch.addAction(Actions.sequence(
                Actions.delay(SCORCH_HOLD),
                Actions.fadeOut(SCORCH_FADE),
                Actions.removeActor()));
    }

    private void playBurst(Vector2 center, int row, String path, String clip, float scale,
            LawnBurst.Kind kind) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setDrawOffset(0f, ExplosionLooks.liftY(kind, scale));
        actor.setUserObject(row);
        layer.addActor(actor);
        playing.add(actor);
        actor.playOnce(path, clip, scale, actor::remove);
    }
}
