package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class ProjectileClips {
    private static final EntityAnimationCatalog.ClipSpec PEA = new EntityAnimationCatalog.ClipSpec(
            "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec ICE = new EntityAnimationCatalog.ClipSpec(
            "768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec FIRE = new EntityAnimationCatalog.ClipSpec(
            "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM", "animation");

    public EntityAnimationCatalog.ClipSpec clip(ProjectileEffect effect) {
        if (effect == null) {
            return PEA;
        }
        return switch (effect) {
            case ICE -> ICE;
            case FIRE -> FIRE;
            default -> PEA;
        };
    }
}
