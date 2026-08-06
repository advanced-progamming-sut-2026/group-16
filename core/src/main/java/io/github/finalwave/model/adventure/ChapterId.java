package io.github.finalwave.model.adventure;

public enum ChapterId {
    ANCIENT_EGYPT("ancient-egypt", "Ancient Egypt"),
    FROSTBITE_CAVES("frostbite-caves", "Frostbite Caves"),
    BIG_WAVE_BEACH("big-wave-beach", "Big Wave Beach"),
    DARK_AGES("dark-ages", "Dark Ages");

    private final String key;
    private final String displayName;

    ChapterId(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ChapterId fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = name.trim().toLowerCase().replace('_', '-').replace(' ', '-');
        for (ChapterId id : values()) {
            if (id.key.equalsIgnoreCase(normalized)
                    || id.displayName.equalsIgnoreCase(name.trim())
                    || id.name().equalsIgnoreCase(name.trim().replace('-', '_').replace(' ', '_'))) {
                return id;
            }
        }
        return null;
    }
}
