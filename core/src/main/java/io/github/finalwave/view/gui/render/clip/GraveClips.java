package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.board.tile.GraveTile;


public final class GraveClips {
    public static final String EGYPT =
            "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
    public static final String DARK_NONE =
            "768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM";
    public static final String DARK_SUN =
            "768/FULL/GRAVESTONES/DARK_SUN/DARK_SUN.PAM";
    public static final String DARK_FOOD =
            "768/FULL/GRAVESTONES/DARK_PLANTFOOD/DARK_PLANTFOOD.PAM";

    public static final String UNDAMAGED = "undamaged";
    public static final String DAMAGE_1 = "damage1";
    public static final String DAMAGE_2 = "damage2";
    public static final String DAMAGE_3 = "damage3";
    public static final String DAMAGE_4 = "damage4";

    private GraveClips() {
    }

    public static String pathFor(ChapterId chapterId, GraveTile.Loot loot) {
        if (chapterId == ChapterId.ANCIENT_EGYPT) {
            return EGYPT;
        }
        if (loot == GraveTile.Loot.SUN_50) {
            return DARK_SUN;
        }
        if (loot == GraveTile.Loot.PLANT_FOOD) {
            return DARK_FOOD;
        }
        return DARK_NONE;
    }

    public static String clipFor(int health, int maxHealth) {
        if (health <= 0 || maxHealth <= 0) {
            return DAMAGE_4;
        }
        float ratio = health / (float) maxHealth;
        if (ratio > 0.8f) {
            return UNDAMAGED;
        }
        if (ratio > 0.6f) {
            return DAMAGE_1;
        }
        if (ratio > 0.4f) {
            return DAMAGE_2;
        }
        if (ratio > 0.2f) {
            return DAMAGE_3;
        }
        return DAMAGE_4;
    }

    public static String[] preloadPaths(ChapterId chapterId) {
        if (chapterId == ChapterId.ANCIENT_EGYPT) {
            return new String[]{EGYPT};
        }
        return new String[]{DARK_NONE, DARK_SUN, DARK_FOOD};
    }
}
