package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.LawnLayout;


public final class PlantClips {
    public static final String ICE_BLOCK_PATH =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    public static final String ICE_BLOCK_CLIP = "freeze_idle";

    private final EntityAnimationCatalog catalog;

    public PlantClips(EntityAnimationCatalog catalog) {
        this.catalog = catalog;
    }

    public EntityAnimationCatalog.ClipSpec idle(String plantName) {
        return catalog.plantIdle(plantName);
    }

    public EntityAnimationCatalog.ClipSpec clip(String plantName, String preferredClip) {
        return catalog.plantClip(plantName, preferredClip);
    }

    public float scale(String plantName) {
        if ("Giant Wall-nut".equals(plantName)) {
            return LawnLayout.GIANT_WALLNUT_SCALE;
        }
        return LawnLayout.PLANT_SCALE;
    }
}
