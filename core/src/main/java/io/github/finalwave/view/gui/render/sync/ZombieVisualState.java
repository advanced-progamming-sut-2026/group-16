package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.ZombieClips;

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
        boolean newspaper = hasNewspaper(zombie);
        if (zombie.getState() == ZombieState.EATING) {
            return newspaper ? clips.eatNewspaper(type) : clips.eat(type);
        }
        if (zombie.getState() == ZombieState.ABILITY) {
            return clips.ability(type);
        }
        if (zombie.getState() == ZombieState.MOVING && !zombie.isStationary() && !isNearlyStopped(zombie)) {
            return newspaper ? clips.walkNewspaper(type) : clips.walk(type);
        }
        return clips.idle(type);
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
        if (zombie.isHypnotized()) {
            return HYPNOTIZED;
        }
        if (zombie.getFreezeTicksRemaining() > 0) {
            return CHILL;
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
}
