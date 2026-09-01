package io.github.finalwave.view.gui.assets;

import io.github.finalwave.model.adventure.ChapterId;

public final class AdventureAssetIds {
    public static final String WORLD_EGYPT = "IMAGE_UI_UNIVERSE_WORLDS_EGYPT";
    public static final String WORLD_ICEAGE = "IMAGE_UI_UNIVERSE_WORLDS_ICEAGE";
    public static final String WORLD_BEACH = "IMAGE_UI_UNIVERSE_WORLDS_BEACH";
    public static final String WORLD_DARK = "IMAGE_UI_UNIVERSE_WORLDS_DARK";

    public static final String MAP_BACKGROUND = "IMAGE_MAINMENU_BACKGROUND";

    public static final String EGYPT_ISLAND_1 = "IMAGE_WORLDMAP_EGYPT_ISLAND4";
    public static final String EGYPT_ISLAND_2 = "IMAGE_WORLDMAP_EGYPT_ISLAND5";
    public static final String EGYPT_ISLAND_3 = "IMAGE_WORLDMAP_EGYPT_ISLAND9";
    public static final String EGYPT_ISLAND_BOSS = "IMAGE_WORLDMAP_EGYPT_ISLAND14";

    public static final String ICEAGE_ISLAND_1 = "IMAGE_WORLDMAP_ICEAGE_ISLAND22";
    public static final String ICEAGE_ISLAND_2 = "IMAGE_WORLDMAP_ICEAGE_ISLAND23";
    public static final String ICEAGE_ISLAND_3 = "IMAGE_WORLDMAP_ICEAGE_ISLAND24";
    public static final String ICEAGE_ISLAND_4 = "IMAGE_WORLDMAP_ICEAGE_ISLAND41";
    public static final String ICEAGE_ISLAND_BOSS = "IMAGE_WORLDMAP_ICEAGE_ISLAND1";

    public static final String BEACH_ISLAND_1 = "IMAGE_WORLDMAP_BEACH_ISLAND16";
    public static final String BEACH_ISLAND_2 = "IMAGE_WORLDMAP_BEACH_ISLAND22";
    public static final String BEACH_ISLAND_3 = "IMAGE_WORLDMAP_BEACH_ISLAND24";
    public static final String BEACH_ISLAND_BOSS = "IMAGE_WORLDMAP_BEACH_ISLAND1";

    public static final String DARK_ISLAND_1 = "IMAGE_WORLDMAP_DARK_ISLAND6";
    public static final String DARK_ISLAND_2 = "IMAGE_WORLDMAP_DARK_ISLAND7";
    public static final String DARK_ISLAND_3 = "IMAGE_WORLDMAP_DARK_ISLAND8";
    public static final String DARK_ISLAND_BOSS = "IMAGE_WORLDMAP_DARK_ISLAND7";

    public static final String LOCK = "IMAGE_UI_CARDS_LOCK_MEDIUM";
    public static final String ARROW_LEFT = "IMAGE_UI_GENERIC_ARROW_LEFT_GREEN";
    public static final String ARROW_RIGHT = "IMAGE_UI_GENERIC_ARROW_RIGHT_GREEN";

    public static final String CONNECTOR_FILL = "IMAGE_UI_QUESTS_LEVEL_NODE_CONNECTOR_FILL";
    public static final String CONNECTOR_EMPTY = "IMAGE_UI_QUESTS_LEVEL_NODE_CONNECTOR_EMPTY";

    public static final String NODE_ATLAS = "IMAGE_WORLDMAP_LEVEL_NODE_LEVEL_NODE_130X128";

    public static final String PAM_LEVEL_NODE = "768/INITIAL/WORLDMAP/LEVEL_NODE/LEVEL_NODE.PAM";

    public static final String[] ALL = {
            WORLD_EGYPT,
            WORLD_ICEAGE,
            WORLD_BEACH,
            WORLD_DARK,
            MAP_BACKGROUND,
            EGYPT_ISLAND_1,
            EGYPT_ISLAND_2,
            EGYPT_ISLAND_3,
            EGYPT_ISLAND_BOSS,
            ICEAGE_ISLAND_1,
            ICEAGE_ISLAND_2,
            ICEAGE_ISLAND_3,
            ICEAGE_ISLAND_4,
            ICEAGE_ISLAND_BOSS,
            BEACH_ISLAND_1,
            BEACH_ISLAND_2,
            BEACH_ISLAND_3,
            BEACH_ISLAND_BOSS,
            DARK_ISLAND_1,
            DARK_ISLAND_2,
            DARK_ISLAND_3,
            DARK_ISLAND_BOSS,
            LOCK,
            ARROW_LEFT,
            ARROW_RIGHT,
            CONNECTOR_FILL,
            CONNECTOR_EMPTY,
            NODE_ATLAS
    };

    private AdventureAssetIds() {
    }

    public static String worldIcon(ChapterId id) {
        return switch (id) {
            case ANCIENT_EGYPT -> WORLD_EGYPT;
            case FROSTBITE_CAVES -> WORLD_ICEAGE;
            case BIG_WAVE_BEACH -> WORLD_BEACH;
            case DARK_AGES -> WORLD_DARK;
        };
    }

    public static String chapterBackground(ChapterId id) {
        return MAP_BACKGROUND;
    }

    public static String levelIsland(ChapterId id, int slot, boolean boss) {
        if (boss) {
            return switch (id) {
                case ANCIENT_EGYPT -> EGYPT_ISLAND_BOSS;
                case FROSTBITE_CAVES -> ICEAGE_ISLAND_BOSS;
                case BIG_WAVE_BEACH -> BEACH_ISLAND_BOSS;
                case DARK_AGES -> DARK_ISLAND_BOSS;
            };
        }
        int index = Math.max(0, slot);
        return switch (id) {
            case ANCIENT_EGYPT -> egyptIslands()[index % 3];
            case FROSTBITE_CAVES -> iceageIslands()[index % iceageIslands().length];
            case BIG_WAVE_BEACH -> beachIslands()[index % 3];
            case DARK_AGES -> darkIslands()[index % 3];
        };
    }

    private static String[] egyptIslands() {
        return new String[] {EGYPT_ISLAND_1, EGYPT_ISLAND_2, EGYPT_ISLAND_3};
    }

    private static String[] iceageIslands() {
        return new String[] {ICEAGE_ISLAND_1, ICEAGE_ISLAND_2, ICEAGE_ISLAND_3, ICEAGE_ISLAND_4};
    }

    private static String[] beachIslands() {
        return new String[] {BEACH_ISLAND_1, BEACH_ISLAND_2, BEACH_ISLAND_3};
    }

    private static String[] darkIslands() {
        return new String[] {DARK_ISLAND_1, DARK_ISLAND_2, DARK_ISLAND_3};
    }

    public record NodePlayback(String pam, String clip, boolean loop, String thenClip) {
    }

    public static NodePlayback levelNodePlayback(boolean completed,
                                                 boolean unlocked,
                                                 boolean playUnlock,
                                                 boolean playComplete) {
        if (playComplete) {
            return new NodePlayback(PAM_LEVEL_NODE, "unlocked_animation", false, "finished");
        }
        if (completed) {
            return new NodePlayback(PAM_LEVEL_NODE, "finished", true, null);
        }
        if (playUnlock) {
            return new NodePlayback(PAM_LEVEL_NODE, "locked_animation", false, "unlocked");
        }
        if (unlocked) {
            return new NodePlayback(PAM_LEVEL_NODE, "unlocked", true, null);
        }
        return new NodePlayback(PAM_LEVEL_NODE, "locked_idle", true, null);
    }

    public static String[] nodePams() {
        return new String[] {PAM_LEVEL_NODE};
    }
}
