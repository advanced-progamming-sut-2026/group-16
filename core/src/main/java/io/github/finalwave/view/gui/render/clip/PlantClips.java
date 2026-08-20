package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.LawnLayout;


public final class PlantClips {
    public static final String ICE_BLOCK_PATH =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    public static final String ICE_BLOCK_CLIP = "freeze_idle";
    public static final String OCTOPUS_PATH =
            "768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM";
    public static final String OCTOPUS_CLIP = "idle";

    private final EntityAnimationCatalog catalog;

    public PlantClips(EntityAnimationCatalog catalog) {
        this.catalog = catalog;
    }

    public EntityAnimationCatalog.ClipSpec idle(String plantName) {
        return catalog.plantIdle(plantName);
    }

    public EntityAnimationCatalog.ClipSpec attack(String plantName) {
        return catalog.plantClip(plantName, "attack", "idle");
    }

    public boolean hasAttack(String plantName) {
        return catalog.hasClip(catalog.plantIdle(plantName).path(), "attack");
    }

    public EntityAnimationCatalog.ClipSpec clip(String plantName, String... preferredClips) {
        return catalog.plantClip(plantName, preferredClips);
    }

    public float scale(String plantName) {
        if ("Giant Wall-nut".equals(plantName)) {
            return LawnLayout.GIANT_WALLNUT_SCALE;
        }
        return LawnLayout.PLANT_SCALE;
    }
}
