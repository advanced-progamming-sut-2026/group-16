package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ProjectileClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;


public final class ProjectileSync {
    private static final float STRAIGHT_SPEED = 0.3f;
    private static final float ARCING_SPEED = 0.25f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final ProjectileClips clips;
    private final Group layer;
    private final ActorRegistry<Projectile, PamActor> projectiles = new ActorRegistry<>();
    private float tickFraction;

    public ProjectileSync(GameAssets assets, LawnLayout layout, ProjectileClips clips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
    }

    public void sync(GameSession session, float tickFraction) {
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null || session.getProjectileSystem() == null) {
            return;
        }
        List<Projectile> live = new ArrayList<>(session.getProjectileSystem().getProjectiles());
        projectiles.sync(live, this::spawn, this::update, PamActor::remove);
    }

    public void clear() {
        projectiles.clear(PamActor::remove);
    }

    private PamActor spawn(Projectile projectile) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        layer.addActor(actor);
        return actor;
    }

    private void update(Projectile projectile, PamActor actor) {
        float worldX = layout.worldX(displayX(projectile));
        float worldY = layout.worldYForRow(projectile.getRow())
                + layout.tileHeight() * LawnLayout.PROJECTILE_ANCHOR_Y;
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY - actor.getHeight() / 2f);
        EntityAnimationCatalog.ClipSpec spec = clips.clip(projectile.getEffect());
        actor.setClip(spec.path(), spec.clip(), LawnLayout.PROJECTILE_SCALE, true);
        actor.setFlipX(projectile.isFromZombie());
        actor.setUserObject(projectile.getRow() * 8 + 1);
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

    private static double speed(Projectile projectile) {
        if (projectile.getProfile() != null
                && projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING) {
            return ARCING_SPEED;
        }
        return STRAIGHT_SPEED;
    }
}
