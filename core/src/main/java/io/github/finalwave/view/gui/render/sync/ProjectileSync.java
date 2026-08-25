package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ProjectileClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public final class ProjectileSync {
    private static final float STRAIGHT_SPEED = 0.3f;
    private static final float ARCING_SPEED = 0.25f;
    private static final float DEFAULT_ARC_SPAN = 4f;
    private static final float ARC_HEIGHT_TILES = 1.15f;
    private static final float SPLAT_SCALE = 1.25f;
    private static final float SPLAT_LIFT = 0.28f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final ProjectileClips clips;
    private final Group layer;
    private final ActorRegistry<Projectile, PamActor> projectiles = new ActorRegistry<>();
    private final Map<PamActor, ArcFlight> arcs = new IdentityHashMap<>();
    private final Map<PamActor, ProjectileEffect> effects = new IdentityHashMap<>();
    private GameSession session;
    private float tickFraction;

    public ProjectileSync(GameAssets assets, LawnLayout layout, ProjectileClips clips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
    }

    public void sync(GameSession session, float tickFraction) {
        this.session = session;
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null || session.getProjectileSystem() == null) {
            return;
        }
        List<Projectile> live = new ArrayList<>(session.getProjectileSystem().getProjectiles());
        projectiles.sync(live, this::spawn, this::update, this::onHit);
    }

    public void clear() {
        projectiles.clear(this::removeOnly);
        arcs.clear();
        effects.clear();
        session = null;
    }

    private PamActor spawn(Projectile projectile) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        layer.addActor(actor);
        assets.audio().playThrow();
        effects.put(actor, projectile.getEffect());
        if (isArcing(projectile)) {
            double launchX = projectile.getX();
            arcs.put(actor, new ArcFlight(launchX, arcSpan(projectile, launchX)));
        }
        return actor;
    }

    private void update(Projectile projectile, PamActor actor) {
        float displayX = (float) displayX(projectile);
        float worldX = layout.worldX(displayX);
        float worldY = layout.worldYForRow(projectile.getRow())
                + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
        ArcFlight arc = arcs.get(actor);
        if (arc != null) {
            worldY += arcLift(displayX, arc);
        }
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY - actor.getHeight() / 2f);
        EntityAnimationCatalog.ClipSpec spec = clips.clip(projectile.getEffect());
        actor.setClip(spec.path(), spec.clip(), clips.scale(projectile.getEffect()), true);
        actor.setFlipX(projectile.isFromZombie());
        actor.setUserObject(projectile.getRow() * 8 + 1);
        effects.put(actor, projectile.getEffect());
    }

    private void onHit(PamActor actor) {
        assets.audio().playHit();
        spawnSplat(actor);
        removeOnly(actor);
    }

    private void removeOnly(PamActor actor) {
        arcs.remove(actor);
        effects.remove(actor);
        actor.remove();
    }

    private void spawnSplat(PamActor source) {
        EntityAnimationCatalog.ClipSpec spec = clips.splat(effects.get(source));
        PamActor splat = new PamActor(assets.pamPlayer());
        splat.setTouchable(Touchable.disabled);
        splat.setAnchor(0.5f, 0.5f);
        splat.setSize(source.getWidth(), source.getHeight());
        splat.setPosition(source.getX(), source.getY() + layout.tileHeight() * SPLAT_LIFT);
        splat.setUserObject(source.getUserObject());
        splat.setFlipX(source.isFlipX());
        Group parent = source.getParent();
        if (parent == null) {
            parent = layer;
        }
        parent.addActor(splat);
        splat.playOnce(spec.path(), spec.clip(), SPLAT_SCALE, splat::remove);
    }

    private float arcLift(float displayX, ArcFlight arc) {
        double traveled = displayX - arc.launchX;
        double t = Math.max(0.0, Math.min(1.0, traveled / arc.span));
        return (float) (4.0 * layout.tileHeight() * ARC_HEIGHT_TILES * t * (1.0 - t));
    }

    private double arcSpan(Projectile projectile, double launchX) {
        double nearest = Double.NaN;
        if (session != null) {
            for (Zombie zombie : session.getZombies()) {
                if (!zombie.isAlive() || !zombie.occupiesRow(projectile.getRow())) {
                    continue;
                }
                double distance = zombie.getX() - launchX;
                if (distance > 0 && (Double.isNaN(nearest) || distance < nearest)) {
                    nearest = distance;
                }
            }
        }
        if (Double.isNaN(nearest)) {
            return DEFAULT_ARC_SPAN;
        }
        return Math.max(nearest, 0.01);
    }

    private double displayX(Projectile projectile) {
        double modelX = projectile.getX();
        if (tickFraction <= 0f) {
            return modelX;
        }
        double step = speed(projectile) * tickFraction;
        if (projectile.isFromZombie()) {
            return modelX - step;
        }
        return modelX + step;
    }

    private static boolean isArcing(Projectile projectile) {
        return projectile.getProfile() != null
                && projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING;
    }

    private static double speed(Projectile projectile) {
        if (isArcing(projectile)) {
            return ARCING_SPEED;
        }
        return STRAIGHT_SPEED;
    }

    private record ArcFlight(double launchX, double span) {
    }
}
