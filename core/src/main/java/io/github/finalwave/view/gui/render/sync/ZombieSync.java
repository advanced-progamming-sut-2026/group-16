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
import io.github.finalwave.view.gui.render.clip.ArmorPartVisibility;
import io.github.finalwave.view.gui.render.clip.ZombossClips;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.widget.ActorFades;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PamPartCentroid;
import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class ZombieSync {
    private static final float DEBRIS_DROP_SECONDS = 0.5f;
    private static final float DEBRIS_DROP_X = 0.35f;
    private static final float HEAD_DROP_Y = 0.58f;
    private static final float ARM_DROP_Y = 0.7f;
    private static final float ARMOR_DROP_Y = 1.15f;
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
    private final Set<PamActor> bossActors = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<PamActor, String> bossLogical = new IdentityHashMap<>();
    private final Set<Armor> thrownArmor = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Armor, String> lastArmorLayers = new IdentityHashMap<>();
    private final ActorRegistry<Zombie, PamActor> iceShells = new ActorRegistry<>();
    private GameSession session;
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
        this.session = session;
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
        iceShells.sync(encased(live), this::spawnIceShell, this::updateIceShell, PamActor::remove);
        hits.retain(live);
        retainThrownArmor(live);
    }

    public void clear() {
        zombies.clear(PamActor::remove);
        iceShells.clear(PamActor::remove);
        hits.clear();
        aliases.clear();
        thrownArmor.clear();
        lastArmorLayers.clear();
        bossActors.clear();
        bossLogical.clear();
        ArmorPartVisibility.clear();
        for (PamActor actor : deathActors) {
            actor.remove();
        }
        deathActors.clear();
        session = null;
    }

    private PamActor spawn(Zombie zombie) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        if (zombie.isBoss()) {
            actor.setAnchor(0.5f, LawnLayout.ZOMBOSS_ANCHOR_Y);
            bossActors.add(actor);
        } else {
            actor.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
        }
        layer.addActor(actor);
        return actor;
    }

    private void update(Zombie zombie, PamActor actor) {
        float worldX = layout.worldX(displayX(zombie));
        float worldY = layout.worldYForRow(displayY(zombie));
        float scale = LawnLayout.ZOMBIE_SCALE;
        if (zombie.isBoss()) {
            worldY = layout.worldYForRow(displayY(zombie) + 1);
            actor.setSize(layout.tileWidth() * 1.85f, layout.tileHeight() * 2.0f);
            scale = LawnLayout.ZOMBOSS_SCALE;
            if ("ZombieDarkZomboss".equals(zombie.getType())) {
                worldX += layout.tileWidth() * LawnLayout.ZOMBOSS_DARK_OFFSET_X;
            }
            bossActors.add(actor);
        } else {
            actor.setSize(layout.tileWidth(), layout.tileHeight());
        }
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        applyClip(zombie, actor, scale);
        actor.setFlipX(!zombie.isBoss() && (zombie.isMovingRight() || zombie.isHypnotized()));
        actor.setTint(ZombieVisualState.tint(zombie, session));
        EntityAnimationCatalog.ClipSpec clip = ZombieVisualState.clip(zombie, clips);
        actor.setVisibility(ArmorPartVisibility.expand(assets.pamPlayer(), clip.path(),
                ZombieVisualState.armorVisibility(zombie, clips)));
        actor.setUserObject(zombie.getRow() * 8);
        actor.setVisible(!zombie.isSubmerged());
        aliases.put(actor, zombie.getType());
        hits.observe(zombie, flashHealth(zombie), actor, zombie.isBoss() ? 0.28f : 0.18f);
        throwBrokenArmor(zombie, actor, clip);
    }

    private List<Zombie> encased(List<Zombie> live) {
        List<Zombie> frozen = new ArrayList<>();
        for (Zombie zombie : live) {
            if (zombie.isBoss() || zombie.getFreezeTicksRemaining() < 40) {
                continue;
            }
            frozen.add(zombie);
        }
        return frozen;
    }

    private PamActor spawnIceShell(Zombie zombie) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
        actor.playOnce(
                ZombossClips.ICE_BLOCK_ZOMBIE_SPAWN,
                ZombossClips.ICE_BLOCK_ZOMBIE_SPAWN_CLIP,
                LawnLayout.ICE_BLOCK_SCALE,
                () -> actor.setClip(
                        ZombossClips.ICE_BLOCK_ZOMBIE, "idle", LawnLayout.ICE_BLOCK_SCALE, true));
        layer.addActor(actor);
        return actor;
    }

    private void updateIceShell(Zombie zombie, PamActor actor) {
        float worldX = layout.worldX(displayX(zombie));
        float worldY = layout.worldYForRow(displayY(zombie));
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        if (!ZombossClips.ICE_BLOCK_ZOMBIE_SPAWN_CLIP.equals(actor.clipName())) {
            actor.setClip(ZombossClips.ICE_BLOCK_ZOMBIE, "idle", LawnLayout.ICE_BLOCK_SCALE, true);
        }
        actor.setUserObject(zombie.getRow() * 8 + 3);
        actor.setVisible(!zombie.isSubmerged());
    }

    private void applyClip(Zombie zombie, PamActor actor, float scale) {
        if (!zombie.isBoss()) {
            EntityAnimationCatalog.ClipSpec clip = ZombieVisualState.clip(zombie, clips);
            actor.setClip(clip.path(), clip.clip(), scale, true);
            return;
        }
        String logical = zombie.getPresentationClip();
        if (logical.equals(bossLogical.get(actor))) {
            actor.setDrawScale(scale);
            return;
        }
        bossLogical.put(actor, logical);
        clips.applyBoss(actor, zombie.getType(), logical, scale);
    }

    private void beginDeath(PamActor actor) {
        actor.flashHit();
        String alias = aliases.remove(actor);
        bossLogical.remove(actor);
        if (alias == null || !clips.hasDie(alias)) {
            actor.remove();
            return;
        }
        EntityAnimationCatalog.ClipSpec die = clips.die(alias);
        EntityAnimationCatalog.ClipSpec parts = clips.particles(alias);
        spawnParticles(actor, parts);
        deathActors.add(actor);
        actor.setVisibility(null);
        float scale = bossActors.remove(actor) ? LawnLayout.ZOMBOSS_SCALE : LawnLayout.ZOMBIE_SCALE;
        actor.playOnce(die.path(), die.clip(), scale,
                () -> actor.addAction(ActorFades.holdThenFade(() -> deathActors.remove(actor))));
    }

    private void throwBrokenArmor(Zombie zombie, PamActor body, EntityAnimationCatalog.ClipSpec clip) {
        for (Armor armor : zombie.getArmorLayers()) {
            if (!armor.isDestroyed()) {
                String layer = clips.armorLayer(armor);
                if (layer != null) {
                    lastArmorLayers.put(armor, layer);
                }
                continue;
            }
            if (!thrownArmor.add(armor)) {
                continue;
            }
            String part = lastArmorLayers.remove(armor);
            if (part == null) {
                part = clips.armorLayer(armor);
            }
            if (part == null || !ArmorPartVisibility.hasPart(assets.pamPlayer(), clip.path(), part)) {
                continue;
            }
            spawnDebris(body, clip, part);
        }
    }

    private void retainThrownArmor(List<Zombie> live) {
        Set<Armor> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Zombie zombie : live) {
            keep.addAll(zombie.getArmorLayers());
        }
        thrownArmor.retainAll(keep);
        lastArmorLayers.keySet().retainAll(keep);
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
        if (partName == null || PARTICLE_HEAD.equals(partName)) {
            return HEAD_DROP_Y;
        }
        return ARMOR_DROP_Y;
    }

    private double displayX(Zombie zombie) {
        if (zombie.isBoss()) {
            return lerp(zombie.getPreviousX(), zombie.getX(), tickFraction);
        }
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

    private double displayY(Zombie zombie) {
        if (zombie.isBoss()) {
            return lerp(zombie.getPreviousY(), zombie.getY(), tickFraction);
        }
        return zombie.getY();
    }

    private static double lerp(double from, double to, float fraction) {
        return from + (to - from) * Math.max(0f, Math.min(1f, fraction));
    }
}
