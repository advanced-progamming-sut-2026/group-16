package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class GrapeshotClips {
    public static final float IDLE_SECONDS = 4.0f;
    public static final float ATTACK_SPAWN_SECONDS = 0.77f;
    public static final String PLANT_NAME = "Grapeshot";
    public static final String PLANT_PATH =
            "768/INITIAL/PLANT/GRAPESHOT/GRAPESHOT.PAM";

    private GrapeshotClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "idle", "attack");
    }

    public static EntityAnimationCatalog.ClipSpec attack(EntityAnimationCatalog catalog, Plant plant) {
        int level = plant == null ? 1 : Math.max(1, plant.getLevel());
        String clip = switch (level) {
            case 1 -> "attack";
            case 2 -> "attack_t2";
            default -> "attack_t3";
        };
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec attack(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "attack", "attack_t2", "attack_t3", "idle");
    }
}
