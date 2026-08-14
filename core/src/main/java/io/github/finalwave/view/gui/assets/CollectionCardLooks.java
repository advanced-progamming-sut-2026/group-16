package io.github.finalwave.view.gui.assets;

import io.github.finalwave.model.collection.CollectionPlantEntry;

import java.util.List;
import java.util.Locale;


public final class CollectionCardLooks {
    private CollectionCardLooks() {
    }

    public static String packetBackground(CollectionPlantEntry entry) {
        List<String> tags = entry.tags() == null ? List.of() : entry.tags();
        if (hasTag(tags, "ICE")) {
            return "IMAGE_UI_PACKETS_ICEAGE";
        }
        if (hasTag(tags, "WATER")) {
            return "IMAGE_UI_PACKETS_BEACH";
        }
        if (hasTag(tags, "EXPLOSIVE") || category(entry, "EXPLOSIVE")) {
            return "IMAGE_UI_PACKETS_DINO";
        }
        if (hasTag(tags, "MAGIC")) {
            return "IMAGE_UI_PACKETS_EIGHTIES";
        }
        if (hasTag(tags, "NIGHT")) {
            return "IMAGE_UI_PACKETS_DARK";
        }
        if (hasTag(tags, "CHARGE")) {
            return "IMAGE_UI_PACKETS_FUTURE";
        }
        if (hasTag(tags, "TRAP")) {
            return "IMAGE_UI_PACKETS_EGYPT";
        }
        if (category(entry, "WALL_NUT")) {
            return "IMAGE_UI_PACKETS_COWBOY";
        }
        if (category(entry, "SHOOTER")) {
            return "IMAGE_UI_PACKETS_LOSTCITY";
        }
        if (category(entry, "SUN_PRODUCER")) {
            return "IMAGE_UI_PACKETS_BOOST";
        }
        return "IMAGE_UI_PACKETS_HOMELESS";
    }

    public static String familyIcon(String category) {
        if (category == null) {
            return "IMAGE_UI_PACKETS_MINTFAM_PEASHOOTER";
        }
        return switch (category.toUpperCase(Locale.ROOT)) {
            case "SUN_PRODUCER" -> "IMAGE_UI_PACKETS_MINTFAM_SUN";
            case "MELEE" -> "IMAGE_UI_PACKETS_MINTFAM_MELEE";
            case "STRIKE_THROUGH" -> "IMAGE_UI_PACKETS_MINTFAM_ELECTRICITY";
            case "HOMING" -> "IMAGE_UI_PACKETS_MINTFAM_SHADOW";
            case "LOBBER" -> "IMAGE_UI_PACKETS_MINTFAM_LOBBER";
            case "SHOOTER" -> "IMAGE_UI_PACKETS_MINTFAM_PEASHOOTER";
            case "MODIFIER" -> "IMAGE_UI_PACKETS_MINTFAM_MAGIC";
            case "WALL_NUT" -> "IMAGE_UI_PACKETS_MINTFAM_DEFENSE";
            case "EXPLOSIVE" -> "IMAGE_UI_PACKETS_MINTFAM_EXPLOSIVE";
            default -> "IMAGE_UI_PACKETS_MINTFAM_PEASHOOTER";
        };
    }

    public static String words(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] parts = raw.replace('_', ' ').toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    public static String plantAbilityLine(String name, String category, String abilityType) {
        String ability = words(abilityType);
        String family = words(category);
        if (ability.isBlank()) {
            return family;
        }
        if (family.isBlank()) {
            return ability + ".";
        }
        return (name == null ? family : name) + "s use " + ability.toLowerCase(Locale.ROOT) + ".";
    }

    public static String plantFoodLine(String plantFoodType) {
        if (plantFoodType == null || plantFoodType.isBlank() || "NONE".equalsIgnoreCase(plantFoodType)) {
            return null;
        }
        return "Plant Food: " + words(plantFoodType) + ".";
    }

    public static String rangeLabel(String category) {
        if (category == null) {
            return "NONE";
        }
        return switch (category.toUpperCase(Locale.ROOT)) {
            case "LOBBER" -> "LOBBED";
            case "SHOOTER" -> "SHOT";
            case "MELEE" -> "CLOSE";
            case "STRIKE_THROUGH" -> "PIERCING";
            default -> "NONE";
        };
    }

    public static String zombiePacketSuffix(String alias) {
        if (alias == null) {
            return "TUTORIAL";
        }
        return switch (alias) {
            case "ZombieDefault" -> "TUTORIAL";
            case "ZombieArmor1" -> "MUMMY_ARMOR1";
            case "ZombieArmor2" -> "MUMMY_ARMOR2";
            case "ZombieArmor4" -> "CARNIE_ARMOR4";
            case "ZombieDarkArmor3" -> "DARK_ARMOR3";
            case "ZombieGargantuar" -> "EGYPT_GARGANTUAR";
            case "ZombieImp" -> "DARK_IMP";
            case "ZombieModernAllStar" -> "MODERN_ALLSTAR";
            case "ZombieArcade" -> "EIGHTIES_ARCADE";
            case "ZombieLostCityJane" -> "LOSTCITY_JANE";
            case "ZombieCrystalSkull" -> "LOSTCITY_CRYSTALSKULL";
            case "ZombieProspector" -> "PROSPECTOR";
            case "ZombiePiano" -> "PIANO";
            case "ZombieNewspaper" -> "MODERN_NEWSPAPER";
            case "ZombieRa" -> "RA";
            case "ZombieExplorer" -> "EXPLORER_VETERAN";
            case "ZombieTombRaiser" -> "TOMB_RAISER";
            case "ZombieIceAgeDodo" -> "ICEAGE_DODO";
            case "ZombieIceAgeHunter" -> "ICEAGE_HUNTER";
            case "ZombieIceAgeTroglobite" -> "ICEAGE_TROGLOBITE";
            case "ZombieBeachFisherman" -> "BEACH_FISHERMAN";
            case "ZombieBeachOctopus" -> "BEACH_OCTOPUS";
            case "ZombieBeachSnorkel" -> "BEACH_SNORKEL";
            case "ZombieDarkJuggler" -> "DARK_JUGGLER";
            case "ZombieWizard" -> "DARK_WIZARD";
            case "ZombieDarkKing" -> "DARK_KING";
            case "ZombieDarkImpDragon" -> "DARK_IMP_DRAGON";
            default -> null;
        };
    }

    private static boolean category(CollectionPlantEntry entry, String expected) {
        return entry.category() != null && entry.category().equalsIgnoreCase(expected);
    }

    private static boolean hasTag(List<String> tags, String tag) {
        for (String value : tags) {
            if (tag.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
