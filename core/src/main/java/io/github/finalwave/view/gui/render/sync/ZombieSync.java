package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;


public final class ZombieSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final ZombieClips clips;
    private final Group layer;
    private final ActorRegistry<Zombie, PamActor> zombies = new ActorRegistry<>();
    private float tickFraction;

    public ZombieSync(GameAssets assets, LawnLayout layout, ZombieClips clips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        sync(session, 0f);
    }

    public void sync(GameSession session, float tickFraction) {
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null) {
            return;
        }
        List<Zombie> live = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (ZombieVisualState.shouldDraw(zombie)) {
                live.add(zombie);
            }
        }
        zombies.sync(live, this::spawn, this::update, PamActor::remove);
    }

    public void clear() {
        zombies.clear(PamActor::remove);
    }

    private PamActor spawn(Zombie zombie) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private void update(Zombie zombie, PamActor actor) {
        float worldX = layout.worldX(displayX(zombie));
        float worldY = layout.worldYForRow(zombie.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        EntityAnimationCatalog.ClipSpec clip = ZombieVisualState.clip(zombie, clips);
        actor.setClip(clip.path(), clip.clip(), LawnLayout.ZOMBIE_SCALE, true);
        actor.setFlipX(zombie.isMovingRight() || zombie.isHypnotized());
        actor.setTint(ZombieVisualState.tint(zombie));
        actor.setVisibility(ZombieVisualState.armorVisibility(zombie, clips));
        actor.setUserObject(zombie.getRow() * 8);
        actor.setVisible(!zombie.isSubmerged());
    }

    private double displayX(Zombie zombie) {
        double modelX = zombie.getX();
        if (tickFraction <= 0f
                || zombie.getState() != ZombieState.MOVING
                || zombie.isStationary()) {
            return modelX;
        }
        double step = zombie.getCurrentSpeed() / GameSession.TICKS_PER_SECOND * tickFraction;
        if (zombie.isMovingRight()) {
            return modelX + step;
        }
        return modelX - step;
    }
}
