package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.GargantuarImpAmmoParts;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.model.minigame.izombie.IZombieHandler;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ArmorPartVisibility;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.ZombossClips;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.render.clip.ZombieDeathLooks;
import io.github.finalwave.view.gui.render.clip.ZombotanyLooks;
import io.github.finalwave.view.gui.widget.ActorFades;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PamPartCentroid;
import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;


public final class ZombieSync {
    private static final float DEBRIS_DROP_SECONDS = 0.5f;
    private static final float DEBRIS_DROP_X = 0.35f;
    private static final float HEAD_DROP_Y = 0.58f;
    private static final float ARM_DROP_Y = 0.7f;
    private static final float ARMOR_DROP_Y = 1.15f;
    private static final float APPEAR_SECONDS = 0.28f;
    private static final float WALK_STRIDE_TILES = 0.32f;
    private static final float WALK_GAIT_MIN = 0.8f;
    private static final float WALK_GAIT_MAX = 2.2f;
    private static final String PARTICLE_HEAD = "particle_head";
    private static final String PARTICLE_ARM = "particle_arm";
    private static final float ICE_TRAP_WIDTH_TILES = 0.88f;
    private static final float ICE_TRAP_ASPECT = 72f / 162f;
    private static final int ICE_TRAP_SORT_OFFSET = 1;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final ZombieClips clips;
    private final PlantClips plantClips;
    private final Group layer;
    private final ActorRegistry<Zombie, PamActor> zombies = new ActorRegistry<>();
    private final ActorRegistry<Zombie, PamActor> overlays = new ActorRegistry<>();
    private final ActorRegistry<Zombie, Image> iceTraps = new ActorRegistry<>();
    private final HitFlashTracker<Zombie> hits = new HitFlashTracker<>();
    private final Map<PamActor, String> aliases = new IdentityHashMap<>();
    private final Vector2 headScratch = new Vector2();
    private final Map<PamActor, Long> appearStart = new IdentityHashMap<>();
    private final Map<Zombie, Image> producerBadges = new IdentityHashMap<>();
    private final Map<Zombie, Boolean> frozenPoses = new IdentityHashMap<>();
    private final List<PamActor> deathActors = new ArrayList<>();
    private final Set<PamActor> bossActors = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<PamActor, String> bossLogical = new IdentityHashMap<>();
    private final Map<PamActor, String> abilityLatch = new IdentityHashMap<>();
    private final Set<Armor> thrownArmor = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Armor, String> lastArmorLayers = new IdentityHashMap<>();
    private final ActorRegistry<Zombie, PamActor> iceShells = new ActorRegistry<>();
    private final Set<String> smashShaking = new HashSet<>();
    private final Map<String, Float> clipSeconds = new HashMap<>();
    private final Map<PamActor, Zombie> actorZombies = new IdentityHashMap<>();
    private GameSession session;
    private float tickFraction;
    private boolean matchPlaying = true;
    private BiConsumer<Float, Float> smashShake;

    public ZombieSync(GameAssets assets, LawnLayout layout, ZombieClips clips, PlantClips plantClips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.plantClips = plantClips;
        this.layer = layer;
    }

    public void setSmashShake(BiConsumer<Float, Float> smashShake) {
        this.smashShake = smashShake;
    }

    public void sync(GameSession session) {
        sync(session, 0f);
    }

    public void sync(GameSession session, float tickFraction) {
        sync(session, tickFraction, true);
    }

    public void sync(GameSession session, float tickFraction, boolean matchPlaying) {
        this.session = session;
        this.matchPlaying = matchPlaying;
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null) {
            return;
        }
        List<Zombie> live = new ArrayList<>();
        List<Zombie> frozen = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (ZombieVisualState.shouldDraw(zombie)) {
                live.add(zombie);
                if (zombie.getFreezeTicksRemaining() > 0) {
                    frozen.add(zombie);
                }
            }
        }
        zombies.sync(live, this::spawn, this::update, this::beginDeath);
        overlays.sync(overlayLive(live), this::spawnOverlay, this::updateOverlay, PamActor::remove);
        iceShells.sync(encased(live), this::spawnIceShell, this::updateIceShell, PamActor::remove);
        pruneProducerBadges(live);
        iceTraps.sync(frozen, this::spawnIceTrap, this::updateIceTrap, Image::remove);
        hits.retain(live);
        retainFrozenPoses(live);
        retainThrownArmor(live);
        actorZombies.keySet().retainAll(zombies.actors());
    }

    public void clear() {
        zombies.clear(PamActor::remove);
        overlays.clear(PamActor::remove);
        iceShells.clear(PamActor::remove);
        iceTraps.clear(Image::remove);
        hits.clear();
        aliases.clear();
        smashShaking.clear();
        appearStart.clear();
        for (Image badge : producerBadges.values()) {
            badge.remove();
        }
        producerBadges.clear();
        actorZombies.clear();
        frozenPoses.clear();
        thrownArmor.clear();
        lastArmorLayers.clear();
        bossActors.clear();
        bossLogical.clear();
        ArmorPartVisibility.clear();
        ZombotanyLooks.clear();
        clipSeconds.clear();
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
        if (shouldAppear(zombie)) {
            appearStart.put(actor, System.nanoTime());
            actor.getColor().a = 0f;
            actor.addAction(Actions.fadeIn(APPEAR_SECONDS));
        }
        return actor;
    }

    private void update(Zombie zombie, PamActor actor) {
        boolean frozen = zombie.getFreezeTicksRemaining() > 0;
        float worldX = layout.worldX(displayX(zombie));
        float worldY = layout.worldYForRow(displayY(zombie));
        float scale = LawnLayout.ZOMBIE_SCALE;
        float appear = 1f;
        Long started = appearStart.get(actor);
        if (started != null) {
            float t = (System.nanoTime() - started) / 1_000_000_000f / APPEAR_SECONDS;
            t = Math.max(0f, Math.min(1f, t));
            appear = t;
            scale = LawnLayout.ZOMBIE_SCALE * (0.45f + 0.55f * Interpolation.swingOut.apply(t));
            if (t >= 1f) {
                appearStart.remove(actor);
                scale = LawnLayout.ZOMBIE_SCALE;
                appear = 1f;
            }
        }
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
        if (zombie.isSubmerged()) {
            worldY -= layout.tileHeight() * 0.12f;
        }
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        EntityAnimationCatalog.ClipSpec clip = ZombieVisualState.clip(zombie, clips);
        if (frozen) {
            if (!Boolean.TRUE.equals(frozenPoses.put(zombie, true))) {
                actor.forceClip(clip.path(), clip.clip(), scale, true);
                actor.setStateTime(0f);
            }
            actor.setPlaying(false);
        } else {
            frozenPoses.remove(zombie);
            actor.setPlaying(matchPlaying);
            applyClip(zombie, actor, scale);
        }
        actor.setTimeScale(!matchPlaying || frozen ? 0f : locomotionScale(zombie, clip));
        actor.setFlipX(shouldFlip(zombie));
        applyThrownImpTilt(zombie, actor);
        actor.setTint(ZombieVisualState.tint(zombie, session));
        Map<String, Boolean> vis = ArmorPartVisibility.expand(assets.pamPlayer(), clip.path(),
                ZombieVisualState.partVisibility(zombie, clips, assets.pamPlayer(), clip.path()));
        if (ZombotanyLooks.plantFor(zombie.getType()) != null) {
            vis = ZombotanyLooks.withHeadHidden(assets.pamPlayer(), clip.path(), vis);
        }
        vis = hideRaStaffSun(zombie, vis);
        vis = hideExplorerTorch(zombie, vis);
        vis = hideGargantuarImpAmmo(zombie, vis, clip);
        actor.setVisibility(vis);
        actor.setUserObject(zombie.getRow() * 8);
        actor.setVisible(true);
        applySnorkelLook(zombie, actor, appear);
        if (session != null && session.isSandboxPractice() && zombie.getX() < 0f) {
            float fade = (float) Math.max(0.0, Math.min(1.0, (zombie.getX() + 1.8) / 1.8));
            actor.getColor().a *= fade;
        }
        aliases.put(actor, zombie.getType());
        actorZombies.put(actor, zombie);
        float flashSeconds = zombie.consumeSuppressHitFlash() || zombie.getPoisonTicksRemaining() > 0
                ? 0f
                : (zombie.isBoss() ? 0.28f : 0.18f);
        hits.observe(zombie, flashHealth(zombie), actor, flashSeconds);
        if (!frozen) {
            throwBrokenArmor(zombie, actor, clip);
        }
        updateProducerBadge(zombie, actor);
        maybeShakeSmash(zombie, clip);
    }

    private List<Zombie> overlayLive(List<Zombie> live) {
        List<Zombie> overlay = new ArrayList<>();
        for (Zombie zombie : live) {
            if (zombie != null && zombie.isAlive() && ZombotanyLooks.plantFor(zombie.getType()) != null) {
                overlay.add(zombie);
            }
        }
        return overlay;
    }

    private PamActor spawnOverlay(Zombie zombie) {
        PamActor overlay = assets.pamActor();
        overlay.setTouchable(Touchable.disabled);
        overlay.setAnchor(0.5f, ZombotanyLooks.overlayAnchorY());
        layer.addActor(overlay);
        return overlay;
    }

    private void updateOverlay(Zombie zombie, PamActor overlay) {
        String plantName = ZombotanyLooks.plantFor(zombie.getType());
        if (plantName == null) {
            overlay.setVisible(false);
            return;
        }
        PamActor body = zombies.get(zombie);
        boolean flipped = body != null
                ? body.isFlipX()
                : zombie.isMovingRight() || zombie.isHypnotized();
        float originX;
        float originY;
        float bodyScale;
        float headTime = 0f;
        if (body != null) {
            originX = body.getX() + body.getWidth() * 0.5f;
            originY = body.getY() + body.getHeight() * LawnLayout.ZOMBIE_ANCHOR_Y;
            bodyScale = body.drawScale();
            headTime = body.stateTime();
        } else {
            originX = layout.worldX(displayX(zombie));
            originY = layout.worldYForRow(displayY(zombie))
                    + layout.tileHeight() * LawnLayout.ZOMBIE_ANCHOR_Y;
            bodyScale = LawnLayout.ZOMBIE_SCALE;
        }
        Vector2 head = headWorld(zombie, originX, originY, bodyScale, flipped, headTime);
        overlay.setSize(layout.tileWidth(), layout.tileHeight());
        overlay.setAnchor(0.5f, ZombotanyLooks.overlayAnchorY());
        overlay.setPosition(
                head.x - overlay.getWidth() * 0.5f,
                head.y - overlay.getHeight() * ZombotanyLooks.overlayAnchorY()
                        - ZombotanyLooks.overlayDrop(plantName, layout.tileHeight()));
        overlay.setFlipX(!flipped);
        overlay.setTint(ZombieVisualState.tint(zombie, session));
        applyOverlayClip(overlay, plantName, zombie);
        EntityAnimationCatalog.ClipSpec idle = plantClips.idle(plantName);
        overlay.setVisibility(ZombotanyLooks.overlayVisibility(
                assets.pamPlayer(), plantName, idle.path()));
        overlay.setUserObject(zombie.getRow() * 8 + 2);
        overlay.setVisible(body == null || body.isVisible());
    }

    private Vector2 headWorld(Zombie zombie,
                              float originX,
                              float originY,
                              float bodyScale,
                              boolean flipped,
                              float time) {
        EntityAnimationCatalog.ClipSpec clip = ZombieVisualState.clip(zombie, clips);
        String part = ZombotanyLooks.attachPart(assets.pamPlayer(), clip.path());
        if (part != null) {
            PamPartCentroid.at(assets.pamPlayer(), clip.path(), clip.clip(), part, time, 0f, 0f, headScratch);
        } else {
            headScratch.setZero();
        }
        if (headScratch.len2() < 1f) {
            PamPartCentroid.at(assets.pamPlayer(), clip.path(), clip.clip(), PARTICLE_HEAD, time, 0f, 0f, headScratch);
        }
        float sign = flipped ? -1f : 1f;
        return headScratch.set(
                originX + sign * bodyScale * headScratch.x,
                originY + bodyScale * headScratch.y);
    }

    private void applyOverlayClip(PamActor overlay, String plantName, Zombie zombie) {
        float scale = ZombotanyLooks.overlayScale(plantName);
        EntityAnimationCatalog.ClipSpec idle = plantClips.idle(plantName);
        boolean freezeIdle = "Peashooter".equals(plantName);
        boolean shoot = freezeIdle
                && zombie.getState() == ZombieState.ABILITY
                && plantClips.hasAttack(plantName);
        if (shoot) {
            EntityAnimationCatalog.ClipSpec attack = plantClips.attack(plantName);
            if (!attack.clip().equals(overlay.clipName())) {
                overlay.setPlaying(matchPlaying);
                if (matchPlaying) {
                    overlay.playThen(attack.path(), attack.clip(), scale, idle.clip(), false,
                            () -> overlay.setPlaying(false));
                }
            } else {
                overlay.setDrawScale(scale);
            }
            return;
        }
        if (plantClips.hasAttack(plantName) && plantClips.attack(plantName).clip().equals(overlay.clipName())) {
            overlay.setDrawScale(scale);
            return;
        }
        overlay.setClip(idle.path(), idle.clip(), scale, !freezeIdle);
        overlay.setPlaying(matchPlaying && !freezeIdle);
    }

    private boolean shouldAppear(Zombie zombie) {
        if (zombie == null || zombie.isBoss() || zombie.wasThrownByGargantuar()) {
            return false;
        }
        return session != null
                && session.isIZombieActive()
                && !zombie.isStationary();
    }

    private void applyThrownImpTilt(Zombie zombie, PamActor actor) {
        if (zombie == null || actor == null || !zombie.wasThrownByGargantuar() || !zombie.isInFlightArc()) {
            actor.setRotation(0f);
            return;
        }
        float tilt = zombie.arcTangentAngleDegrees(tickFraction);
        actor.setRotation(Math.max(-48f, Math.min(18f, tilt)));
    }

    private void updateProducerBadge(Zombie zombie, PamActor actor) {
        boolean show = session != null
                && session.isIZombieActive()
                && IZombieHandler.SUN_PRODUCER_ALIAS.equals(zombie.getType())
                && zombie.isStationary()
                && !zombie.isBoss()
                && zombie.isAlive();
        Image badge = producerBadges.get(zombie);
        if (!show) {
            if (badge != null) {
                badge.remove();
                producerBadges.remove(zombie);
            }
            return;
        }
        if (badge == null) {
            badge = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.SUN_ICON)));
            badge.setTouchable(Touchable.disabled);
            badge.setScaling(Scaling.fit);
            layer.addActor(badge);
            producerBadges.put(zombie, badge);
        }
        float size = layout.tileWidth() * 0.34f;
        badge.setSize(size, size);
        badge.setPosition(
                actor.getX() + actor.getWidth() * 0.58f,
                actor.getY() + actor.getHeight() * 0.68f);
        badge.setVisible(actor.isVisible());
    }

    private void pruneProducerBadges(List<Zombie> live) {
        Iterator<Map.Entry<Zombie, Image>> iterator = producerBadges.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Zombie, Image> entry = iterator.next();
            if (!live.contains(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
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
        actor.setVisible(true);
    }

    private void maybeShakeSmash(Zombie zombie, EntityAnimationCatalog.ClipSpec clip) {
        if (zombie == null || clip == null || clip.clip() == null) {
            return;
        }
        boolean smash = clip.clip().contains("smash")
                || (zombie.getState() == ZombieState.ABILITY
                && zombie.getType() != null
                && zombie.getType().contains("Gargantuar"));
        String id = zombie.getId();
        if (!smash) {
            smashShaking.remove(id);
            return;
        }
        if (!smashShaking.add(id) || smashShake == null) {
            return;
        }
        smashShake.accept(0.28f, 6f);
    }

    private void applyClip(Zombie zombie, PamActor actor, float scale) {
        if (!zombie.isBoss()) {
            EntityAnimationCatalog.ClipSpec clip = ZombieVisualState.clip(zombie, clips);
            if (ZombieClips.isOneShot(zombie.getType(), clip.clip())) {
                if (!clip.clip().equals(abilityLatch.get(actor))) {
                    abilityLatch.put(actor, clip.clip());
                    actor.playThen(clip.path(), clip.clip(), scale,
                            ZombieVisualState.followClip(zombie, clips), true, null);
                } else {
                    actor.setDrawScale(scale);
                }
                return;
            }
            abilityLatch.remove(actor);
            if (actor.hasFollowUp()) {
                actor.setDrawScale(scale);
                return;
            }
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

    private float locomotionScale(Zombie zombie, EntityAnimationCatalog.ClipSpec clip) {
        if (zombie == null || clip == null || zombie.isBoss() || !isLocomotionClip(clip.clip())) {
            return 1f;
        }
        float duration = clipDuration(clip);
        if (duration < 0.25f) {
            return 1f;
        }
        float gait = duration * (float) zombie.getCurrentSpeed() / WALK_STRIDE_TILES;
        return Math.max(WALK_GAIT_MIN, Math.min(WALK_GAIT_MAX, gait));
    }

    private float clipDuration(EntityAnimationCatalog.ClipSpec clip) {
        String key = clip.path() + "|" + clip.clip();
        Float cached = clipSeconds.get(key);
        if (cached != null) {
            return cached;
        }
        float duration = 3f;
        try {
            duration = assets.pamPlayer().clipDurationSeconds(clip.path(), clip.clip());
        } catch (RuntimeException e) {
            duration = 3f;
        }
        if (duration <= 0f) {
            duration = 3f;
        }
        clipSeconds.put(key, duration);
        return duration;
    }

    private static boolean isLocomotionClip(String clip) {
        return "walk".equals(clip)
                || "walk_newspaper".equals(clip)
                || "run".equals(clip)
                || "spin_walk".equals(clip);
    }

    private Image spawnIceTrap(Zombie zombie) {
        Image ice = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.ICE_TRAP)));
        ice.setTouchable(Touchable.disabled);
        ice.setScaling(Scaling.stretch);
        layer.addActor(ice);
        return ice;
    }

    private void updateIceTrap(Zombie zombie, Image ice) {
        float worldX = layout.worldX(displayX(zombie));
        float worldY = layout.worldYForRow(zombie.getRow());
        float width = layout.tileWidth() * ICE_TRAP_WIDTH_TILES;
        float height = width * ICE_TRAP_ASPECT;
        ice.setSize(width, height);
        ice.setPosition(worldX - width / 2f, worldY + layout.tileHeight() * LawnLayout.ICE_TRAP_Y_OFFSET_TILES);
        ice.setUserObject(zombie.getRow() * 8 + ICE_TRAP_SORT_OFFSET);
        ice.setVisible(!zombie.isSubmerged());
    }

    private void retainFrozenPoses(List<Zombie> live) {
        frozenPoses.keySet().retainAll(live);
    }

    private void beginDeath(PamActor actor) {
        Zombie zombie = actorZombies.remove(actor);
        if (zombie != null && zombie.isSwallowed()) {
            aliases.remove(actor);
            actor.remove();
            return;
        }
        actor.flashHit();
        String alias = aliases.remove(actor);
        bossLogical.remove(actor);
        abilityLatch.remove(actor);
        if (zombie != null && zombie.shouldPowderOnDeath()) {
            beginPowderDeath(actor, zombie);
            return;
        }
        if (alias == null || !clips.hasDie(alias)) {
            actor.remove();
            return;
        }
        EntityAnimationCatalog.ClipSpec die = clips.die(alias);
        EntityAnimationCatalog.ClipSpec parts = clips.particles(alias);
        spawnParticles(actor, parts);
        deathActors.add(actor);
        if (zombie != null && zombie.shouldHideGargantuarImpAmmo()
                && zombie.getType() != null && zombie.getType().contains("Gargantuar")) {
            actor.setVisibility(hideGargantuarImpAmmo(zombie, null, die));
        } else {
            actor.setVisibility(null);
        }
        actor.setTimeScale(1f);
        float scale = bossActors.remove(actor) ? LawnLayout.ZOMBOSS_SCALE : LawnLayout.ZOMBIE_SCALE;
        actor.playOnce(die.path(), die.clip(), scale,
                () -> actor.addAction(ActorFades.holdThenFade(() -> deathActors.remove(actor))));
    }

    private void beginPowderDeath(PamActor actor, Zombie zombie) {
        deathActors.add(actor);
        actor.setVisibility(null);
        actor.setTimeScale(1f);
        float scale = bossActors.remove(actor) ? LawnLayout.ZOMBOSS_SCALE : LawnLayout.ZOMBIE_SCALE;
        String path = ZombieDeathLooks.ashPath(zombie);
        actor.playOnce(path, ZombieDeathLooks.CLIP, scale, () -> {
            actor.remove();
            deathActors.remove(actor);
        });
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
        try {
            assets.pamPlayer().loadSync(parts.path());
        } catch (RuntimeException e) {
            return List.of();
        }
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
        try {
            collectDebrisParts(assets.pamPlayer().getParts(pamPath), found);
        } catch (RuntimeException e) {
            return List.of();
        }
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
        if (zombie.wasThrownByGargantuar() && zombie.isInFlightArc()) {
            return zombie.arcDisplayX(tickFraction);
        }
        double modelX = zombie.getX();
        if (zombie.isAbilityHeld()) {
            return lerp(zombie.getPreviousX(), modelX, tickFraction);
        }
        if (zombie.isDragLocked()) {
            return modelX + zombie.getDragStep() * tickFraction;
        }
        if (zombie.getFreezeTicksRemaining() > 0
                || tickFraction <= 0f
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
        double row = zombie.getRow();
        if (zombie.wasThrownByGargantuar() && zombie.isInFlightArc()) {
            return row - zombie.arcLift(tickFraction);
        }
        if (session != null && session.isIZombieActive()) {
            return row;
        }
        return row - zombie.flightLift();
    }

    private static boolean shouldFlip(Zombie zombie) {
        if (zombie.isBoss()) {
            return false;
        }
        if ("ZombieExplorer".equals(zombie.getType())) {
            return zombie.isHypnotized();
        }
        return zombie.isMovingRight() || zombie.isHypnotized();
    }

    private static void applySnorkelLook(Zombie zombie, PamActor actor, float appear) {
        float alpha = appear;
        if (zombie.isSubmerged()) {
            alpha *= 0.45f;
        }
        actor.getColor().a = alpha;
    }

    private static Map<String, Boolean> hideRaStaffSun(Zombie zombie, Map<String, Boolean> vis) {
        if (zombie == null || !"ZombieRa".equals(zombie.getType()) || !zombie.isStaffSunConcealed()) {
            return vis;
        }
        Map<String, Boolean> hidden = vis == null ? new java.util.HashMap<>() : new java.util.HashMap<>(vis);
        hidden.put("zombie_egypt_ra_88x88", Boolean.FALSE);
        hidden.put("zombie_egypt_ra_95x89", Boolean.FALSE);
        hidden.put("zombie_egypt_ra_100x96", Boolean.FALSE);
        hidden.put("zombie_egypt_ra_100x96_2", Boolean.FALSE);
        return hidden;
    }

    private static Map<String, Boolean> hideExplorerTorch(Zombie zombie, Map<String, Boolean> vis) {
        if (zombie == null || !"ZombieExplorer".equals(zombie.getType()) || zombie.isTorchLit()) {
            return vis;
        }
        Map<String, Boolean> hidden = vis == null ? new java.util.HashMap<>() : new java.util.HashMap<>(vis);
        hidden.put("torch_stick", Boolean.FALSE);
        hidden.put("torch_end_lit", Boolean.FALSE);
        hidden.put("torch_fire_frame_01", Boolean.FALSE);
        hidden.put("torch_fire_frame_02", Boolean.FALSE);
        hidden.put("torch_fire_frame_03", Boolean.FALSE);
        hidden.put("torch_fire_frame_04", Boolean.FALSE);
        hidden.put("torch_fire_fire_frame_01", Boolean.FALSE);
        return hidden;
    }

    private Map<String, Boolean> hideGargantuarImpAmmo(
            Zombie zombie, Map<String, Boolean> vis, EntityAnimationCatalog.ClipSpec clip) {
        if (zombie == null || !zombie.shouldHideGargantuarImpAmmo()
                || zombie.getType() == null || !zombie.getType().contains("Gargantuar")) {
            return vis;
        }
        Map<String, Boolean> hidden = vis == null ? new HashMap<>() : new HashMap<>(vis);
        for (String part : GargantuarImpAmmoParts.HIDDEN_AFTER_THROW) {
            hidden.put(part, Boolean.FALSE);
        }
        if (clip != null) {
            for (String part : ArmorPartVisibility.partNames(assets.pamPlayer(), clip.path())) {
                if (isGargantuarImpAmmoPart(part)) {
                    hidden.put(part, Boolean.FALSE);
                }
            }
        }
        return hidden;
    }

    private static boolean isGargantuarImpAmmoPart(String part) {
        if (part == null) {
            return false;
        }
        return part.startsWith("zombie_imp_")
                || part.startsWith("Zombie_imp_")
                || "Zombie_gargantuar_whiterope".equals(part);
    }

    private static double lerp(double from, double to, float fraction) {
        return from + (to - from) * Math.max(0f, Math.min(1f, fraction));
    }
}
