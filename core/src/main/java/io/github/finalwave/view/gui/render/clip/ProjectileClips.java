package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.Projectile;
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
    private static final EntityAnimationCatalog.ClipSpec CABBAGE_PLANTFOOD = spec(
            "768/INITIAL/EFFECTS/CABBAGEPULT_PLANTFOOD_PROJECTILE/CABBAGEPULT_PLANTFOOD_PROJECTILE.PAM",
            "plantfood_cabbage");
    private static final EntityAnimationCatalog.ClipSpec KERNEL = spec(
            "768/INITIAL/EFFECTS/T_KERNALPULT_PROJECTILE/T_KERNALPULT_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec BUTTER = spec(
            "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec MELON = spec(
            "768/INITIAL/EFFECTS/T_MELON_PROJECTILE/T_MELON_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec WINTER_MELON = spec(
            "768/FULL/EFFECTS/T_WINTERMELON_PROJECTILE/T_WINTERMELON_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec PEPPER = spec(
            "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE/PEPPERPULT_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec PEPPER_PLANTFOOD = spec(
            "768/FULL/EFFECTS/T_PEPPERPULT_PROJECTILE/T_PEPPERPULT_PROJECTILE.PAM", "animation3");
    private static final EntityAnimationCatalog.ClipSpec FUME = spec(
            "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM", "special");
    private static final EntityAnimationCatalog.ClipSpec FUME_PLANTFOOD = spec(
            "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM", "plantfood");
    private static final EntityAnimationCatalog.ClipSpec SPIKE = spec(
            "768/INITIAL/EFFECTS/T_CACTUS_PROJECTILE/T_CACTUS_PROJECTILE.PAM", "idle");
    private static final EntityAnimationCatalog.ClipSpec PUFF = spec(
            "768/INITIAL/EFFECTS/T_PUFFSHROOM_PROJECTILE/T_PUFFSHROOM_PROJECTILE.PAM", "animation");
    private static final String GOO_PROJECTILES_PATH =
            "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
    private static final EntityAnimationCatalog.ClipSpec GOO = spec(GOO_PROJECTILES_PATH, "projectile_t1");
    private static final EntityAnimationCatalog.ClipSpec MEGA_GATLING_PEA = spec(
            "768/INITIAL/EFFECTS/MEGAGATLING_PROJECTILE/MEGAGATLING_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec SEA_SHROOM = spec(
            "768/FULL/EFFECTS/SEASHROOM_PROJECTILE/SEASHROOM_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec ROTOBAGA = spec(
            "768/FULL/EFFECTS/ROTORUTABAGA_PROJECTILE1/ROTORUTABAGA_PROJECTILE1.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec STAR = spec(
            "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec STARFRUIT = STAR;
    private static final EntityAnimationCatalog.ClipSpec STAR_PF = spec(
            "768/INITIAL/EFFECTS/STARFRUIT_PROJECTILE_PLANTFOOD/STARFRUIT_PROJECTILE_PLANTFOOD.PAM",
            "animation");
    private static final EntityAnimationCatalog.ClipSpec GOO_PF = spec(
            "768/INITIAL/EFFECTS/GOOPEASHOOTER_PLANTFOOD/GOOPEASHOOTER_PLANTFOOD.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec GOO_PUDDLE = spec(
            "768/INITIAL/EFFECTS/GOOPEASHOOTER_PLANTFOOD_TILE/GOOPEASHOOTER_PLANTFOOD_TILE.PAM",
            "animation2");
    private static final EntityAnimationCatalog.ClipSpec GIANT_PEA = spec(
            "768/INITIAL/EFFECTS/REPEATER_PLANTFOOD_GIANTPEA/REPEATER_PLANTFOOD_GIANTPEA.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec PEAPOD_GIANT_PEA = spec(
            "768/FULL/EFFECTS/PEAPOD_PLANTFOOD_GIANTPEA/PEAPOD_PLANTFOOD_GIANTPEA.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec PLASMA = spec(
            "768/FULL/EFFECTS/T_CITRON_CITRUS_ORB/T_CITRON_CITRUS_ORB.PAM", "Citron_Citrus_Orb");
    private static final EntityAnimationCatalog.ClipSpec CITRON = PLASMA;
    private static final EntityAnimationCatalog.ClipSpec PLASMA_PF = spec(
            "768/FULL/EFFECTS/CITRON_PLANTFOOD_ORB/CITRON_PLANTFOOD_ORB.PAM", "Plantfood_Citron_Plasma_Orb");
    private static final EntityAnimationCatalog.ClipSpec MAGIC_BEAM = spec(
            "768/INITIAL/EFFECTS/CAULIPOWER_PROJECTILE/CAULIPOWER_PROJECTILE.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec CAULIPOWER = MAGIC_BEAM;
    private static final EntityAnimationCatalog.ClipSpec LIGHTNING = spec(
            "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM",
            "attack");
    private static final EntityAnimationCatalog.ClipSpec BLUEBERRY = LIGHTNING;
    private static final EntityAnimationCatalog.ClipSpec BOWLING_CYAN = spec(
            "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE1/BOWLINGBULB_PROJECTILE1.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec BOWLING = BOWLING_CYAN;
    private static final EntityAnimationCatalog.ClipSpec BOWLING_BLUE = spec(
            "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE2/BOWLINGBULB_PROJECTILE2.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec BOWLING_ORANGE = spec(
            "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE3/BOWLINGBULB_PROJECTILE3.PAM", "animation");
    private static final EntityAnimationCatalog.ClipSpec BOWLING_PF = spec(
            "768/FULL/EFFECTS/BOWLINGBULB_PLANTFOOD_PROJECTILE/BOWLINGBULB_PLANTFOOD_PROJECTILE.PAM",
            "animation");
    private static final EntityAnimationCatalog.ClipSpec SPIKE_PF = spec(
            "768/INITIAL/EFFECTS/CACTUS_PROJECTILE_PLANTFOOD/CACTUS_PROJECTILE_PLANTFOOD.PAM", "idle");
    private static final String GRAPE_PROJECTILE_PATH =
            "768/INITIAL/EFFECTS/GRAPESHOT_PROJECTILE/GRAPESHOT_PROJECTILE.PAM";
    private static final EntityAnimationCatalog.ClipSpec GRAPE = spec(
            GRAPE_PROJECTILE_PATH, "animation_forward");
    private static final EntityAnimationCatalog.ClipSpec GRAPE_FORWARD = spec(
            GRAPE_PROJECTILE_PATH, "animation_forward");
    private static final EntityAnimationCatalog.ClipSpec GRAPE_BACKWARD = spec(
            GRAPE_PROJECTILE_PATH, "animation_backward");
    private static final EntityAnimationCatalog.ClipSpec GRAPE_UP = spec(
            GRAPE_PROJECTILE_PATH, "animation_verticle_up");
    private static final EntityAnimationCatalog.ClipSpec GRAPE_DOWN = spec(
            GRAPE_PROJECTILE_PATH, "animation_verticle_down");
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
    private static final EntityAnimationCatalog.ClipSpec SPLAT_CABBAGE_PLANTFOOD = spec(
            "768/INITIAL/EFFECTS/CABBAGEPULT_PLANTFOOD_PROJECTILE/CABBAGEPULT_PLANTFOOD_PROJECTILE.PAM",
            "plantfood_cabbageExplode");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_KERNEL = spec(
            "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_KERNAL/SPLAT_KERNALPULT_KERNAL.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_BUTTER = spec(
            "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_MELON = spec(
            "768/INITIAL/EFFECTS/T_SPLAT_MELONPULT/T_SPLAT_MELONPULT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_MELON_PLANTFOOD = spec(
            "768/INITIAL/EFFECTS/MELON_EXPLODE/MELON_EXPLODE.PAM", "plantfood_MelonExplode");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_WINTER_MELON = spec(
            "768/FULL/EFFECTS/T_SPLAT_WINTERMELON/T_SPLAT_WINTERMELON.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_WINTER_MELON_PLANTFOOD = spec(
            "768/FULL/EFFECTS/WINTERMELON_EXPLODE/WINTERMELON_EXPLODE.PAM",
            "plantfood_WintermelonExplode");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PEPPER = spec(
            "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE_SPLAT/PEPPERPULT_PROJECTILE_SPLAT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PEPPER_PLANTFOOD = spec(
            "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE_PF_SPLAT/PEPPERPULT_PROJECTILE_PF_SPLAT.PAM",
            SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_FUME = spec(
            "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES_HIT/FUMESHROOM_BUBBLES_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_SPIKE = spec(
            "768/INITIAL/EFFECTS/CACTUS_PROJECTILE_HIT/CACTUS_PROJECTILE_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PUFF = spec(
            "768/INITIAL/EFFECTS/T_PUFFSHROOM_HIT/T_PUFFSHROOM_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_ROTOBAGA = spec(
            "768/FULL/EFFECTS/ROTORUTABAGA_PROJECTILE_HIT/ROTORUTABAGA_PROJECTILE_HIT.PAM", SPLAT_CLIP);
    private static final String STARFRUIT_HIT_PATH =
            "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE_HIT/T_STARFRUIT_PROJECTILE_HIT.PAM";
    private static final EntityAnimationCatalog.ClipSpec SPLAT_STAR = spec(STARFRUIT_HIT_PATH, "idle");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_LASER = spec(
            "768/FULL/EFFECTS/LASERBEAN_LASER_HIT/LASERBEAN_LASER_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PLASMA = spec(
            "768/FULL/EFFECTS/CITRON_CITRUS_ORB_HIT/CITRON_CITRUS_ORB_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_CITRON = SPLAT_PLASMA;
    private static final EntityAnimationCatalog.ClipSpec SPLAT_PLASMA_PF = spec(
            "768/FULL/EFFECTS/CITRON_PLANTFOOD_ORB_HIT/CITRON_PLANTFOOD_ORB_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_MAGIC = spec(
            "768/INITIAL/EFFECTS/CAULIPOWER_PROJECTILE/CAULIPOWER_PROJECTILE.PAM", "animation2");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_LIGHTNING = spec(
            "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM",
            "death");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_GOO = spec(
            "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM", "hit_t1");
    private static final EntityAnimationCatalog.ClipSpec SPLAT_STARFRUIT = SPLAT_STAR;
    private static final EntityAnimationCatalog.ClipSpec SPLAT_CAULIPOWER = spec(
            "768/INITIAL/EFFECTS/HYPNO_ZOMBIE_EFFECT/HYPNO_ZOMBIE_EFFECT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_BLUEBERRY = spec(
            "768/INITIAL/EFFECTS/ELECTRIC_PEASHOOTER_ELECTROBALL_EFFECTS/ELECTRIC_PEASHOOTER_ELECTROBALL_EFFECTS.PAM",
            SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_GRAPE = spec(
            "768/INITIAL/EFFECTS/GRAPESHOT_HIT/GRAPESHOT_HIT.PAM", SPLAT_CLIP);
    private static final EntityAnimationCatalog.ClipSpec SPLAT_GIANT_PEA = spec(
            "768/INITIAL/EFFECTS/SPLAT_GIANTPEA/SPLAT_GIANTPEA.PAM", SPLAT_CLIP);

    public EntityAnimationCatalog.ClipSpec clip(Projectile projectile) {
        if (projectile != null && projectile.isGiantPea()) {
            if (projectile.getSource() != null && projectile.getSource().isPeaPod()) {
                return PEAPOD_GIANT_PEA;
            }
            return GIANT_PEA;
        }
        if (projectile != null && projectile.isFumePlantFood()) {
            return FUME_PLANTFOOD;
        }
        if (projectile != null && projectile.isCabbagePlantFood()) {
            return CABBAGE_PLANTFOOD;
        }
        if (projectile != null && projectile.isPepperPlantFood()) {
            return PEPPER_PLANTFOOD;
        }
        if (projectile != null && projectile.isGrapeshotGrape()) {
            return grapeshotFlightClip(projectile.getVelocityX(), projectile.getVelocityY());
        }
        EntityAnimationCatalog.ClipSpec named = namedFlight(sourceName(projectile));
        if (named != null) {
            return named;
        }
        return clip(projectile == null ? null : projectile.getEffect(),
                projectile == null ? null : projectile.getVisualClip());
    }

    public EntityAnimationCatalog.ClipSpec clip(ProjectileEffect effect) {
        return clip(effect, null);
    }

    public EntityAnimationCatalog.ClipSpec clip(ProjectileEffect effect, String clipOverride) {
        if (effect == ProjectileEffect.GOO) {
            String clip = clipOverride == null || clipOverride.isBlank() ? "projectile_t1" : clipOverride;
            return spec(GOO_PROJECTILES_PATH, clip);
        }
        if (effect == ProjectileEffect.STAR || effect == ProjectileEffect.STAR_PF) {
            String clip = clipOverride == null || clipOverride.isBlank() ? "animation" : clipOverride;
            String path = effect == ProjectileEffect.STAR_PF
                    ? "768/INITIAL/EFFECTS/STARFRUIT_PROJECTILE_PLANTFOOD/STARFRUIT_PROJECTILE_PLANTFOOD.PAM"
                    : "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM";
            return spec(path, clip);
        }
        if (effect == ProjectileEffect.MEGA_GATLING_PEA) {
            String clip = clipOverride == null || clipOverride.isBlank() ? "animation" : clipOverride;
            return spec(
                    "768/INITIAL/EFFECTS/MEGAGATLING_PROJECTILE/MEGAGATLING_PROJECTILE.PAM",
                    clip);
        }
        if (effect == ProjectileEffect.SEA_SHROOM) {
            String clip = clipOverride == null || clipOverride.isBlank() ? "animation" : clipOverride;
            return spec(
                    "768/FULL/EFFECTS/SEASHROOM_PROJECTILE/SEASHROOM_PROJECTILE.PAM",
                    clip);
        }
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
            case GOO -> GOO;
            case MEGA_GATLING_PEA -> MEGA_GATLING_PEA;
            case SEA_SHROOM -> SEA_SHROOM;
            case ROTOBAGA -> ROTOBAGA;
            case GIANT_PEA -> GIANT_PEA;
            case PLASMA -> PLASMA;
            case PLASMA_PF -> PLASMA_PF;
            case MAGIC_BEAM -> MAGIC_BEAM;
            case LIGHTNING -> LIGHTNING;
            case BOWLING_CYAN -> BOWLING_CYAN;
            case BOWLING_BLUE -> BOWLING_BLUE;
            case BOWLING_ORANGE -> BOWLING_ORANGE;
            case BOWLING_PF -> BOWLING_PF;
            case SPIKE_PF -> SPIKE_PF;
            case STAR -> STAR;
            case STAR_PF -> STAR_PF;
            case GOO_PF -> GOO_PF;
            case GRAPE -> GRAPE_FORWARD;
            case PEA, POISON, LASER, GENERIC -> PEA;
        };
    }

    public float scale(Projectile projectile) {
        if (projectile != null && projectile.isGiantPea()) {
            return LawnLayout.PROJECTILE_DRAW_SCALE;
        }
        float named = namedScale(sourceName(projectile));
        if (!Float.isNaN(named)) {
            return LawnLayout.PROJECTILE_DRAW_SCALE * named;
        }
        float effectScale = scale(projectile == null ? null : projectile.getEffect());
        float visual = projectile == null ? 1f : projectile.getVisualScale();
        return effectScale * (visual <= 0f ? 1f : visual);
    }

    public float scale(ProjectileEffect effect) {
        if (effect == ProjectileEffect.PLASMA_PF || effect == ProjectileEffect.SPIKE_PF) {
            return 1.35f;
        }
        if (effect == ProjectileEffect.GOO_PF) {
            return 0.6f;
        }
        if (effect == ProjectileEffect.GOO) {
            return 0.95f;
        }
        if (effect == ProjectileEffect.MEGA_GATLING_PEA) {
            return 0.9f;
        }
        if (effect == ProjectileEffect.SEA_SHROOM) {
            return 0.88f;
        }
        if (effect == ProjectileEffect.STAR_PF) {
            return 1.35f;
        }
        if (effect == ProjectileEffect.BOWLING_PF) {
            return 1.5f;
        }
        if (effect == ProjectileEffect.MAGIC_BEAM || effect == ProjectileEffect.LIGHTNING) {
            return 1.1f;
        }
        float multiplier = switch (effect == null ? ProjectileEffect.GENERIC : effect) {
            case PUFF -> 0.85f;
            case SPIKE -> 0.9f;
            case CABBAGE, KERNEL, BUTTER -> 0.95f;
            case MELON, WINTER_MELON, PEPPER -> 1.05f;
            case FUME -> 1.0f;
            default -> 1.0f;
        };
        return LawnLayout.PROJECTILE_DRAW_SCALE * multiplier;
    }

    public EntityAnimationCatalog.ClipSpec splat(Projectile projectile) {
        if (projectile != null && projectile.isGiantPea()) {
            return SPLAT_GIANT_PEA;
        }
        if (projectile != null && projectile.isCabbagePlantFood()) {
            return SPLAT_CABBAGE_PLANTFOOD;
        }
        if (projectile != null && projectile.isMelonPlantFood()) {
            if (projectile.getEffect() == ProjectileEffect.WINTER_MELON) {
                return SPLAT_WINTER_MELON_PLANTFOOD;
            }
            return SPLAT_MELON_PLANTFOOD;
        }
        if (projectile != null && projectile.isPepperPlantFood()) {
            return SPLAT_PEPPER_PLANTFOOD;
        }
        if (projectile != null && projectile.getEffect() == ProjectileEffect.FUME) {
            return null;
        }
        EntityAnimationCatalog.ClipSpec named = namedSplat(sourceName(projectile));
        if (named != null || "Bowling Bulb".equals(sourceName(projectile))) {
            return named;
        }
        return splat(projectile == null ? null : projectile.getEffect(),
                projectile == null ? null : projectile.getVisualClip());
    }

    public EntityAnimationCatalog.ClipSpec splat(ProjectileEffect effect) {
        return splat(effect, null);
    }

    public EntityAnimationCatalog.ClipSpec splat(ProjectileEffect effect, String clipOverride) {
        if (effect == ProjectileEffect.GOO) {
            String clip = clipOverride == null || clipOverride.isBlank()
                    ? "hit_t1"
                    : clipOverride.replace("projectile_", "hit_");
            return spec(GOO_PROJECTILES_PATH, clip);
        }
        if (effect == ProjectileEffect.STAR || effect == ProjectileEffect.STAR_PF) {
            String clip = clipOverride == null || clipOverride.isBlank()
                    ? "idle"
                    : clipOverride.replace("animation", "idle");
            return spec(STARFRUIT_HIT_PATH, clip);
        }
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
            case FUME -> null;
            case POISON -> SPLAT_GOO;
            case GRAPE -> SPLAT_GRAPE;
            case SPIKE -> SPLAT_SPIKE;
            case PUFF -> SPLAT_PUFF;
            case GOO -> spec(GOO_PROJECTILES_PATH, "hit_t1");
            case MEGA_GATLING_PEA, SEA_SHROOM -> SPLAT_PEA;
            case ROTOBAGA -> SPLAT_ROTOBAGA;
            case LASER -> SPLAT_LASER;
            case PLASMA -> SPLAT_PLASMA;
            case PLASMA_PF -> SPLAT_PLASMA_PF;
            case MAGIC_BEAM -> SPLAT_MAGIC;
            case LIGHTNING -> SPLAT_LIGHTNING;
            case SPIKE_PF -> SPLAT_SPIKE;
            case STAR, STAR_PF -> SPLAT_STAR;
            case GOO_PF -> SPLAT_FUME;
            case BOWLING_CYAN, BOWLING_BLUE, BOWLING_ORANGE, BOWLING_PF -> SPLAT_PEA;
            case PEA, GIANT_PEA, GENERIC -> SPLAT_PEA;
        };
    }

    public EntityAnimationCatalog.ClipSpec gooPuddle() {
        return GOO_PUDDLE;
    }

    public EntityAnimationCatalog.ClipSpec fumeHit() {
        return SPLAT_FUME;
    }

    public List<String> splatPaths() {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        for (ProjectileEffect effect : ProjectileEffect.values()) {
            EntityAnimationCatalog.ClipSpec spec = splat(effect);
            if (spec != null) {
                paths.add(spec.path());
            }
        }
        addPath(paths, SPLAT_ROTOBAGA);
        addPath(paths, SPLAT_STARFRUIT);
        addPath(paths, SPLAT_CITRON);
        addPath(paths, SPLAT_CAULIPOWER);
        addPath(paths, SPLAT_BLUEBERRY);
        addPath(paths, SPLAT_GRAPE);
        addPath(paths, SPLAT_GOO);
        addPath(paths, SPLAT_GIANT_PEA);
        addPath(paths, SPLAT_FUME);
        addPath(paths, SPLAT_CABBAGE_PLANTFOOD);
        addPath(paths, SPLAT_MELON_PLANTFOOD);
        addPath(paths, SPLAT_WINTER_MELON_PLANTFOOD);
        addPath(paths, SPLAT_PEPPER_PLANTFOOD);
        return List.copyOf(paths);
    }

    public List<String> flightPaths() {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        for (ProjectileEffect effect : ProjectileEffect.values()) {
            paths.add(clip(effect).path());
        }
        addPath(paths, ROTOBAGA);
        addPath(paths, STARFRUIT);
        addPath(paths, CITRON);
        addPath(paths, CAULIPOWER);
        addPath(paths, BLUEBERRY);
        addPath(paths, BOWLING);
        addPath(paths, GRAPE);
        addPath(paths, GRAPE_FORWARD);
        addPath(paths, GRAPE_BACKWARD);
        addPath(paths, GRAPE_UP);
        addPath(paths, GRAPE_DOWN);
        addPath(paths, GOO);
        addPath(paths, GIANT_PEA);
        addPath(paths, PEAPOD_GIANT_PEA);
        addPath(paths, CABBAGE_PLANTFOOD);
        addPath(paths, PEPPER_PLANTFOOD);
        return List.copyOf(paths);
    }

    public EntityAnimationCatalog.ClipSpec grapeshotFlightClip(double velocityX, double velocityY) {
        if (Math.abs(velocityX) >= Math.abs(velocityY)) {
            return velocityX >= 0 ? GRAPE_FORWARD : GRAPE_BACKWARD;
        }
        return velocityY < 0 ? GRAPE_UP : GRAPE_DOWN;
    }

    private static EntityAnimationCatalog.ClipSpec namedFlight(String plantName) {
        if (plantName == null) {
            return null;
        }
        return switch (plantName) {
            case "Rotobaga" -> ROTOBAGA;
            case "Starfruit" -> STARFRUIT;
            case "Citron" -> CITRON;
            case "Caulipower" -> CAULIPOWER;
            case "Electric Blueberry" -> BLUEBERRY;
            case "Bowling Bulb" -> BOWLING;
            case "Grapeshot" -> GRAPE;
            case "Goo Peashooter" -> GOO;
            default -> null;
        };
    }

    private static float namedScale(String plantName) {
        if (plantName == null) {
            return Float.NaN;
        }
        return switch (plantName) {
            case "Citron" -> 1.05f;
            case "Caulipower" -> 1.0f;
            case "Electric Blueberry" -> 1.1f;
            case "Rotobaga", "Bowling Bulb", "Grapeshot", "Starfruit", "Goo Peashooter" -> 1.0f;
            default -> Float.NaN;
        };
    }

    private static EntityAnimationCatalog.ClipSpec namedSplat(String plantName) {
        if (plantName == null) {
            return null;
        }
        return switch (plantName) {
            case "Rotobaga" -> SPLAT_ROTOBAGA;
            case "Starfruit" -> SPLAT_STARFRUIT;
            case "Citron" -> SPLAT_CITRON;
            case "Caulipower" -> SPLAT_CAULIPOWER;
            case "Electric Blueberry" -> SPLAT_BLUEBERRY;
            case "Bowling Bulb" -> null;
            case "Fume-shroom" -> null;
            case "Grapeshot" -> SPLAT_GRAPE;
            case "Goo Peashooter" -> SPLAT_GOO;
            default -> null;
        };
    }

    private static String sourceName(Projectile projectile) {
        if (projectile == null) {
            return null;
        }
        Plant source = projectile.getSource();
        return source == null ? null : source.getName();
    }

    private static void addPath(LinkedHashSet<String> paths, EntityAnimationCatalog.ClipSpec spec) {
        if (spec != null && spec.path() != null) {
            paths.add(spec.path());
        }
    }

    private static EntityAnimationCatalog.ClipSpec spec(String path, String clip) {
        return new EntityAnimationCatalog.ClipSpec(path, clip);
    }
}
