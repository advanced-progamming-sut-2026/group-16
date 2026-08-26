package io.github.finalwave.view.gui.render.clip;

import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public final class ZombotanyLooks {
    private static final Map<String, List<String>> HEAD_PARTS = new HashMap<>();

    private ZombotanyLooks() {
    }

    public static String plantFor(String alias) {
        if (alias == null) {
            return null;
        }
        return switch (alias) {
            case "ZombiePeaShooter" -> "Peashooter";
            case "ZombieWallNut" -> "Wall-nut";
            case "ZombieJalapeno" -> "Jalapeno";
            case "ZombieSquash" -> "Squash";
            default -> null;
        };
    }

    public static Map<String, Boolean> withHeadHidden(PamPlayer player, String pamPath, Map<String, Boolean> vis) {
        Map<String, Boolean> merged = vis == null ? new HashMap<>() : new HashMap<>(vis);
        for (String name : headParts(player, pamPath)) {
            merged.put(name, Boolean.FALSE);
        }
        return merged;
    }

    public static String attachPart(PamPlayer player, String pamPath) {
        String skull = null;
        String jaw = null;
        String particle = null;
        String other = null;
        for (String name : headParts(player, pamPath)) {
            String n = name.toLowerCase(Locale.ROOT);
            if (n.contains("skull") && !n.contains("particle")) {
                skull = name;
            } else if (n.contains("jaw")) {
                jaw = name;
            } else if (n.contains("particle") && n.contains("head")) {
                particle = name;
            } else {
                other = name;
            }
        }
        if (skull != null) {
            return skull;
        }
        if (jaw != null) {
            return jaw;
        }
        if (particle != null) {
            return particle;
        }
        return other;
    }

    public static void clear() {
        HEAD_PARTS.clear();
    }

    public static Map<String, Boolean> overlayVisibility(PamPlayer player, String plantName, String pamPath) {
        if (!"Peashooter".equals(plantName)) {
            return null;
        }
        Map<String, Boolean> vis = new HashMap<>();
        vis.put("peashooter_leaf", Boolean.FALSE);
        vis.put("peashooter_stem", Boolean.FALSE);
        vis.put("peashooter_collar", Boolean.FALSE);
        if (player != null && pamPath != null) {
            for (String name : ArmorPartVisibility.partNames(player, pamPath)) {
                if (hideOnPeashooterOverlay(name)) {
                    vis.put(name, Boolean.FALSE);
                }
            }
        }
        return vis;
    }

    public static float overlayDrop(String plantName, float tileHeight) {
        float drop = tileHeight * 0.2f;
        if ("Peashooter".equals(plantName)) {
            drop += tileHeight * 0.16f;
        }
        return drop;
    }

    public static float overlayAnchorY() {
        return 0.5f;
    }

    public static float overlayScale(String plantName) {
        if ("Wall-nut".equals(plantName)) {
            return 0.86f;
        }
        if ("Squash".equals(plantName)) {
            return 0.8f;
        }
        if ("Jalapeno".equals(plantName)) {
            return 0.78f;
        }
        return 0.76f;
    }

    public static List<String> overlayPlants() {
        return List.of("Peashooter", "Wall-nut", "Jalapeno", "Squash");
    }

    private static List<String> headParts(PamPlayer player, String pamPath) {
        if (player == null || pamPath == null) {
            return List.of();
        }
        List<String> cached = HEAD_PARTS.get(pamPath);
        if (cached != null) {
            return cached;
        }
        Set<String> names = ArmorPartVisibility.partNames(player, pamPath);
        if (names.isEmpty()) {
            return List.of();
        }
        List<String> found = new ArrayList<>();
        for (String name : names) {
            if (isHeadPart(name)) {
                found.add(name);
            }
        }
        List<String> frozen = List.copyOf(found);
        HEAD_PARTS.put(pamPath, frozen);
        return frozen;
    }

    private static boolean isHeadPart(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("skull")
                || n.contains("jaw")
                || n.contains("head")
                || n.contains("face")
                || n.contains("mouth")
                || n.contains("hair")
                || n.contains("goatee")
                || n.contains("beard");
    }

    private static boolean hideOnPeashooterOverlay(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("head")) {
            return false;
        }
        return n.equals("peashooter_leaf")
                || n.startsWith("peashooter_leaf")
                || n.equals("peashooter_stem")
                || n.startsWith("peashooter_stem")
                || n.equals("peashooter_collar");
    }
}
