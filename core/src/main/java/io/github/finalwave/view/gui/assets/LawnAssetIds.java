package io.github.finalwave.view.gui.assets;

import io.github.finalwave.model.adventure.ChapterId;


public final class LawnAssetIds {
    public static final String EGYPT_MAIN = "IMAGE_BACKGROUNDS_EGYPT_TEXTURE";
    public static final String EGYPT_LEFT = "IMAGE_BACKGROUNDS_EGYPT_TEXTURE_LEFT";
    public static final String EGYPT_RIGHT = "IMAGE_BACKGROUNDS_EGYPT_TEXTURE_RIGHT";

    public static final String ICEAGE_MAIN = "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE";
    public static final String ICEAGE_LEFT = "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_LEFT";
    public static final String ICEAGE_RIGHT = "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_RIGHT";

    public static final String BEACH_MAIN = "IMAGE_BACKGROUNDS_BEACH_TEXTURE";
    public static final String BEACH_LEFT = "IMAGE_BACKGROUNDS_BEACH_TEXTURE_LEFT";
    public static final String BEACH_RIGHT = "IMAGE_BACKGROUNDS_BEACH_TEXTURE_RIGHT";

    public static final String DARK_MAIN = "IMAGE_BACKGROUNDS_DARK_TEXTURE";
    public static final String DARK_LEFT = "IMAGE_BACKGROUNDS_DARK_TEXTURE_LEFT";
    public static final String DARK_RIGHT = "IMAGE_BACKGROUNDS_DARK_TEXTURE_RIGHT";

    public static final String MINIGAME_MAIN = "IMAGE_BACKGROUNDS_DINO_TEXTURE";

    public static final String PAUSE = "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON";
    public static final String SPEED_2X = "IMAGE_UI_HUD_INGAME_2X";
    public static final String SPEED_2X_SELECTED = "IMAGE_UI_HUD_INGAME_2X_SELECTED";
    public static final String SHOVEL = "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON";
    public static final String FLOATING_SHOVEL = "IMAGE_UI_HUD_INGAME_SHOVEL_ICON";
    public static final String SUN_ICON = "IMAGE_UI_HUD_INGAME_SUN";
    public static final String SUN_BANNER = "IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE";
    public static final String PLANTFOOD_BANK = "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK";
    public static final String PLANTFOOD_COLLECT = "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_COLLECT";
    public static final String PLANTFOOD_SLOT = "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_FILLED_SLOT";
    public static final String PLANTFOOD_LEAF = "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON";
    public static final String FLOATING_PLANTFOOD = "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON_DOWN";
    public static final String HUD_PLUS = "IMAGE_UI_HUD_INGAME_COIN_BUY";
    public static final String HUD_PLUS_DOWN = "IMAGE_UI_HUD_INGAME_COIN_BUY_DOWN";
    public static final String PROGRESS_METER = "IMAGE_UI_HUD_INGAME_PROGRESS_METER";
    public static final String PROGRESS_FILL = "IMAGE_UI_HUD_INGAME_PROGRESS_METER_FILL";
    public static final String PROGRESS_FLAG = "IMAGE_UI_HUD_INGAME_PROGRESS_METER_FLAG_DEFAULT";
    public static final String PROGRESS_FLAG_POLE = "IMAGE_UI_HUD_INGAME_PROGRESS_METER_FLAG_POLE";
    public static final String PROGRESS_ZOMBIE_HEAD = "IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBIEHEAD";
    public static final String PACKET_BG = "IMAGE_UI_PACKETS_MODERNDAY";
    public static final String PACKET_EMPTY = "IMAGE_UI_PACKETS_EMPTY_PACKET";
    public static final String PACKET_LOCK = "IMAGE_UI_CARDS_LOCK_MEDIUM";
    public static final String PACKET_SELECT = "IMAGE_UI_PACKETS_SELECT";
    public static final String CONVEYOR_BELT = "IMAGE_UI_CONVEYOR_CONVEYOR_BELT";
    public static final String CONVEYOR_SIDE = "IMAGE_UI_CONVEYOR_CONVEYOR_SIDE";
    public static final String CONVEYOR_TOP = "IMAGE_UI_CONVEYOR_CONVEYOR_TOP";
    public static final String PROTECT_TILE = "IMAGE_BACKGROUNDS_PROTECT_TILE_PROTECT_TILE_133X157";

    public static final String[] BOOT_PRELOAD = {
            EGYPT_MAIN,
            EGYPT_LEFT,
            EGYPT_RIGHT,
            ICEAGE_MAIN,
            BEACH_MAIN,
            DARK_MAIN,
            PAUSE,
            SPEED_2X,
            SPEED_2X_SELECTED,
            SHOVEL,
            FLOATING_SHOVEL,
            SUN_ICON,
            SUN_BANNER,
            PLANTFOOD_BANK,
            PLANTFOOD_SLOT,
            PLANTFOOD_LEAF,
            FLOATING_PLANTFOOD,
            HUD_PLUS,
            HUD_PLUS_DOWN,
            PROGRESS_METER,
            PROGRESS_FILL,
            PROGRESS_FLAG,
            PACKET_BG,
            PACKET_EMPTY,
            PACKET_LOCK,
            PACKET_SELECT
    };

    private LawnAssetIds() {
    }

    public static String chapterMain(ChapterId chapterId) {
        if (chapterId == null) {
            return EGYPT_MAIN;
        }
        return switch (chapterId) {
            case ANCIENT_EGYPT -> EGYPT_MAIN;
            case FROSTBITE_CAVES -> ICEAGE_MAIN;
            case BIG_WAVE_BEACH -> BEACH_MAIN;
            case DARK_AGES -> DARK_MAIN;
        };
    }

    public static String chapterLeft(ChapterId chapterId) {
        if (chapterId == null) {
            return EGYPT_LEFT;
        }
        return switch (chapterId) {
            case ANCIENT_EGYPT -> EGYPT_LEFT;
            case FROSTBITE_CAVES -> ICEAGE_LEFT;
            case BIG_WAVE_BEACH -> BEACH_LEFT;
            case DARK_AGES -> DARK_LEFT;
        };
    }

    public static String chapterRight(ChapterId chapterId) {
        if (chapterId == null) {
            return EGYPT_RIGHT;
        }
        return switch (chapterId) {
            case ANCIENT_EGYPT -> EGYPT_RIGHT;
            case FROSTBITE_CAVES -> ICEAGE_RIGHT;
            case BIG_WAVE_BEACH -> BEACH_RIGHT;
            case DARK_AGES -> DARK_RIGHT;
        };
    }
}
