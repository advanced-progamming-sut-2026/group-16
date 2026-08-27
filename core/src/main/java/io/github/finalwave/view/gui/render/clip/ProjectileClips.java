package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.LawnLayout;

import java.util.LinkedHashSet;
import java.util.List;


public final class ProjectileClips {
    private static final EntityAnimationCatalog.ClipSpec PEA = spec(
            "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec ICE = spec(
            "768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec FIRE = spec(
            "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec CABBAGE = spec(
            "768/INITIAL/EFFECTS/T_CABBAGEPULT_PROJECTILE/T_CABBAGEPULT_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec KERNEL = spec(
            "768/INITIAL/EFFECTS/T_KERNALPULT_PROJECTILE/T_KERNALPULT_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec BUTTER = spec(
            "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec MELON = spec(
            "768/INITIAL/EFFECTS/T_MELON_PROJECTILE/T_MELON_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec WINTER_MELON = spec(
            "768/FULL/EFFECTS/T_WINTERMELON_PROJECTILE/T_WINTERMELON_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec PEPPER = spec(
            "768/FULL/EFFECTS/T_PEPPERPULT_PROJECTILE/T_PEPPERPULT_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec FUME = spec(
            "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM", "special");
    private static final EntityAnimationCatalog.ClipSpec SPIKE = spec(
            "768/INITIAL/EFFECTS/T_CACTUS_PROJECTILE/T_CACTUS_PROJECTILE.PAM", "idle");
    private static final EntityAnimationCatalog.ClipSpec PUFF = spec(
            "768/INITIAL/EFFECTS/T_PUFFSHROOM_PROJECTILE/T_PUFFSHROOM_PROJECTILE.PAM", "animation");
    private static final String SPLAT_CLIP = "animation";
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PEA = spec(
            "768/INITIAL/EFFECTS/SPLAT_PEA/SPLAT_PEA.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_ICE = spec(
            "768/INITIAL/EFFECTS/SPLAT_SNOW_PEA/SPLAT_SNOW_PEA.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_SNOWBALL = spec(
            "768/FULL/EFFECTS/ZOMBIE_HUNTER_SNOWBALL_SPLAT/ZOMBIE_HUNTER_SNOWBALL_SPLAT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_FIRE = spec(
            "768/INITIAL/EFFECTS/SPLAT_FIRE_PEA_BLUE/SPLAT_FIRE_PEA_BLUE.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_CABBAGE = spec(
            "768/INITIAL/EFFECTS/SPLAT_CABBAGEPULT/SPLAT_CABBAGEPULT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_KERNEL = spec(
            "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_KERNAL/SPLAT_KERNALPULT_KERNAL.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_BUTTER = spec(
            "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_MELON = spec(
            "768/INITIAL/EFFECTS/T_SPLAT_MELONPULT/T_SPLAT_MELONPULT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_WINTER_MELON = spec(
            "768/FULL/EFFECTS/T_SPLAT_WINTERMELON/T_SPLAT_WINTERMELON.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PEPPER = spec(
            "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE_SPLAT/PEPPERPULT_PROJECTILE_SPLAT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_FUME = spec(
            "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES_HIT/FUMESHROOM_BUBBLES_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_SPIKE = spec(
            "768/INITIAL/EFFECTS/CACTUS_PROJECTILE_HIT/CACTUS_PROJECTILE_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PUFF = spec(
            "768/INITIAL/EFFECTS/T_PUFFSHROOM_HIT/T_PUFFSHROOM_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_LASER = spec(
            "768/FULL/EFFECTS/LASERBEAN_LASER_HIT/LASERBEAN_LASER_HIT.PAM", SPLAT_CLIP);

    public EntityAnimationCatalog.ClipSpec clip(ProjectileEffect effect) {
        if (effect == null) {
            return PEA;
        }
        return switch (effect) {
            case ICE -> ICE;
            case SNOWBALL -> SPLAT_SNOWBALL;
            case FIRE -> FIRE;
            case CABBAGE -> CABBAGE;
            case KERNEL -> KERNEL;
            case BUTTER -> BUTTER;
            case MELON -> MELON;
            case WINTER_MELON -> WINTER_MELON;
            case PEPPER -> PEPPER;
            case FUME -> FUME;
            case SPIKE -> SPIKE;
            case PUFF -> PUFF;
            case PEA, POISON, LASER, GENERIC -> PEA;
        };
    }

    public float scale(ProjectileEffect effect) {
        if (effect == ProjectileEffect.CABBAGE) {
            return 0.82f;
        }
        return LawnLayout.PROJECTILE_SCALE;
    }

    public EntityAnimationCatalog.ClipSpec splat(ProjectileEffect effect) {
        if (effect == null) {
            return SPLAT_PEA;
        }
        return switch (effect) {
            case ICE -> SPLAT_ICE;
            case SNOWBALL -> SPLAT_SNOWBALL;
            case FIRE -> SPLAT_FIRE;
            case CABBAGE -> SPLAT_CABBAGE;
            case KERNEL -> SPLAT_KERNEL;
            case BUTTER -> SPLAT_BUTTER;
            case MELON -> SPLAT_MELON;
            case WINTER_MELON -> SPLAT_WINTER_MELON;
            case PEPPER -> SPLAT_PEPPER;
            case FUME, POISON -> SPLAT_FUME;
            case SPIKE -> SPLAT_SPIKE;
            case PUFF -> SPLAT_PUFF;
            case LASER -> SPLAT_LASER;
            case PEA, GENERIC -> SPLAT_PEA;
        };
    }

    public List<String> splatPaths() {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        for (ProjectileEffect effect : ProjectileEffect.values()) {
            paths.add(splat(effect).path());
        }
        return List.copyOf(paths);
    }

    private static EntityAnimationCatalog.ClipSpec spec(String path, String clip) {
        return new EntityAnimationCatalog.ClipSpec(path, clip);
    }
}
