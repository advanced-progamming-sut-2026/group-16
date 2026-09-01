package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.food.PeaPodPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PotatoMinePlantFood;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.PotatoMineClips;
import io.github.finalwave.view.gui.render.LawnLayout;

public final class PlantClips {
    public static final String ICE_BLOCK_PATH = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    public static final String ICE_BLOCK_CLIP = "freeze_idle";
    public static final String ICE_BLOCK_START_CLIP = "freeze_start";
    public static final String CHILL_PLANT_PATH = "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM";
    public static final String CHILL_STAGE_1 = "chill_stage1";
    public static final String CHILL_STAGE_2 = "chill_stage2";
    public static final float ICE_OVERLAY_ALPHA = 0.8f;
    public static final String[] ICE_BLOCK_DAMAGE_PARTS = {
            "ice_block_damage0",
            "ice_block_damage1",
            "ice_block_damage2",
            "ice_block_damage3",
            "ice_block_damage4",
            "ice_block_damage5"
    };
    public static final String OCTOPUS_PATH = "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM";
    public static final String OCTOPUS_FLY_CLIP = "animation";
    public static final String OCTOPUS_LAND_CLIP = "animation2";
    public static final String OCTOPUS_IDLE_CLIP = "animation3";
    public static final String SHEEP_PATH = "768/FULL/EFFECTS/DARK_WIZARD_SHEEPENING/DARK_WIZARD_SHEEPENING.PAM";
    public static final String SHEEP_INTRO_CLIP = "animation";
    public static final String SHEEP_IDLE_CLIP = "idle";
    public static final String GRAVE_BUSTER_PATH = "768/INITIAL/PLANT/GRAVEBUSTER/GRAVEBUSTER.PAM";
    public static final String GRAVE_BUSTER_DIRT_PATH = "768/INITIAL/EFFECTS/GRAVEBUSTER_DIRT/GRAVEBUSTER_DIRT.PAM";
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

    public EntityAnimationCatalog.ClipSpec resting(Plant plant) {
        if (plant == null) {
            return catalog.plantIdle(null);
        }
        if (isWall(plant.getName())) {
            return wallClip(plant);
        }
        if (plant.isPeaPod()) {
            return peaPodIdle(plant);
        }
        if (hasGrowthClips(plant.getName())) {
            return growthIdle(plant);
        }
        return idle(plant.getName());
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

    public EntityAnimationCatalog.ClipSpec action(Plant plant) {
        if (plant.isPeaPod()) {
            return peaPodAttack(plant);
        }
        if (plant.isFumeShroom()) {
            return catalog.plantClip(plant.getName(), "special", "attack");
        }
        if (plant.isChomper()) {
            return switch (plant.chomperPhase()) {
                case BITE, BITE_END -> ChomperClips.bite();
                default -> ChomperClips.swallow();
            };
        }
        String name = plant.getName();
        int stage = Math.max(1, plant.getGrowthStage() + 1);
        return catalog.plantClip(name,
                "attack",
                "special",
                "special_stage" + stage,
                "bite",
                "attack1");
    }

    public EntityAnimationCatalog.ClipSpec plantFood(Plant plant) {
        if (plant.isFumeShroom()) {
            return catalog.plantClip(plant.getName(), "plantfood", "special", "idle");
        }
        if (plant.isPhatBeet()) {
            return PhatBeetClips.plantFood();
        }
        if (plant.isChomper()) {
            return switch (plant.chomperPhase()) {
                case PF_PULL -> ChomperClips.plantFood();
                case PF_OFF -> ChomperClips.plantFoodOff();
                case PF_BURP -> ChomperClips.plantFoodBurp();
                case PF_BURP_END -> ChomperClips.plantFoodBurpEnd();
                default -> ChomperClips.plantFoodOn();
            };
        }
        if (plant.isCabbagePult() || plant.isKernelPult()
                || plant.isMelonPult() || plant.isWinterMelon()
                || plant.isPepperPult()) {
            return catalog.plantClip(plant.getName(), "plantfood", "idle");
        }
        PeaPodPlantFood.Phase phase = plant.plantFoodPhase();
        if (phase == PeaPodPlantFood.Phase.ON) {
            return catalog.plantClip(plant.getName(), "plantfood_on", "plantfood", "idle");
        }
        if (phase == PeaPodPlantFood.Phase.LOOP) {
            return catalog.plantClip(plant.getName(), "plantfood", "plantfood_on", "idle");
        }
        if (phase == PeaPodPlantFood.Phase.OFF) {
            return catalog.plantClip(plant.getName(), "plantfood_off", "plantfood", "idle");
        }
        return resting(plant);
    }

    public EntityAnimationCatalog.ClipSpec potatoMineIntro(Plant plant) {
        return PotatoMineClips.intro(catalog, plant.getName());
    }

    public EntityAnimationCatalog.ClipSpec potatoMineCloneIntro(Plant plant) {
        return PotatoMineClips.cloneIntro(catalog, plant.getName());
    }

    public EntityAnimationCatalog.ClipSpec potatoMineUnarmedIdle(Plant plant) {
        return PotatoMineClips.unarmedIdle(catalog, plant.getName());
    }

    public EntityAnimationCatalog.ClipSpec potatoMineRecover(Plant plant) {
        return PotatoMineClips.recover(catalog, plant.getName());
    }

    public EntityAnimationCatalog.ClipSpec potatoMineArmedIdle(Plant plant) {
        return PotatoMineClips.armedIdle(catalog, plant.getName());
    }

    public EntityAnimationCatalog.ClipSpec potatoMineAttack(Plant plant) {
        return PotatoMineClips.attack(catalog, plant.getName());
    }

    public EntityAnimationCatalog.ClipSpec potatoMinePlantFood(Plant plant) {
        return PotatoMineClips.plantFoodPhase(catalog, plant, plant.potatoMinePlantFoodPhase());
    }

    public EntityAnimationCatalog.ClipSpec potatoMinePlantFoodOff(Plant plant) {
        return PotatoMineClips.plantFoodPhase(catalog, plant, PotatoMinePlantFood.Phase.OFF);
    }

    public EntityAnimationCatalog.ClipSpec cherryBombIdle() {
        return CherryBombClips.idle(catalog);
    }

    public EntityAnimationCatalog.ClipSpec cherryBombAttack() {
        return CherryBombClips.attack(catalog);
    }

    public EntityAnimationCatalog.ClipSpec grapeshotIdle() {
        return GrapeshotClips.idle(catalog);
    }

    public EntityAnimationCatalog.ClipSpec grapeshotAttack(Plant plant) {
        return GrapeshotClips.attack(catalog, plant);
    }

    public EntityAnimationCatalog.ClipSpec jalapenoIdle() {
        return JalapenoClips.idle(catalog);
    }

    public EntityAnimationCatalog.ClipSpec jalapenoAttack() {
        return JalapenoClips.attack(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashIdle() {
        return SquashClips.idle(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashJumpUpRight() {
        return SquashClips.jumpUpRight(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashJumpDownRight() {
        return SquashClips.jumpDownRight(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashPlantFoodJumpDownRight() {
        return SquashClips.plantFoodJumpDownRight(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashTurn() {
        return SquashClips.turn(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashJumpUpLeft() {
        return SquashClips.jumpUpLeft(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashJumpDownLeft() {
        return SquashClips.jumpDownLeft(catalog);
    }

    public EntityAnimationCatalog.ClipSpec squashPlantFoodJumpDownLeft() {
        return SquashClips.plantFoodJumpDownLeft(catalog);
    }

    public EntityAnimationCatalog.ClipSpec charge(String plantName) {
        return catalog.plantClip(plantName, "charge", "idle");
    }

    public boolean hasAction(Plant plant) {
        if (plant == null) {
            return false;
        }
        String path = catalog.plantIdle(plant.getName()).path();
        int stage = Math.max(1, plant.getGrowthStage() + 1);
        return catalog.hasClip(path, "attack")
                || catalog.hasClip(path, "special")
                || catalog.hasClip(path, "special_stage" + stage)
                || catalog.hasClip(path, "bite")
                || catalog.hasClip(path, "attack1");
    }

    public boolean hasCharge(String plantName) {
        return catalog.hasClip(catalog.plantIdle(plantName).path(), "charge");
    }

    public boolean hasGrowthClips(String plantName) {
        return catalog.hasClip(catalog.plantIdle(plantName).path(), "idle_stage1");
    }

    public EntityAnimationCatalog.ClipSpec clip(String plantName, String... preferredClips) {
        return catalog.plantClip(plantName, preferredClips);
    }

    public float scale(String plantName) {
        if ("Giant Wall-nut".equals(plantName)) {
            return LawnLayout.GIANT_WALLNUT_SCALE;
        }
        if ("Squash".equals(plantName)) {
            return LawnLayout.SQUASH_SCALE;
        }
        return LawnLayout.PLANT_SCALE;
    }

    public static boolean isWall(String plantName) {
        return "Wall-nut".equals(plantName)
                || "Tall-nut".equals(plantName)
                || "Endurian".equals(plantName)
                || "Giant Wall-nut".equals(plantName);
    }

    private EntityAnimationCatalog.ClipSpec peaPodIdle(Plant plant) {
        int heads = peaPodHeads(plant);
        if (heads == 1) {
            return catalog.plantClip(plant.getName(), "idle");
        }
        return catalog.plantClip(plant.getName(), "idle" + heads, "idle");
    }

    private EntityAnimationCatalog.ClipSpec peaPodAttack(Plant plant) {
        int heads = peaPodHeads(plant);
        if (heads == 1) {
            return catalog.plantClip(plant.getName(), "attack");
        }
        return catalog.plantClip(plant.getName(), "attack " + heads, "attack");
    }

    private static int peaPodHeads(Plant plant) {
        return Math.max(1, Math.min(Plant.MAX_PEA_POD_STACK, plant.getStackCount()));
    }

    public EntityAnimationCatalog.ClipSpec doomShroomStage1Spawn() {
        return DoomShroomClips.stage1Spawn();
    }

    public EntityAnimationCatalog.ClipSpec doomShroomIdle(Plant plant) {
        int stage = plant == null ? 0 : plant.getDoomShroomGrowthStage();
        boolean alert = plant != null && plant.isDoomShroomProximityAlert();
        return DoomShroomClips.idle(stage, alert);
    }

    public EntityAnimationCatalog.ClipSpec doomShroomExplode(int growthStage) {
        return DoomShroomClips.explode(growthStage);
    }

    public EntityAnimationCatalog.ClipSpec doomShroomTransform(int fromStage) {
        return DoomShroomClips.transform(fromStage);
    }

    public EntityAnimationCatalog.ClipSpec tangleKelpIdle() {
        return TangleKelpClips.idle();
    }

    public EntityAnimationCatalog.ClipSpec icebergLettuceIdle() {
        return IcebergLettuceClips.idle();
    }

    public EntityAnimationCatalog.ClipSpec bonkChoyIdle() {
        return BonkChoyClips.idle();
    }

    public EntityAnimationCatalog.ClipSpec wasabiWhipIdle() {
        return WasabiWhipClips.idle();
    }

    public EntityAnimationCatalog.ClipSpec kiwibeastIdle(Plant plant) {
        int stage = plant == null ? 1 : plant.kiwibeastStage();
        return KiwibeastClips.idle(stage);
    }

    public EntityAnimationCatalog.ClipSpec wallNutIdle(Plant plant) {
        if (plant != null && plant.hasSmashArmor()) {
            return WallNutClips.plantFoodLoop(Math.max(1, plant.wallNutArmorStage()));
        }
        int stage = plant == null ? 0 : plant.wallNutDamageStage();
        if (stage <= 0) {
            return WallNutClips.idle();
        }
        return WallNutClips.damage(stage);
    }

    public EntityAnimationCatalog.ClipSpec tallNutIdle(Plant plant) {
        int stage = plant == null ? 0 : plant.tallNutDamageStage();
        if (stage <= 0) {
            return TallNutClips.idle();
        }
        return TallNutClips.damage(stage);
    }

    public EntityAnimationCatalog.ClipSpec endurianIdle(Plant plant) {
        int stage = plant == null ? 0 : plant.endurianDamageStage();
        return EndurianClips.bodyIdle(stage);
    }

    public EntityAnimationCatalog.ClipSpec phatBeetIdle() {
        return PhatBeetClips.idle();
    }

    public EntityAnimationCatalog.ClipSpec chomperIdle() {
        return ChomperClips.idle();
    }

    private EntityAnimationCatalog.ClipSpec growthIdle(Plant plant) {
        int stage = Math.max(1, plant.getGrowthStage() + 1);
        return catalog.plantClip(plant.getName(),
                "idle_stage" + stage,
                "idle_stage1",
                "idle");
    }

    private EntityAnimationCatalog.ClipSpec wallClip(Plant plant) {
        float ratio = plant.getMaxHealth() <= 0
                ? 0f
                : plant.getHealth() / (float) plant.getMaxHealth();
        if (ratio > 0.75f) {
            return catalog.plantClip(plant.getName(), "idle", "idle2");
        }
        if (ratio > 0.5f) {
            return catalog.plantClip(plant.getName(), "damage", "idle");
        }
        if (ratio > 0.25f) {
            return catalog.plantClip(plant.getName(), "damage2", "damage", "idle");
        }
        return catalog.plantClip(plant.getName(), "damage3", "damage2", "damage", "idle");
    }
}
