package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
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
        if (zombie.getState() == ZombieState.EATING) {
            return clips.eat(zombie.getType());
        }
        return clips.walk(zombie.getType());
    }

    public static Color tint(Zombie zombie) {
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
        return Color.WHITE;
    }

    public static Map<String, Boolean> armorVisibility(Zombie zombie, ZombieClips clips) {
        Map<String, Boolean> vis = null;
        for (Armor armor : zombie.getArmorLayers()) {
            if (armor.isDestroyed()) {
                continue;
            }
            String part = clips.armorPart(armor.getType(), armor.getAlias());
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
