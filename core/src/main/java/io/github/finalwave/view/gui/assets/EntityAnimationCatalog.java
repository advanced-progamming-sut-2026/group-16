package io.github.finalwave.view.gui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public final class EntityAnimationCatalog {
    public record ClipSpec(String path, String clip) {
    }

    public static final ClipSpec FALLBACK_PLANT = new ClipSpec(
            PlantAnimationCatalog.SPROUT.path(),
            PlantAnimationCatalog.SPROUT.clip());
    public static final String FALLBACK_ZOMBIE =
            "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM";

    private static final String TAG = "EntityAnimationCatalog";
    private static final String DEFAULT_PLANT_CLIP = "idle";
    private static final String DEFAULT_ZOMBIE_CLIP = "walk";

    private static final Map<String, String> PLANT_NAME_OVERRIDES = Map.of(
            normalize("Twin Sunflower"), "SUNFLOWER_TWIN",
            normalize("Rotobaga"), "ROTORUTABAGA",
            normalize("Mega Gatling Pea"), "MEGAGATLING",
            normalize("Phat Beet"), "PHATBEETS",
            normalize("Giant Wall-nut"), "TALLNUT"
    );

    private static final Map<String, String> ZOMBIE_PATHS = zombiePaths();

    private final Map<String, ClipSpec> plantsByKey = new HashMap<>();
    private final Map<String, Map<String, String>> clipsByPath = new HashMap<>();
    private final Set<String> missingPlants = new HashSet<>();
    private final Set<String> missingZombies = new HashSet<>();

    public EntityAnimationCatalog(FileHandle assetsRoot) {
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
            if (name.isBlank() || path.isBlank()) {
                continue;
            }
            Map<String, String> clips = clipNames(animation.get("clips"));
            clipsByPath.put(path, clips);
            if (!path.contains("/PLANT/")) {
                continue;
            }
            String key = normalize(name);
            ClipSpec spec = new ClipSpec(path, preferredClip(clips, DEFAULT_PLANT_CLIP));
            ClipSpec existing = plantsByKey.get(key);
            if (existing == null || isPreferred(path, existing.path())) {
                plantsByKey.put(key, spec);
            }
        }
    }

    public ClipSpec plantIdle(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return FALLBACK_PLANT;
        }
        String key = normalize(plantName);
        String overrideName = PLANT_NAME_OVERRIDES.get(key);
        if (overrideName != null) {
            ClipSpec override = plantsByKey.get(normalize(overrideName));
            if (override != null) {
                return override;
            }
        }
        ClipSpec spec = plantsByKey.get(key);
        if (spec != null) {
            return spec;
        }
        if (missingPlants.add(plantName)) {
            Gdx.app.error(TAG, "No plant PAM for " + plantName);
        }
        return FALLBACK_PLANT;
    }

    public ClipSpec plantClip(String plantName, String... preferredClips) {
        ClipSpec idle = plantIdle(plantName);
        String clip = pick(idle.path(), DEFAULT_PLANT_CLIP, preferredClips);
        return new ClipSpec(idle.path(), clip);
    }

    public ClipSpec zombieClip(String alias, String... preferredClips) {
        String path = zombiePath(alias);
        String clip = pick(path, DEFAULT_ZOMBIE_CLIP, preferredClips);
        return new ClipSpec(path, clip);
    }

    public boolean hasClip(String path, String name) {
        if (path == null || name == null) {
            return false;
        }
        return clipsByPath.getOrDefault(path, Map.of()).containsKey(name);
    }

    public String zombiePath(String alias) {
        if (alias == null || alias.isBlank()) {
            return FALLBACK_ZOMBIE;
        }
        String path = ZOMBIE_PATHS.get(alias);
        if (path != null) {
            return path;
        }
        if (missingZombies.add(alias)) {
            Gdx.app.error(TAG, "Unknown zombie alias " + alias);
        }
        return FALLBACK_ZOMBIE;
    }

    public String armorPart(String armorType) {
        String[] layers = armorLayers(armorType);
        return layers == null || layers.length == 0 ? null : layers[0];
    }

    public String[] armorLayers(String armorType) {
        if (armorType == null) {
            return null;
        }
        return switch (armorType) {
            case "Cone", "ConeDefault" -> new String[]{
                    "zombie_armor_cone_norm",
                    "zombie_armor_cone_damage_01",
                    "zombie_armor_cone_damage_02"};
            case "Bucket", "BucketDefault" -> new String[]{
                    "zombie_armor_bucket_norm",
                    "zombie_armor_bucket_damage_01",
                    "zombie_armor_bucket_damage_02"};
            case "Brick", "BrickDefault" -> new String[]{
                    "zombie_armor_brick_norm",
                    "zombie_armor_brick_damage_01",
                    "zombie_armor_brick_damage_02"};
            case "ShoulderArmor", "ShoulderArmorDefault" -> new String[]{
                    "zombie_shoulder_armor_norm",
                    "zombie_shoulder_armor_damage_01",
                    "zombie_shoulder_armor_damage_02"};
            case "Crown", "CrownDefault" -> new String[]{
                    "zombie_armor_crown_norm",
                    "zombie_armor_crown_damage_01",
                    "zombie_armor_crown_damage_02"};
            case "Newspaper", "NewspaperDefault" -> new String[]{
                    "_zombie_newspaper",
                    "_zombie_newspaper_dmg1",
                    "_zombie_newspaper_dmg2"};
            default -> null;
        };
    }

    private String pick(String path, String lastResort, String... preferred) {
        Map<String, String> clips = clipsByPath.getOrDefault(path, Map.of());
        if (preferred != null) {
            for (String name : preferred) {
                if (name != null && clips.containsKey(name)) {
                    return name;
                }
            }
        }
        if (clips.containsKey(lastResort)) {
            return lastResort;
        }
        if (!clips.isEmpty()) {
            return clips.keySet().iterator().next();
        }
        return lastResort;
    }

    private static Map<String, String> clipNames(JsonValue clips) {
        Map<String, String> names = new LinkedHashMap<>();
        if (clips == null || !clips.isObject()) {
            return names;
        }
        for (JsonValue child = clips.child; child != null; child = child.next) {
            names.put(child.name, child.name);
        }
        return names;
    }

    private static String preferredClip(Map<String, String> clips, String preferred) {
        if (clips.containsKey(preferred)) {
            return preferred;
        }
        if (!clips.isEmpty()) {
            return clips.keySet().iterator().next();
        }
        return preferred;
    }

    private static boolean isPreferred(String candidate, String current) {
        boolean candidateInitial = candidate.contains("/INITIAL/");
        boolean currentInitial = current.contains("/INITIAL/");
        if (candidateInitial != currentInitial) {
            return candidateInitial;
        }
        return candidate.contains("/PLANT/") && !current.contains("/PLANT/");
    }

    private static String normalize(String name) {
        return name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private static Map<String, String> zombiePaths() {
        String egypt = FALLBACK_ZOMBIE;
        String brick = "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC_BRICK/ZOMBIE_DARK_BASIC_BRICK.PAM";
        Map<String, String> paths = new HashMap<>();
        paths.put("ZombieDefault", egypt);
        paths.put("ZombieArmor1", egypt);
        paths.put("ZombieArmor2", egypt);
        paths.put("ZombieArmor4", brick);
        paths.put("ZombieDarkArmor3", brick);
        paths.put("ZombieGargantuar", "768/INITIAL/ZOMBIE/EGYPT_GARGANTUAR/EGYPT_GARGANTUAR.PAM");
        paths.put("ZombieImp", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_IMP/ZOMBIE_EGYPT_IMP.PAM");
        paths.put("ZombieRa", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM");
        paths.put("ZombieExplorer", "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM");
        paths.put("ZombieTombRaiser", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM");
        paths.put("ZombieEgyptZomboss", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM");
        paths.put("ZombieDarkZomboss", "768/FULL/ZOMBIE/ZOMBIE_DARK_ZOMBOSS/ZOMBIE_DARK_ZOMBOSS.PAM");
        paths.put("ZombieIceageZomboss", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_ZOMBOSS/ZOMBIE_ICEAGE_ZOMBOSS.PAM");
        paths.put("ZombieBeachZomboss", "768/FULL/ZOMBIE/ZOMBIE_BEACH_ZOMBOSS/ZOMBIE_BEACH_ZOMBOSS.PAM");
        paths.put("ZombieIceAgeDodo", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM");
        paths.put("ZombieIceAgeHunter", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM");
        paths.put("ZombieIceAgeTroglobite", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_TROGLOBITE/ZOMBIE_ICEAGE_TROGLOBITE.PAM");
        paths.put("ZombieBeachFisherman", "768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM");
        paths.put("ZombieBeachOctopus", "768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM");
        paths.put("ZombieBeachSnorkel", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM");
        paths.put("ZombieDarkJuggler", egypt);
        paths.put("ZombieWizard", "768/FULL/ZOMBIE/ZOMBIE_DARK_WIZARD/ZOMBIE_DARK_WIZARD.PAM");
        paths.put("ZombieDarkKing", "768/FULL/ZOMBIE/ZOMBIE_DARK_KING/ZOMBIE_DARK_KING.PAM");
        paths.put("ZombieDarkImpDragon", "768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_DRAGON/ZOMBIE_DARK_IMP_DRAGON.PAM");
        paths.put("ZombieModernAllStar", "768/FULL/ZOMBIE/ZOMBIE_MODERN_ALLSTAR/ZOMBIE_MODERN_ALLSTAR.PAM");
        paths.put("ZombieLostCityJane", "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_JANE/ZOMBIE_LOSTCITY_JANE.PAM");
        paths.put("ZombieCrystalSkull", "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM");
        paths.put("ZombieProspector", "768/FULL/ZOMBIE/ZOMBIE_PROSPECTOR/ZOMBIE_PROSPECTOR.PAM");
        paths.put("ZombiePiano", "768/FULL/ZOMBIE/ZOMBIE_PIANO/ZOMBIE_PIANO.PAM");
        paths.put("ZombieNewspaper", "768/FULL/ZOMBIE/ZOMBIE_MODERN_NEWSPAPER/ZOMBIE_MODERN_NEWSPAPER.PAM");
        paths.put("ZombieArcade", "768/FULL/ZOMBIE/ZOMBIE_80S_ARCADE/ZOMBIE_80S_ARCADE.PAM");
        return Map.copyOf(paths);
    }
}
