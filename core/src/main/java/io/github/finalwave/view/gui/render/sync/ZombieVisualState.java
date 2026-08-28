package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.ArmorPartVisibility;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Map;


public final class ZombieVisualState {
    private static final Color CHILL = new Color(0.45f, 0.92f, 1f, 1f);
    private static final Color BUTTER = new Color(1f, 0.92f, 0.45f, 1f);
    private static final Color HYPNOTIZED = new Color(0.82f, 0.55f, 1f, 1f);
    private static final Color DANGER = new Color(1f, 0.55f, 0.55f, 1f);
    private static final Color DANGER_BLEND = new Color(Color.WHITE);
    private static final double NEAR_END_TILES = 2;

    private ZombieVisualState() {
    }

    public static boolean shouldDraw(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) {
            return false;
        }
        ZombieState state = zombie.getState();
        return state != ZombieState.SPAWNING && state != ZombieState.DYING;
    }

    public static EntityAnimationCatalog.ClipSpec clip(Zombie zombie, ZombieClips clips) {
        String type = zombie.getType();
        if (zombie.isBoss()) {
            return clips.boss(type, zombie.getPresentationClip());
        }
        String presented = zombie.getPresentationClip();
        if (zombie.isAbilityHeld() || zombie.getState() == ZombieState.ABILITY
                || ZombieClips.isSpecial(presented)) {
            return clips.ability(type, presented);
        }
        boolean newspaper = hasNewspaper(zombie);
        if (zombie.getState() == ZombieState.EATING) {
            return newspaper ? clips.eatNewspaper(type) : clips.eat(type);
        }
        if (isAllStarCharge(zombie)) {
            return clips.named(type, "run", "walk", "idle");
        }
        if (zombie.getState() == ZombieState.MOVING && !zombie.isStationary() && !isNearlyStopped(zombie)) {
            return newspaper ? clips.walkNewspaper(type) : clips.walk(type);
        }
        return clips.idle(type);
    }

    public static String followClip(Zombie zombie, ZombieClips clips) {
        boolean newspaper = hasNewspaper(zombie);
        if (zombie.getState() == ZombieState.EATING) {
            return newspaper ? clips.eatNewspaper(zombie.getType()).clip() : clips.eat(zombie.getType()).clip();
        }
        if (isAllStarCharge(zombie)) {
            return clips.named(zombie.getType(), "run", "walk").clip();
        }
        return newspaper ? clips.walkNewspaper(zombie.getType()).clip() : clips.walk(zombie.getType()).clip();
    }

    private static boolean isAllStarCharge(Zombie zombie) {
        return "ZombieModernAllStar".equals(zombie.getType())
                && zombie.getCurrentSpeed() > zombie.getBaseSpeed() * 1.5
                && zombie.getState() != ZombieState.EATING;
    }

    private static boolean hasNewspaper(Zombie zombie) {
        if (!"ZombieNewspaper".equals(zombie.getType())) {
            return false;
        }
        for (Armor armor : zombie.getArmorLayers()) {
            if (armor.isDestroyed()) {
                continue;
            }
            if ("Newspaper".equals(armor.getType()) || "NewspaperDefault".equals(armor.getAlias())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNearlyStopped(Zombie zombie) {
        return zombie.getCurrentSpeed() <= zombie.getBaseSpeed() * 0.05;
    }

    public static Color tint(Zombie zombie) {
        return tint(zombie, null);
    }

    public static Color tint(Zombie zombie, GameSession session) {
        if (zombie.isBoss()) {
            if (zombie.getFreezeTicksRemaining() > 0) {
                return CHILL;
            }
            return Color.WHITE;
        }
        if (zombie.isHypnotized()) {
            return HYPNOTIZED;
        }
        if (zombie.getFreezeTicksRemaining() > 0) {
            return CHILL;
        }
        if (zombie.isStationary()) {
            return Color.WHITE;
        }
        if (zombie.getState() != ZombieState.EATING
                && zombie.getCurrentSpeed() <= zombie.getBaseSpeed() * 0.05) {
            return BUTTER;
        }
        if (zombie.getCurrentSpeed() < zombie.getBaseSpeed() * 0.75) {
            return CHILL;
        }
        Color danger = dangerTint(zombie, session);
        if (danger != null) {
            return danger;
        }
        return Color.WHITE;
    }

    private static Color dangerTint(Zombie zombie, GameSession session) {
        if (zombie == null || !zombie.isAlive() || zombie.isMovingRight() || zombie.isHypnotized()) {
            return null;
        }
        double finishX = 0;
        if (session != null && session.isDeadLineActive()) {
            finishX = session.getDeadLineColumn();
        }
        double x = zombie.getX();
        if (x <= finishX || x > finishX + NEAR_END_TILES) {
            return null;
        }
        float t = (float) (1.0 - (x - finishX) / NEAR_END_TILES);
        t = Math.max(0f, Math.min(1f, t));
        return DANGER_BLEND.set(Color.WHITE).lerp(DANGER, t);
    }

    public static Map<String, Boolean> armorVisibility(Zombie zombie, ZombieClips clips) {
        Map<String, Boolean> vis = null;
        for (Armor armor : zombie.getArmorLayers()) {
            if (armor.isDestroyed()) {
                continue;
            }
            String part = clips.armorLayer(armor);
            if (part == null) {
                continue;
            }
            if (vis == null) {
                vis = new HashMap<>();
            }
            vis.put(part, Boolean.TRUE);
        }
        return vis;
    }

    public static Map<String, Boolean> inkVisibility(Zombie zombie, PamPlayer player, String pamPath) {
        if (zombie == null || player == null || pamPath == null || zombie.getPoisonTicksRemaining() <= 0) {
            return null;
        }
        Map<String, Boolean> vis = null;
        for (String part : ArmorPartVisibility.partNames(player, pamPath)) {
            if (!part.equals("ink") && !part.startsWith("ink_")) {
                continue;
            }
            if (vis == null) {
                vis = new HashMap<>();
            }
            vis.put(part, Boolean.TRUE);
        }
        return vis;
    }

    public static Map<String, Boolean> partVisibility(
            Zombie zombie, ZombieClips clips, PamPlayer player, String pamPath) {
        Map<String, Boolean> vis = armorVisibility(zombie, clips);
        Map<String, Boolean> ink = inkVisibility(zombie, player, pamPath);
        if (ink != null) {
            if (vis == null) {
                vis = new HashMap<>(ink);
            } else {
                vis.putAll(ink);
            }
        }
        return vis;
    }
}
