package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.PlantClips;


public final class PlantVisualState {
    private static final String ATTACK = "attack";

    private PlantVisualState() {
    }

    public static EntityAnimationCatalog.ClipSpec clip(Plant plant, PlantClips clips, boolean justFired) {
        if (plant.hasTag(PlantTag.TRAP) && !plant.isArmedTrap()) {
            return clips.clip(plant.getName(), "plant_idle", "plant", "idle");
        }
        if ((justFired || plant.isAttacking())
                && !plant.isDisabled()
                && !plant.isCatTransformed()
                && clips.hasAttack(plant.getName())) {
            return clips.attack(plant.getName());
        }
        return clips.idle(plant.getName());
    }

    public static boolean isAttack(EntityAnimationCatalog.ClipSpec spec) {
        return spec != null && ATTACK.equals(spec.clip());
    }

    public static boolean isAttackClip(String clipName) {
        return ATTACK.equals(clipName);
    }
}
