package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.boss.BossVfx;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ZombossClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class BossFxSync {
    private static final float LOCK_SCALE = 1.15f;
    private static final float FLIGHT_SCALE = 1.2f;
    private static final float IMPACT_SCALE = 1.2f;
    private static final float FLIGHT_DELAY = 0.5f;
    private static final float FLIGHT_SECONDS = 1.5f;
    private static final float FLIGHT_START_Y = 900f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final List<PamActor> playing = new ArrayList<>();
    private final Map<String, PamActor> locks = new HashMap<>();

    public BossFxSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        for (BossVfx vfx : session.drainBossVfx()) {
            spawn(vfx);
        }
        Iterator<PamActor> iterator = playing.iterator();
        while (iterator.hasNext()) {
            PamActor actor = iterator.next();
            if (actor.getStage() == null) {
                iterator.remove();
            }
        }
        locks.values().removeIf(actor -> actor.getStage() == null);
    }

    public void clear() {
        for (PamActor actor : playing) {
            actor.remove();
        }
        playing.clear();
        for (PamActor actor : locks.values()) {
            actor.remove();
        }
        locks.clear();
    }

    private void spawn(BossVfx vfx) {
        if (vfx == null) {
            return;
        }
        String path = pathOf(vfx.kind());
        String clip = clipOf(vfx.kind());
        if (path == null || clip == null) {
            return;
        }
        Vector2 center = layout.cellCenter(vfx.col(), vfx.row());
        if (vfx.kind() == BossVfx.Kind.LOCK_RETICLE) {
            spawnLock(path, clip, center, vfx);
            return;
        }
        if (vfx.kind() == BossVfx.Kind.MISSILE_FLIGHT) {
            spawnFlight(path, clip, center, vfx.row());
            return;
        }
        if (vfx.kind() == BossVfx.Kind.MISSILE_EGYPT || vfx.kind() == BossVfx.Kind.MISSILE_ICE) {
            removeLock(vfx.col(), vfx.row());
        }
        PamActor actor = place(center, vfx.row());
        actor.playOnce(path, clip, IMPACT_SCALE, actor::remove);
    }

    private void spawnLock(String path, String clip, Vector2 center, BossVfx vfx) {
        String key = key(vfx.col(), vfx.row());
        PamActor existing = locks.get(key);
        if (existing != null) {
            existing.remove();
        }
        PamActor actor = place(center, vfx.row());
        actor.setClip(path, clip, LOCK_SCALE, true);
        locks.put(key, actor);
    }

    private void spawnFlight(String path, String clip, Vector2 center, int row) {
        PamActor actor = place(center, row);
        actor.setClip(path, clip, FLIGHT_SCALE, true);
        float x = center.x - actor.getWidth() / 2f;
        float endY = center.y - actor.getHeight() / 2f;
        actor.setPosition(x, endY + FLIGHT_START_Y);
        actor.addAction(Actions.sequence(
                Actions.delay(FLIGHT_DELAY),
                Actions.moveTo(x, endY, FLIGHT_SECONDS, Interpolation.sineIn),
                Actions.run(actor::remove)));
    }

    private PamActor place(Vector2 center, int row) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.45f);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setUserObject(row);
        layer.addActor(actor);
        playing.add(actor);
        return actor;
    }

    private void removeLock(int col, int row) {
        PamActor lock = locks.remove(key(col, row));
        if (lock != null) {
            lock.remove();
        }
    }

    private static String key(int col, int row) {
        return col + "," + row;
    }

    private static String pathOf(BossVfx.Kind kind) {
        return switch (kind) {
            case LOCK_RETICLE, MISSILE_FLIGHT, MISSILE_EGYPT -> ZombossClips.EGYPT_MISSILE;
            case MISSILE_ICE -> ZombossClips.ICE_MISSILE;
            case FIREBALL -> ZombossClips.DARK_FIREBALL;
            case SHARK -> ZombossClips.SHARK;
            case VACUUM -> ZombossClips.TURBINE;
            case GLACIER -> ZombossClips.GLACIER;
        };
    }

    private static String clipOf(BossVfx.Kind kind) {
        return switch (kind) {
            case LOCK_RETICLE -> "missile_lock_reticle";
            case MISSILE_FLIGHT -> "missile";
            case MISSILE_EGYPT, MISSILE_ICE -> "missile_explosion";
            case FIREBALL -> "impact";
            case SHARK -> "attack";
            case VACUUM -> "animation";
            case GLACIER -> "idle";
        };
    }
}
