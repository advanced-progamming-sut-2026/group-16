package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.widget.ActorFades;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PamPartCentroid;
import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public final class ZombieSync {
    private static final float DEBRIS_DROP_SECONDS = 0.5f;
    private static final float DEBRIS_DROP_X = 0.35f;
    private static final float HEAD_DROP_Y = 0.58f;
    private static final float ARM_DROP_Y = 0.7f;
    private static final String PARTICLE_HEAD = "particle_head";
    private static final String PARTICLE_ARM = "particle_arm";

    private final GameAssets assets;
    private final LawnLayout layout;
    private final ZombieClips clips;
    private final Group layer;
    private final ActorRegistry<Zombie, PamActor> zombies = new ActorRegistry<>();
    private final HitFlashTracker<Zombie> hits = new HitFlashTracker<>();
    private final Map<PamActor, String> aliases = new IdentityHashMap<>();
    private final List<PamActor> deathActors = new ArrayList<>();
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
        zombies.sync(live, this::spawn, this::update, this::beginDeath);
        hits.retain(live);
    }

    public void clear() {
        zombies.clear(PamActor::remove);
        hits.clear();
        aliases.clear();
        for (PamActor actor : deathActors) {
            actor.remove();
        }
        deathActors.clear();
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
        aliases.put(actor, zombie.getType());
        hits.observe(zombie, flashHealth(zombie), actor);
    }

    private void beginDeath(PamActor actor) {
        actor.flashHit();
        String alias = aliases.remove(actor);
        if (alias == null || !clips.hasDie(alias)) {
            actor.remove();
            return;
        }
        EntityAnimationCatalog.ClipSpec die = clips.die(alias);
        EntityAnimationCatalog.ClipSpec parts = clips.particles(alias);
        spawnParticles(actor, parts);
        deathActors.add(actor);
        actor.setVisibility(null);
        actor.playOnce(die.path(), die.clip(), LawnLayout.ZOMBIE_SCALE,
                () -> actor.addAction(ActorFades.holdThenFade(() -> deathActors.remove(actor))));
    }

    private List<PamActor> spawnParticles(PamActor body, EntityAnimationCatalog.ClipSpec parts) {
        if (parts == null) {
            return List.of();
        }
        assets.pamPlayer().loadSync(parts.path());
        List<String> names = debrisPartNames(parts.path());
        if (names.isEmpty()) {
            return List.of(spawnDebris(body, parts, null));
        }
        List<PamActor> spawned = new ArrayList<>();
        for (String name : names) {
            spawned.add(spawnDebris(body, parts, name));
        }
        return spawned;
    }

    private List<String> debrisPartNames(String pamPath) {
        List<String> found = new ArrayList<>();
        collectDebrisParts(assets.pamPlayer().getParts(pamPath), found);
        List<String> names = new ArrayList<>();
        if (found.contains(PARTICLE_HEAD)) {
            names.add(PARTICLE_HEAD);
        }
        if (found.contains(PARTICLE_ARM)) {
            names.add(PARTICLE_ARM);
        }
        return names;
    }

    private static void collectDebrisParts(PamPlayer.AnimationPart part, List<String> names) {
        if (part == null) {
            return;
        }
        String name = part.name;
        if (PARTICLE_HEAD.equals(name) || PARTICLE_ARM.equals(name)) {
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        for (PamPlayer.AnimationPart child : part.children) {
            collectDebrisParts(child, names);
        }
    }

    private PamActor spawnDebris(PamActor body, EntityAnimationCatalog.ClipSpec parts, String partName) {
        PamActor debris = new PamActor(assets.pamPlayer());
        debris.setTouchable(Touchable.disabled);
        debris.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
        debris.setSize(body.getWidth(), body.getHeight());
        debris.setPosition(body.getX(), body.getY());
        debris.setFlipX(body.isFlipX());
        Object sort = body.getUserObject();
        int key = sort instanceof Integer value ? value : 0;
        debris.setUserObject(key + 1);
        debris.setClip(parts.path(), parts.clip(), LawnLayout.ZOMBIE_SCALE, false);
        if (partName != null) {
            debris.setDrawPart(partName);
            Vector2 centroid = PamPartCentroid.of(assets.pamPlayer(), parts.path(), parts.clip(), partName);
            debris.setRotateOffset(centroid.x, centroid.y);
        }
        debris.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.moveBy(-layout.tileWidth() * DEBRIS_DROP_X,
                                -layout.tileHeight() * debrisDropY(partName),
                                DEBRIS_DROP_SECONDS, Interpolation.sineOut),
                        Actions.rotateBy(debrisRotation(partName), DEBRIS_DROP_SECONDS, Interpolation.sineOut)),
                ActorFades.holdThenFade(() -> deathActors.remove(debris))));
        layer.addActor(debris);
        deathActors.add(debris);
        return debris;
    }

    private static int flashHealth(Zombie zombie) {
        int health = zombie.getHealth();
        for (Armor armor : zombie.getArmorLayers()) {
            health += armor.getHealth();
        }
        return health;
    }

    private static float debrisRotation(String partName) {
        if (PARTICLE_ARM.equals(partName)) {
            return 270f;
        }
        return 180f;
    }

    private static float debrisDropY(String partName) {
        if (PARTICLE_ARM.equals(partName)) {
            return ARM_DROP_Y;
        }
        return HEAD_DROP_Y;
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
