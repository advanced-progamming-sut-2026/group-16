package io.github.finalwave.model.minigame;

public enum MiniGameId {
    VASE_BREAKER("vase-breaker", "Vasebreaker"),
    WALNUT_BOWLING("walnut-bowling", "Wallnut Bowling"),
    I_ZOMBIE("i-zombie", "I, Zombie"),
    BEGHOULED("beghouled", "Beghouled"),
    ZOMBOTANY("zombotany", "Zombotany");

    private final String key;
    private final String displayName;

    MiniGameId(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MiniGameId fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = name.trim().toLowerCase().replace('_', '-').replace(' ', '-');
        for (MiniGameId id : values()) {
            if (id.key.equalsIgnoreCase(normalized)
                    || id.displayName.equalsIgnoreCase(name.trim())
                    || id.name().equalsIgnoreCase(name.trim().replace('-', '_').replace(' ', '_'))) {
                return id;
            }
        }
        return null;
    }
}
