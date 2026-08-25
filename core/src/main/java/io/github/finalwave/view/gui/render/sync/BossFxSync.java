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
    private static final float FIREBALL_FLIGHT_SCALE = 1.35f;
    private static final float FIREBALL_IMPACT_SCALE = 1.4f;
    private static final float FLIGHT_DELAY = 0.5f;
    private static final float FLIGHT_SECONDS = 1.5f;
    private static final float FIREBALL_FLIGHT_DELAY = 1.0f;
    private static final float FIREBALL_FLIGHT_SECONDS = 1.0f;
    private static final float FLIGHT_START_Y = 900f;
    private static final float WIND_SCALE = 1.35f;
    private static final float WIND_SECONDS = 2.5f;
    private static final float WIND_WIDTH_TILES = 3.6f;
    private static final float SHARK_SCALE = 1.25f;
    private static final float SHARK_SURFACE_SECONDS = 2.0f;
    private static final float SHARK_ATTACK_SECONDS = 2.13f;
    private static final float TURBINE_SCALE = 1.4f;
    private static final float TURBINE_SECONDS = 4.1f;
    private static final float TURBINE_WIDTH_TILES = 4.2f;

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
        if (vfx.kind() == BossVfx.Kind.LOCK_RETICLE || vfx.kind() == BossVfx.Kind.LOCK_RETICLE_ICE) {
            spawnLock(path, clip, center, vfx);
            return;
        }
        if (vfx.kind() == BossVfx.Kind.MISSILE_FLIGHT) {
            spawnFlight(path, clip, center, vfx.row(), FLIGHT_SCALE, FLIGHT_DELAY, FLIGHT_SECONDS);
            return;
        }
        if (vfx.kind() == BossVfx.Kind.ICE_MISSILE_FLIGHT) {
            spawnFlight(path, clip, center, vfx.row(), FLIGHT_SCALE, FLIGHT_DELAY, FLIGHT_SECONDS);
            return;
        }
        if (vfx.kind() == BossVfx.Kind.FIREBALL_FLIGHT) {
            spawnFlight(path, clip, center, vfx.row(),
                    FIREBALL_FLIGHT_SCALE, FIREBALL_FLIGHT_DELAY, FIREBALL_FLIGHT_SECONDS);
            return;
        }
        if (vfx.kind() == BossVfx.Kind.ICE_WIND) {
            spawnWind(vfx.row());
            return;
        }
        if (vfx.kind() == BossVfx.Kind.SHARK) {
            spawnShark(vfx.col(), vfx.row());
            return;
        }
        if (vfx.kind() == BossVfx.Kind.VACUUM) {
            spawnTurbine(vfx.row());
            return;
        }
        if (vfx.kind() == BossVfx.Kind.MISSILE_EGYPT || vfx.kind() == BossVfx.Kind.MISSILE_ICE) {
            removeLock(vfx.col(), vfx.row());
        }
        PamActor actor = place(center, vfx.row());
        float impactScale = vfx.kind() == BossVfx.Kind.FIREBALL ? FIREBALL_IMPACT_SCALE : IMPACT_SCALE;
        actor.playOnce(path, clip, impactScale, actor::remove);
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

    private void spawnWind(int row) {
        Vector2 right = layout.cellCenter(Math.max(0, layout.cols() - 1), row);
        Vector2 left = layout.cellCenter(0, row);
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setSize(layout.tileWidth() * WIND_WIDTH_TILES, layout.tileHeight() * 1.5f);
        actor.setClip(ZombossClips.CHILL_WIND, "animation", WIND_SCALE, true);
        float y = right.y - actor.getHeight() / 2f;
        float startX = right.x + layout.tileWidth();
        float endX = left.x - layout.tileWidth() * 2f;
        actor.setPosition(startX - actor.getWidth() / 2f, y);
        actor.setUserObject(row);
        layer.addActor(actor);
        actor.addAction(Actions.sequence(
                Actions.moveTo(endX - actor.getWidth() / 2f, y, WIND_SECONDS, Interpolation.linear),
                Actions.run(actor::remove)));
        playing.add(actor);
    }

    private void spawnShark(int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        PamActor actor = place(center, row);
        actor.setSize(layout.tileWidth() * 1.25f, layout.tileHeight() * 1.2f);
        actor.playThen(ZombossClips.SHARK, "idle2", SHARK_SCALE, "attack", false, null);
        actor.addAction(Actions.sequence(
                Actions.delay(SHARK_SURFACE_SECONDS + SHARK_ATTACK_SECONDS),
                Actions.run(actor::remove)));
    }

    private void spawnTurbine(int row) {
        Vector2 left = layout.cellCenter(0, row);
        Vector2 right = layout.cellCenter(Math.max(0, layout.cols() - 1), row);
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setSize(layout.tileWidth() * TURBINE_WIDTH_TILES, layout.tileHeight() * 1.6f);
        actor.setClip(ZombossClips.TURBINE, "animation", TURBINE_SCALE, true);
        float y = left.y - actor.getHeight() / 2f;
        float startX = left.x - layout.tileWidth();
        float endX = right.x + layout.tileWidth();
        actor.setPosition(startX - actor.getWidth() / 2f, y);
        actor.setUserObject(row);
        layer.addActor(actor);
        actor.addAction(Actions.sequence(
                Actions.moveTo(endX - actor.getWidth() / 2f, y, TURBINE_SECONDS, Interpolation.linear),
                Actions.run(actor::remove)));
        playing.add(actor);
    }

    private void spawnFlight(String path, String clip, Vector2 center, int row,
            float scale, float delay, float seconds) {
        PamActor actor = place(center, row);
        actor.setClip(path, clip, scale, true);
        float x = center.x - actor.getWidth() / 2f;
        float endY = center.y - actor.getHeight() / 2f;
        actor.setPosition(x, endY + FLIGHT_START_Y);
        actor.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.moveTo(x, endY, seconds, Interpolation.sineIn),
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
            case LOCK_RETICLE_ICE, ICE_MISSILE_FLIGHT, MISSILE_ICE -> ZombossClips.ICE_MISSILE;
            case FIREBALL_FLIGHT, FIREBALL -> ZombossClips.DARK_FIREBALL;
            case ICE_WIND -> ZombossClips.CHILL_WIND;
            case SHARK -> ZombossClips.SHARK;
            case VACUUM -> ZombossClips.TURBINE;
            case GLACIER -> ZombossClips.GLACIER;
        };
    }

    private static String clipOf(BossVfx.Kind kind) {
        return switch (kind) {
            case LOCK_RETICLE, LOCK_RETICLE_ICE -> "missile_lock_reticle";
            case MISSILE_FLIGHT, ICE_MISSILE_FLIGHT -> "missile";
            case MISSILE_EGYPT, MISSILE_ICE -> "missile_explosion";
            case FIREBALL_FLIGHT -> "fall";
            case FIREBALL -> "impact";
            case ICE_WIND -> "animation";
            case SHARK -> "attack";
            case VACUUM -> "animation";
            case GLACIER -> "idle";
        };
    }
}
