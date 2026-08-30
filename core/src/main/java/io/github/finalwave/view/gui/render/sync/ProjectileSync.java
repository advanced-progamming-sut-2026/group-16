package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.projectile.FumeHitMark;
import io.github.finalwave.model.game.entity.projectile.GrapeshotMuzzles;
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
    private static final float ARC_HEIGHT_TILES = 2.2f;
    private static final float SPLAT_SCALE = 1.25f;
    private static final float SPLAT_LIFT = 0.28f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final ProjectileClips clips;
    private final Group layer;
    private final ActorRegistry<Projectile, PamActor> projectiles = new ActorRegistry<>();
    private final Map<PamActor, ArcFlight> arcs = new IdentityHashMap<>();
    private final Map<PamActor, ProjectileEffect> effects = new IdentityHashMap<>();
    private final Map<PamActor, String> visualClips = new IdentityHashMap<>();
    private final Map<PamActor, EntityAnimationCatalog.ClipSpec> splats = new IdentityHashMap<>();
    private final Map<PamActor, Float> splatScales = new IdentityHashMap<>();
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
        playFumeHits(session.getProjectileSystem().drainFumeHits());
    }

    public void clear() {
        projectiles.clear(this::removeOnly);
        arcs.clear();
        effects.clear();
        visualClips.clear();
        splats.clear();
        splatScales.clear();
        session = null;
    }

    private PamActor spawn(Projectile projectile) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, isFumeCloud(projectile) ? LawnLayout.PLANT_ANCHOR_Y : 0.5f);
        layer.addActor(actor);
        assets.audio().playThrow();
        effects.put(actor, projectile.getEffect());
        visualClips.put(actor, projectile.getVisualClip());
        rememberSplat(actor, projectile);
        if (isArcing(projectile)) {
            double launchX = projectile.getX();
            arcs.put(actor, new ArcFlight(launchX, arcSpan(projectile, launchX),
                    projectile.getLaneYOffset(), projectile.getRow()));
        }
        return actor;
    }

    private void update(Projectile projectile, PamActor actor) {
        float displayX = (float) displayX(projectile);
        float displayY = (float) displayY(projectile);
        float worldX = layout.worldX(displayX);
        float worldY;
        if (isGrapeshotGrape(projectile)) {
            worldY = projectileWorldY(projectile, displayX);
        } else {
            worldY = layout.worldYForRow(displayY + projectile.getVisualLaneOffset())
                    + layout.tileHeight() * anchorY(projectile);
        }
        ArcFlight arc = arcs.get(actor);
        if (arc != null) {
            arc = resolvedArc(projectile, arc);
            arcs.put(actor, arc);
            double t = arcProgress(displayX, arc);
            worldY = arcingWorldY(projectile, t, arc);
        }
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY - actor.getHeight() / 2f);
        EntityAnimationCatalog.ClipSpec spec = clips.clip(projectile);
        boolean loop = !isFumeCloud(projectile);
        actor.setClip(spec.path(), spec.clip(), clips.scale(projectile), loop);
        actor.setFlipX(projectile.isFromZombie());
        actor.setUserObject(projectileSortKey(projectile));
        effects.put(actor, projectile.getEffect());
        visualClips.put(actor, projectile.getVisualClip());
        rememberSplat(actor, projectile);
    }

    private static float anchorY(Projectile projectile) {
        float custom = projectile.getVisualAnchorY();
        if (custom >= 0f) {
            return custom;
        }
        return LawnLayout.PROJECTILE_ANCHOR_Y;
    }

    private void onHit(PamActor actor) {
        if (splats.get(actor) != null) {
            assets.audio().playHit();
            spawnSplat(actor);
        }
        removeOnly(actor);
    }

    private void removeOnly(PamActor actor) {
        arcs.remove(actor);
        effects.remove(actor);
        visualClips.remove(actor);
        splats.remove(actor);
        splatScales.remove(actor);
        actor.remove();
    }

    private void rememberSplat(PamActor actor, Projectile projectile) {
        EntityAnimationCatalog.ClipSpec splat = clips.splat(projectile);
        if (splat == null) {
            splats.remove(actor);
            splatScales.remove(actor);
            return;
        }
        splats.put(actor, splat);
        splatScales.put(actor, clips.scale(projectile));
    }

    private void spawnSplat(PamActor source) {
        EntityAnimationCatalog.ClipSpec spec = splats.get(source);
        if (spec == null) {
            ProjectileEffect effect = effects.get(source);
            spec = clips.splat(effect, visualClips.get(source));
        }
        if (spec == null) {
            return;
        }
        float splatScale = splatScales.getOrDefault(source, SPLAT_SCALE);
        PamActor splat = new PamActor(assets.pamPlayer());
        splat.setTouchable(Touchable.disabled);
        splat.setAnchor(0.5f, 0.5f);
        splat.setSize(source.getWidth(), source.getHeight());
        float splatX = source.getX();
        float splatY = source.getY() + layout.tileHeight() * SPLAT_LIFT;
        ArcFlight arc = arcs.get(source);
        if (arc != null) {
            float worldX = layout.worldX(arc.launchX + arc.span);
            float worldY = layout.worldYForRow(arc.row)
                    + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
            splatX = worldX - source.getWidth() / 2f;
            splatY = worldY - source.getHeight() / 2f;
        }
        splat.setPosition(splatX, splatY);
        splat.setUserObject(source.getUserObject());
        splat.setFlipX(source.isFlipX());
        Group parent = source.getParent();
        if (parent == null) {
            parent = layer;
        }
        parent.addActor(splat);
        splat.playOnce(spec.path(), spec.clip(), splatScale, splat::remove);
    }

    private void playFumeHits(List<FumeHitMark> hits) {
        EntityAnimationCatalog.ClipSpec spec = clips.fumeHit();
        if (spec == null || hits == null || hits.isEmpty()) {
            return;
        }
        for (FumeHitMark hit : hits) {
            if (hit == null) {
                continue;
            }
            spawnFumeHit(spec, hit);
        }
    }

    private void spawnFumeHit(EntityAnimationCatalog.ClipSpec spec, FumeHitMark hit) {
        assets.audio().playHit();
        PamActor splat = new PamActor(assets.pamPlayer());
        splat.setTouchable(Touchable.disabled);
        splat.setAnchor(0.5f, 0.5f);
        splat.setSize(layout.tileWidth(), layout.tileHeight());
        float worldX = layout.worldX(hit.x());
        float worldY = layout.worldYForRow(hit.row())
                + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
        splat.setPosition(worldX - splat.getWidth() / 2f, worldY - splat.getHeight() / 2f);
        splat.setUserObject(hit.row() * 8 + 1);
        layer.addActor(splat);
        splat.playOnce(spec.path(), spec.clip(), LawnLayout.PROJECTILE_DRAW_SCALE, splat::remove);
    }

    private float arcingWorldY(Projectile projectile, double t, ArcFlight arc) {
        float ground = layout.worldYForRow(projectile.getRow())
                + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
        float launchOffset = (float) (arc.launchYOffset * layout.tileHeight() * (1.0 - t));
        return ground + launchOffset + arcLift(t);
    }

    private float arcLift(double t) {
        return (float) (4.0 * layout.tileHeight() * ARC_HEIGHT_TILES * t * (1.0 - t));
    }

    private ArcFlight resolvedArc(Projectile projectile, ArcFlight arc) {
        if (!projectile.hasLandX()) {
            return arc;
        }
        double span = Math.max(projectile.getLandX() - arc.launchX, 0.01);
        if (span == arc.span && arc.row == projectile.getRow()) {
            return arc;
        }
        return new ArcFlight(arc.launchX, span, arc.launchYOffset, projectile.getRow());
    }

    private double arcProgress(float displayX, ArcFlight arc) {
        double traveled = displayX - arc.launchX;
        return Math.max(0.0, Math.min(1.0, traveled / arc.span));
    }

    private double arcSpan(Projectile projectile, double launchX) {
        if (projectile.hasLandX()) {
            return Math.max(projectile.getLandX() - launchX, 0.01);
        }
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

    private float projectileWorldY(Projectile projectile) {
        return projectileWorldY(projectile, (float) projectile.getX());
    }

    private float projectileWorldY(Projectile projectile, float displayX) {
        if (projectile != null && projectile.isGrapeshotGrape()) {
            double rowPos = projectile.getRowPosition();
            if (tickFraction > 0f) {
                rowPos += projectile.getVelocityY() * tickFraction;
            }
            return grapeshotWorldY(rowPos);
        }
        float anchor = isFumeCloud(projectile)
                ? 0.5f
                : LawnLayout.PROJECTILE_ANCHOR_Y;
        return layout.worldYForRow(projectile.getRow())
                + layout.tileHeight() * anchor
                + (float) projectile.getLaneYOffset() * layout.tileHeight();
    }

    private float grapeshotWorldY(double rowPosition) {
        int baseRow = (int) Math.floor(rowPosition);
        float fraction = (float) (rowPosition - baseRow);
        float baseY = layout.worldYForRow(baseRow) + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
        if (fraction <= 0f || baseRow >= layout.rows() - 1) {
            return baseY;
        }
        int nextRow = baseRow + 1;
        float nextY = layout.worldYForRow(nextRow) + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
        return baseY + (nextY - baseY) * fraction;
    }

    private double displayX(Projectile projectile) {
        double modelX = projectile.getX();
        if (isGrapeshotGrape(projectile)) {
            if (tickFraction <= 0f) {
                return modelX;
            }
            return modelX + projectile.getVelocityX() * tickFraction;
        }
        if (isFumeCloud(projectile) || tickFraction <= 0f) {
            return clampArcX(projectile, modelX);
        }
        return clampArcX(projectile, modelX + velocityX(projectile) * tickFraction);
    }

    private double displayY(Projectile projectile) {
        double modelY = projectile.getY();
        if (tickFraction <= 0f || !projectile.isDirected()) {
            return modelY;
        }
        return modelY + projectile.getVy() * tickFraction;
    }

    private double velocityX(Projectile projectile) {
        if (projectile.isDirected()) {
            return projectile.getVx();
        }
        double step = speed(projectile);
        if (projectile.isFromZombie() || projectile.isReverse()) {
            return -step;
        }
        return step;
    }

    private static double clampArcX(Projectile projectile, double x) {
        if (isArcing(projectile) && projectile.hasLandX() && !projectile.isFromZombie()) {
            return Math.min(x, projectile.getLandX());
        }
        return x;

    private static boolean isArcing(Projectile projectile) {
        return projectile.getProfile() != null
                && projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING;
    }

    private static boolean isFumeCloud(Projectile projectile) {
        return projectile != null
                && !projectile.isFromZombie()
                && projectile.getEffect() == ProjectileEffect.FUME;
    }

    private static boolean isGrapeshotGrape(Projectile projectile) {
        return projectile != null && projectile.isGrapeshotGrape();
    }

    private static int projectileSortKey(Projectile projectile) {
        if (projectile.isGrapeshotGrape()) {
            return (int) Math.round(projectile.getRowPosition() * 8) + 1;
        }
        return projectile.getRow() * 8 + 1;
    }

    private static double speed(Projectile projectile) {
        if (isGrapeshotGrape(projectile)) {
            return GrapeshotMuzzles.GRAPE_SPEED_TILES_PER_TICK;
        }
        if (isArcing(projectile)) {
            return ARCING_SPEED;
        }
        return STRAIGHT_SPEED;
    }

    private record ArcFlight(double launchX, double span, double launchYOffset, int row) {
    }
}
