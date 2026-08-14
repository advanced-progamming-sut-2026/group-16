package io.github.finalwave.view.gui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public final class ZombieAnimationCatalog {
    public record ClipSpec(String path, String clip) {
    }

    public static final ClipSpec TUTORIAL = new ClipSpec(
            "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM",
            "idle");

    private static final String TAG = "ZombieAnimationCatalog";
    private static final String[] SEASONAL = {
            "HALLOWEEN", "EASTER", "FEASTIVUS", "HOLIDAY", "LNY", "VALENTINE",
            "FOODFIGHT", "STPATRICK", "BIRTHDAY", "SPORTZBALL", "NUTCRACKER"
    };

    private final Map<String, ClipSpec> idleByKey = new HashMap<>();

    public ZombieAnimationCatalog(FileHandle assetsRoot) {
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
            if (name.isBlank() || path.isBlank() || !path.contains("/ZOMBIE/")) {
                continue;
            }
            String clip = firstClip(animation.get("clips"), "idle");
            ClipSpec spec = new ClipSpec(path, clip);
            String key = normalize(name);
            ClipSpec existing = idleByKey.get(key);
            if (existing == null || isPreferred(path, name, existing.path(), existingKeyName(existing))) {
                idleByKey.put(key, spec);
            }
        }
    }

    public ClipSpec idleFor(String alias) {
        if (alias == null || alias.isBlank()) {
            return TUTORIAL;
        }
        String key = normalize(alias);
        ClipSpec exact = idleByKey.get(key);
        if (exact != null) {
            return exact;
        }
        String rest = key.startsWith("ZOMBIE") ? key.substring("ZOMBIE".length()) : key;
        if (rest.equals("DEFAULT") || rest.equals("TUTORIAL")) {
            ClipSpec tutorial = idleByKey.get("ZOMBIETUTORIAL");
            return tutorial == null ? TUTORIAL : tutorial;
        }
        ClipSpec prefixed = idleByKey.get("ZOMBIE" + rest);
        if (prefixed != null) {
            return prefixed;
        }
        ClipSpec scored = bestMatch(rest);
        return scored == null ? TUTORIAL : scored;
    }

    public PlantAnimationCatalog.ClipSpec plantClip(String alias) {
        ClipSpec spec = idleFor(alias);
        return new PlantAnimationCatalog.ClipSpec(spec.path(), spec.clip());
    }

    private ClipSpec bestMatch(String rest) {
        if (rest.isBlank()) {
            return null;
        }
        ClipSpec best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Map.Entry<String, ClipSpec> entry : idleByKey.entrySet()) {
            String name = entry.getKey();
            if (isSeasonal(name)) {
                continue;
            }
            if (!name.contains(rest) && !rest.contains(stripZombie(name))) {
                continue;
            }
            int score = 0;
            if (name.contains(rest)) {
                score += 40 + rest.length();
            }
            if (rest.contains(stripZombie(name))) {
                score += 20;
            }
            if (entry.getValue().path().contains("/INITIAL/")) {
                score += 10;
            }
            score -= name.length();
            if (score > bestScore) {
                bestScore = score;
                best = entry.getValue();
            }
        }
        return best;
    }

    private static String existingKeyName(ClipSpec spec) {
        return spec.path();
    }

    private static boolean isPreferred(String candidatePath, String candidateName, String currentPath, String currentName) {
        boolean candidateSeasonal = isSeasonal(candidateName) || isSeasonal(candidatePath);
        boolean currentSeasonal = isSeasonal(currentName) || isSeasonal(currentPath);
        if (candidateSeasonal != currentSeasonal) {
            return !candidateSeasonal;
        }
        boolean candidateInitial = candidatePath.contains("/INITIAL/");
        boolean currentInitial = currentPath.contains("/INITIAL/");
        if (candidateInitial != currentInitial) {
            return candidateInitial;
        }
        return candidatePath.length() < currentPath.length();
    }

    private static boolean isSeasonal(String value) {
        if (value == null) {
            return false;
        }
        String upper = value.toUpperCase(Locale.ROOT);
        for (String token : SEASONAL) {
            if (upper.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String stripZombie(String key) {
        return key.startsWith("ZOMBIE") ? key.substring("ZOMBIE".length()) : key;
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

    private static String normalize(String name) {
        return name.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }
}
