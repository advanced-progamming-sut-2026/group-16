package io.github.finalwave.model.collection;

import java.util.Locale;

public final class CollectionNames {
    private CollectionNames() {
    }

    public static String zombie(String alias) {
        if (alias == null || alias.isBlank()) {
            return "";
        }
        String stripped = alias.startsWith("Zombie") ? alias.substring("Zombie".length()) : alias;
        if (stripped.isBlank()) {
            stripped = alias;
        }
        if ("Default".equalsIgnoreCase(stripped) || "Tutorial".equalsIgnoreCase(stripped)) {
            return "Basic Zombie";
        }
        String spaced = stripped
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Za-z])([0-9])", "$1 $2")
                .trim();
        return spaced.isBlank() ? alias : spaced;
    }

    public static String statLabel(String raw, String prefix, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String value = raw.trim();
        if (prefix != null && value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
            value = value.substring(prefix.length()).trim();
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "1" -> prefix != null && prefix.equalsIgnoreCase("speed") ? "Stiff" : "Average";
            case "2" -> prefix != null && prefix.equalsIgnoreCase("speed") ? "Basic" : "Protected";
            case "3" -> prefix != null && prefix.equalsIgnoreCase("speed") ? "Hungry" : "Hardened";
            case "4" -> prefix != null && prefix.equalsIgnoreCase("speed") ? "Fast" : "Dense";
            case "5" -> "Macho";
            default -> value.isBlank() ? fallback : capitalize(value);
        };
    }

    public static String formatSpeed(double speed) {
        return String.format(Locale.US, "%.3f", speed);
    }

    private static String capitalize(String value) {
        if (value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
