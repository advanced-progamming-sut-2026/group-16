package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.food.PotatoMinePlantFood;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class PotatoMineClips {
    public static final float DETONATION_FX_SECONDS = 0.63f;
    public static final String EXPLOSION_CLIP = "animation";

    private static final String POTATO_EXPLOSION_PATH =
            "768/INITIAL/EFFECTS/POTATOMINE_EXPLOSION/POTATOMINE_EXPLOSION.PAM";
    private static final String PRIMAL_EXPLOSION_PATH =
            "768/INITIAL/EFFECTS/PRIMAL_POTATOMINE_EXPLOSION/PRIMAL_POTATOMINE_EXPLOSION.PAM";

    private PotatoMineClips() {
    }

    public static EntityAnimationCatalog.ClipSpec intro(EntityAnimationCatalog catalog, String plantName) {
        return catalog.plantClip(plantName, "plant", "plant_idle", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec unarmedIdle(EntityAnimationCatalog catalog, String plantName) {
        return catalog.plantClip(plantName, "plant_idle", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec recover(EntityAnimationCatalog catalog, String plantName) {
        return catalog.plantClip(plantName, "recover", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec armedIdle(EntityAnimationCatalog catalog, String plantName) {
        return catalog.plantClip(plantName, "idle", "plant_idle");
    }

    public static EntityAnimationCatalog.ClipSpec attack(EntityAnimationCatalog catalog, String plantName) {
        return catalog.plantClip(plantName, "attack", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec cloneIntro(EntityAnimationCatalog catalog, String plantName) {
        return catalog.plantClip(plantName, "plantfood2", "recover", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodPhase(
            EntityAnimationCatalog catalog,
            Plant plant,
            PotatoMinePlantFood.Phase phase) {
        String name = plant.getName();
        return switch (phase) {
            case ON -> catalog.plantClip(name, "plantfood_on", "plantfood", "idle");
            case LOOP -> catalog.plantClip(name, "plantfood", "plantfood_on", "idle");
            case OFF -> catalog.plantClip(name, "plantfood_off", "plantfood", "idle");
            case NONE -> armedIdle(catalog, name);
        };
    }

    public static EntityAnimationCatalog.ClipSpec explosion(boolean primal) {
        if (primal) {
            return new EntityAnimationCatalog.ClipSpec(PRIMAL_EXPLOSION_PATH, EXPLOSION_CLIP);
        }
        return new EntityAnimationCatalog.ClipSpec(POTATO_EXPLOSION_PATH, EXPLOSION_CLIP);
    }

    public static EntityAnimationCatalog.ClipSpec explosion(Plant plant) {
        return explosion(plant != null && plant.isPrimalPotatoMine());
    }
}
