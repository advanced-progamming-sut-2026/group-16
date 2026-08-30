package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.plant.ability.SquashAbility;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class SquashClips {
    public static final float IDLE_SECONDS = 1.97f;
    public static final float JUMP_UP_SECONDS = SquashAbility.JUMP_UP_SECONDS;
    public static final float JUMP_DOWN_SECONDS = SquashAbility.JUMP_DOWN_SECONDS;
    public static final float TURN_SECONDS = SquashAbility.TURN_SECONDS;
    public static final float PF_JUMP_DOWN_RIGHT_SECONDS = SquashAbility.PF_JUMP_DOWN_RIGHT_SECONDS;
    public static final float PF_JUMP_DOWN_LEFT_SECONDS = SquashAbility.PF_JUMP_DOWN_LEFT_SECONDS;
    public static final String PLANT_NAME = "Squash";
    public static final String PAM_PATH = "768/INITIAL/PLANT/SQUASH/SQUASH.PAM";

    private SquashClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "idle", "plant_idle");
    }

    public static EntityAnimationCatalog.ClipSpec jumpUpRight(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "jump_up_right", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec jumpDownRight(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "jump_down_right", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodJumpDownRight(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "plantfood_jump_down_right", "jump_down_right", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec turn(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "turn", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec jumpUpLeft(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "jump_up_left", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec jumpDownLeft(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "jump_down_left", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodJumpDownLeft(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "plantfood_jump_down_left", "jump_down_left", "idle");
    }
}
