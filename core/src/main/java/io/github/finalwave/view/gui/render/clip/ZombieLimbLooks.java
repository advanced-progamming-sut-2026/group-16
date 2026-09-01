package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.zombie.Zombie;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;



public final class ZombieLimbLooks {
    public static final float ARM_DROP_HEALTH_RATIO = 0.2f;

    private static final Set<String> MID_FIGHT_ARM_DROP_TYPES = Set.of(
            "ZombieDefault",
            "ZombieArmor1",
            "ZombieArmor2",
            "ZombieArmor4",
            "ZombieNewspaper",
            "ZombieDarkJuggler",
            "ZombieModernAllStar"
    );

    private ZombieLimbLooks() {
    }

    public static boolean losesArmMidFight(String type) {
        return type != null && MID_FIGHT_ARM_DROP_TYPES.contains(type);
    }

    public static boolean losesArmMidFight(Zombie zombie) {
        return zombie != null && losesArmMidFight(zombie.getType());
    }

    public static boolean shouldDropArm(Zombie zombie) {
        if (!losesArmMidFight(zombie) || !zombie.isAlive() || zombie.isBoss()) {
            return false;
        }
        int max = zombie.getMaxHealth();
        if (max <= 0) {
            return false;
        }
        return zombie.getHealth() / (float) max <= ARM_DROP_HEALTH_RATIO;
    }

    public static Map<String, Boolean> withArmDropped(
            PamPlayer player, String pamPath, Map<String, Boolean> vis) {
        if (player == null || pamPath == null) {
            return vis;
        }
        Set<String> names = ArmorPartVisibility.partNames(player, pamPath);
        if (names.isEmpty()) {
            return vis;
        }
        Map<String, Boolean> merged = vis == null ? new HashMap<>() : new HashMap<>(vis);
        for (String name : names) {
            if (isHiddenWhenArmDropped(name)) {
                merged.put(name, Boolean.FALSE);
            }
        }
        for (String name : names) {
            if (isArmsOuterUpper(name)) {
                merged.put(name, Boolean.TRUE);
            } else if (isArmOuterUpperBone(name)) {
                merged.put(name, Boolean.TRUE);
            }
        }
        return merged;
    }

    public static Map<String, Boolean> withArmIntact(
            PamPlayer player, String pamPath, Map<String, Boolean> vis) {
        if (player == null || pamPath == null) {
            return vis;
        }
        Set<String> names = ArmorPartVisibility.partNames(player, pamPath);
        if (names.isEmpty()) {
            return vis;
        }
        Map<String, Boolean> merged = null;
        for (String name : names) {
            if (isArmsOuterUpper(name)) {
                if (merged == null) {
                    merged = vis == null ? new HashMap<>() : new HashMap<>(vis);
                }
                merged.put(name, Boolean.TRUE);
            } else if (isArmOuterUpperBone(name)) {
                if (merged == null) {
                    merged = vis == null ? new HashMap<>() : new HashMap<>(vis);
                }
                merged.put(name, Boolean.FALSE);
            }
        }
        return merged == null ? vis : merged;
    }

    public static Map<String, Boolean> withHeadDropped(
            PamPlayer player, String pamPath, Map<String, Boolean> vis) {
        if (player == null || pamPath == null) {
            return vis;
        }
        Set<String> names = ArmorPartVisibility.partNames(player, pamPath);
        if (names.isEmpty()) {
            return vis;
        }
        Map<String, Boolean> merged = vis == null ? new HashMap<>() : new HashMap<>(vis);
        for (String name : names) {
            if (isHeadPart(name)) {
                merged.put(name, Boolean.FALSE);
            }
        }
        return merged;
    }

    static String boneArmPart(Set<String> names) {
        String arms = null;
        String bone = null;
        for (String name : names) {
            if (isArmsOuterUpper(name)) {
                arms = name;
            } else if (isArmOuterUpperBone(name)) {
                bone = name;
            }
        }
        return arms != null ? arms : bone;
    }

    static boolean isBoneArmPart(String name) {
        return isArmsOuterUpper(name) || isArmOuterUpperBone(name);
    }

    private static boolean isArmsOuterUpper(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("arms_outer_upper");
    }

    private static boolean isArmOuterUpperBone(String name) {
        if (name == null) {
            return false;
        }
        return name.toLowerCase(Locale.ROOT).contains("arm_outer_upper_bone");
    }

    static boolean isHiddenWhenArmDropped(String name) {
        if (name == null || name.isBlank() || isBoneArmPart(name)) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("inner")) {
            return false;
        }
        return n.contains("hand_outer")
                || n.contains("outerarm_hand")
                || n.contains("outer_hand")
                || (n.contains("hand") && n.contains("outer"));
    }

    static boolean isHeadPart(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("particle")) {
            return false;
        }
        return n.contains("skull")
                || n.contains("jaw")
                || n.contains("head")
                || n.contains("face")
                || n.contains("mouth")
                || n.contains("hair")
                || n.contains("goatee")
                || n.contains("beard");
    }
}
