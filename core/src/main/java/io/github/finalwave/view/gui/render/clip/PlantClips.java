package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.LawnLayout;


public final class PlantClips {
    public static final String ICE_BLOCK_PATH =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    public static final String ICE_BLOCK_CLIP = "freeze_idle";
    public static final String ICE_BLOCK_START_CLIP = "freeze_start";
    public static final String OCTOPUS_PATH =
            "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM";
    public static final String OCTOPUS_FLY_CLIP = "animation";
    public static final String OCTOPUS_LAND_CLIP = "animation2";
    public static final String OCTOPUS_IDLE_CLIP = "animation3";
    public static final String SHEEP_PATH =
            "768/FULL/EFFECTS/DARK_WIZARD_SHEEPENING/DARK_WIZARD_SHEEPENING.PAM";
    public static final String SHEEP_INTRO_CLIP = "animation";
    public static final String SHEEP_IDLE_CLIP = "idle";
    public static final String GRAVE_BUSTER_PATH =
            "768/INITIAL/PLANT/GRAVEBUSTER/GRAVEBUSTER.PAM";
    public static final String GRAVE_BUSTER_DIRT_PATH =
            "768/INITIAL/EFFECTS/GRAVEBUSTER_DIRT/GRAVEBUSTER_DIRT.PAM";
    public static final String GRAVE_BUSTER_DIRT_INTRO = "gravebuster_dirt_anim";
    public static final String GRAVE_BUSTER_DIRT_IDLE = "idle";
    public static final String GRAVE_BUSTER_DIRT_FADE = "gravebuster_dirt_fade";
    public static final String GRAVE_BUSTER_ATTACK = "attack";
    public static final String GRAVE_BUSTER_EAT = "attack1";
    public static final String GRAVE_BUSTER_DIRT_PART = "gravebuster_dirt";

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
        return hasClip(plantName, "attack");
    }

    public boolean hasClip(String plantName, String clipName) {
        return catalog.hasClip(catalog.plantIdle(plantName).path(), clipName);
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
