package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.LawnLayout;


public final class PlantFoodFxClips {
    public static final String PATH = "768/INITIAL/EFFECTS/PLANTFOOD_FX/PLANTFOOD_FX.PAM";
    public static final String ON = "plantfood_on";
    public static final String LOOP = "plantfood";
    public static final String OFF = "plantfood_off";
    private static final float CANVAS_HEIGHT = 400f;
    public static final float FIT_TILES = 5.0f;
    public static final float OFFSET_X = 20f;
    public static final float OFFSET_Y = 185f;
    public static final int SORT_BEHIND_OFFSET = -2;
    public static final int SORT_FRONT_OFFSET = 2;

    private PlantFoodFxClips() {
    }

    public static EntityAnimationCatalog.ClipSpec on() {
        return new EntityAnimationCatalog.ClipSpec(PATH, ON);
    }

    public static EntityAnimationCatalog.ClipSpec loop() {
        return new EntityAnimationCatalog.ClipSpec(PATH, LOOP);
    }

    public static EntityAnimationCatalog.ClipSpec off() {
        return new EntityAnimationCatalog.ClipSpec(PATH, OFF);
    }

    public static float scale(LawnLayout layout) {
        if (layout == null) {
            return 1f;
        }
        return layout.tileHeight() * FIT_TILES / CANVAS_HEIGHT;
    }

    public static boolean drawsInFront(String clipName) {
        return ON.equals(clipName) || OFF.equals(clipName);
    }
}
