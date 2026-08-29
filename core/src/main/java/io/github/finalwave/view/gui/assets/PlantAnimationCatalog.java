package io.github.finalwave.view.gui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;


public final class PlantAnimationCatalog {
    public record ClipSpec(String path, String clip) {
    }

    public static final ClipSpec SPROUT = new ClipSpec(
            "768/INITIAL/ZEN_GARDEN/PLANT_ANIMATIONS/SPROUT/SPROUT.PAM",
            "idle");
    public static final ClipSpec SPROUT_PLANT = new ClipSpec(
            "768/INITIAL/ZEN_GARDEN/PLANT_ANIMATIONS/SPROUT/SPROUT.PAM",
            "transition");
    public static final ClipSpec GROWING_SLOT = new ClipSpec(
            "768/INITIAL/ZEN_GARDEN/GROWING_PLANT_SLOT/GROWING_PLANT_SLOT.PAM",
            "idle");
    public static final ClipSpec PLANT_POOF = new ClipSpec(
            "768/INITIAL/ZEN_GARDEN/PLANT_POOF/PLANT_POOF.PAM",
            "animation");
    public static final String UPGRADE_BADGE_PAM = "768/INITIAL/UI/LEVELING/UPGRADE_BADGE/UPGRADE_BADGE.PAM";

    private static final String TAG = "PlantAnimationCatalog";

    private final Map<String, ClipSpec> idleByKey = new HashMap<>();

    public PlantAnimationCatalog(FileHandle assetsRoot) {
        FileHandle file = assetsRoot.child("animations.json");
        if (!file.exists()) {
            Gdx.app.error(TAG, "animations.json not found");
            return;
        }
        JsonValue root = new JsonReader().parse(file);
        JsonValue animations = root.get("animations");
        if (animations == null) {
            return;
        }
        for (JsonValue animation : animations) {
            String name = animation.getString("name", "");
            String path = animation.getString("path", "");
            if (name.isBlank() || path.isBlank() || !path.contains("/PLANT/")) {
                continue;
            }
            String clip = firstClip(animation.get("clips"), "idle");
            ClipSpec spec = new ClipSpec(path, clip);
            String key = PlantNameAliases.normalize(name);
            ClipSpec existing = idleByKey.get(key);
            if (existing == null || isPreferred(path, existing.path())) {
                idleByKey.put(key, spec);
            }
        }
    }

    public ClipSpec idleFor(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return SPROUT;
        }
        if ("Grave Buster".equals(plantName)) {
            ClipSpec spec = idleByKey.get(PlantNameAliases.pamKey(plantName));
            if (spec == null) {
                spec = idleByKey.get(PlantNameAliases.normalize(plantName));
            }
            if (spec != null) {
                return new ClipSpec(spec.path(), "attack1");
            }
        }
        ClipSpec spec = idleByKey.get(PlantNameAliases.pamKey(plantName));
        if (spec == null) {
            spec = idleByKey.get(PlantNameAliases.normalize(plantName));
        }
        if (spec == null) {
            return SPROUT;
        }
        if (isMint(plantName)) {
            return new ClipSpec(spec.path(), "loop");
        }
        return spec;
    }

    private static boolean isMint(String plantName) {
        String lower = plantName.toLowerCase();
        return lower.equals("enlighten-mint")
                || lower.equals("appease-mint")
                || lower.equals("arma-mint")
                || lower.equals("bombard-mint")
                || lower.equals("enforce-mint")
                || lower.equals("reinforce-mint")
                || lower.equals("enchant-mint");
    }

    private static boolean isPreferred(String candidate, String current) {
        boolean candidateInitial = candidate.contains("/INITIAL/");
        boolean currentInitial = current.contains("/INITIAL/");
        if (candidateInitial != currentInitial) {
            return candidateInitial;
        }
        return candidate.contains("/PLANT/") && !current.contains("/PLANT/");
    }

    private static String firstClip(JsonValue clips, String preferred) {
        if (clips == null || !clips.isObject()) {
            return preferred;
        }
        if (clips.has(preferred)) {
            return preferred;
        }
        if (clips.child != null) {
            return clips.child.name;
        }
        return preferred;
    }
}
