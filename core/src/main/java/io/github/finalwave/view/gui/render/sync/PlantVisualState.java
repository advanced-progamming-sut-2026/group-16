package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.plant.ability.BowlingBulbAbility;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.PlantClips;

import java.util.List;


public final class PlantVisualState {
    private PlantVisualState() {
    }

    public static EntityAnimationCatalog.ClipSpec clip(Plant plant, PlantClips clips, boolean justFired) {
        return clip(plant, clips, justFired, List.of(), null, null);
    }

    public static EntityAnimationCatalog.ClipSpec clip(
            Plant plant, PlantClips clips, boolean justFired, GameSession session) {
        List<Zombie> zombies = session == null ? List.of() : session.getZombies();
        return clip(plant, clips, justFired, zombies, null, session);
    }

    public static EntityAnimationCatalog.ClipSpec clip(
            Plant plant, PlantClips clips, boolean justFired, List<Zombie> zombies, String currentClip) {
        return clip(plant, clips, justFired, zombies, currentClip, null);
    }

    public static EntityAnimationCatalog.ClipSpec clip(
            Plant plant, PlantClips clips, boolean justFired, List<Zombie> zombies, String currentClip,
            GameSession session) {
        EntityAnimationCatalog.ClipSpec specialized = specializedClip(plant, clips, justFired, session);
        if (specialized != null) {
            return specialized;
        }
        String[] names = preferredClips(plant, justFired, cactusDown(plant, zombies), currentClip, zombies);
        return clips.clip(pamName(plant), names);
    }

    public static EntityAnimationCatalog.ClipSpec idle(Plant plant, PlantClips clips) {
        return clips.clip(pamName(plant), idleNames(plant));
    }

    private static String pamName(Plant plant) {
        return pamNameForRender(plant);
    }

    public static String pamNameForRender(Plant plant) {
        if (plant == null) {
            return "";
        }
        if ("Imitater".equals(plant.getName()) && plant.getImitaterMorphTicks() > 0) {
            return "Imitater";
        }
        return plant.getName();
    }

    public static boolean isOneShot(EntityAnimationCatalog.ClipSpec spec) {
        return spec != null && isOneShotClip(spec.clip());
    }

    public static boolean isOneShot(Plant plant, EntityAnimationCatalog.ClipSpec spec) {
        if (plant != null && "Cactus".equals(plant.getName()) && plant.isUsingPlantFood()
                && spec != null && "plantfood".equals(spec.clip())) {
            return true;
        }
        if (plant != null && "Sea-shroom".equals(plant.getName()) && plant.isUsingPlantFood()
                && spec != null && "pf".equals(spec.clip())) {
            return true;
        }
        if (plant != null && "Grave Buster".equals(plant.getName()) && spec != null) {
            return "attack".equals(spec.clip());
        }
        return isOneShot(spec);
    }

    public static boolean isOneShotClip(String clipName) {
        if (clipName == null || clipName.isBlank()) {
            return false;
        }
        if ("down".equals(clipName) || "down_attack".equals(clipName)) {
            return true;
        }
        if (clipName.startsWith("growth_stage")) {
            return true;
        }
        if (clipName.startsWith("plantfood_on")
                || clipName.startsWith("plantfood_off")
                || clipName.startsWith("plantfood_stage")
                || clipName.startsWith("plantfood_start")
                || clipName.startsWith("plantfood_end")
                || "plantfood2".equals(clipName)
                || "pf".equals(clipName)) {
            return true;
        }
        if (clipName.startsWith("special") || clipName.startsWith("reload")) {
            return true;
        }
        if ("attack1".equals(clipName)) {
            return false;
        }
        return clipName.equals("attack") || clipName.startsWith("attack");
    }

    public static boolean isAttack(EntityAnimationCatalog.ClipSpec spec) {
        return isOneShot(spec);
    }

    public static boolean isAttackClip(String clipName) {
        return isOneShotClip(clipName);
    }

    public static String[] preferredClips(
            Plant plant, boolean justFired, boolean cactusDown, String currentClip, List<Zombie> zombies) {
        if (plant.hasTag(PlantTag.TRAP) && !plant.isArmedTrap()) {
            return new String[]{"plant_idle", "plant", "idle"};
        }
        if ("Gold Bloom".equals(plant.getName()) && plant.isAttacking()) {
            return new String[]{"attack", "idle"};
        }
        if ("Hot Potato".equals(plant.getName()) && plant.isAttacking()) {
            return new String[]{"attack", "idle"};
        }
        if ("Imitater".equals(plant.getName()) && plant.getImitaterMorphTicks() > 0) {
            return new String[]{"attack", "idle"};
        }
        if ("Magnet-shroom".equals(plant.getName())) {
            if (plant.getMagnetStealAnimTicks() > 0) {
                return new String[]{"catch", "idle"};
            }
            if (plant.getMagnetHeldMetalTicks() > 0) {
                return new String[]{"special", "idle"};
            }
            if (plant.getMagnetBusyTicks() > 0) {
                return new String[]{"busy", "idle2", "idle"};
            }
        }
        if ("Ice-shroom".equals(plant.getName()) && plant.getIceShroomAttackTicks() > 0) {
            return new String[]{"attack", "idle"};
        }
        if ("Grave Buster".equals(plant.getName())) {
            if (plant.isAttacking()) {
                return new String[]{"attack", "attack1"};
            }
            if (plant.isGraveBusting()) {
                return new String[]{"attack1"};
            }
        }
        if (plant.isUsingPlantFood()) {
            return plantFoodNames(plant);
        }
        if ("Mega Gatling Pea".equals(plant.getName()) && plant.isAttacking()
                && currentClip != null && !isAttackClip(currentClip)) {
            justFired = false;
        }
        if ("Citron".equals(plant.getName()) && plant.getRecoveryTicksRemaining() > 0) {
            return new String[]{"recovery", "idle"};
        }
        if ("Bowling Bulb".equals(plant.getName()) && plant.isBowlingReloading()) {
            return new String[]{
                    BowlingBulbAbility.reloadClipForAmmo(plant.getBowlingAmmo()),
                    "reload",
                    "idle"
            };
        }
        if (plant.isGrowing() && plant.hasTag(PlantTag.WARM_UP)) {
            return new String[]{"growth_stage" + plant.getGrowthStage(), "idle_stage" + plant.pamStage()};
        }
        if (plant.isProducingSun()) {
            if ("Sun-shroom".equals(plant.getName())) {
                return new String[]{"special_stage" + plant.pamStage(), "special", "idle_stage" + plant.pamStage()};
            }
            return new String[]{"special", "idle"};
        }
        if (cactusDown) {
            boolean inDown = currentClip != null && currentClip.startsWith("down");
            if (justFired || plant.isAttacking()) {
                return new String[]{"down_attack", "down_idle", "down"};
            }
            if (!inDown) {
                return new String[]{"down", "down_idle"};
            }
            return new String[]{"down_idle", "down"};
        }
        if ((justFired || plant.isAttacking()) && !plant.isDisabled() && !plant.isCatTransformed()) {
            if ("Split Pea".equals(plant.getName())
                    && plant.getSplitFireVisual() != Plant.SplitFireVisual.NONE) {
                return splitPeaFireNames(plant);
            }
            return fireNames(plant, hasAhead(plant, zombies), hasBehind(plant, zombies));
        }
        if (plant.hasTag(PlantTag.CHARGE) && plant.getChargeTicksRemaining() > 0) {
            return new String[]{"charge", "idle"};
        }
        return idleNames(plant);
    }

    public static String[] preferredClips(Plant plant, boolean justFired, boolean cactusDown, String currentClip) {
        return preferredClips(plant, justFired, cactusDown, currentClip, List.of());
    }

    private static String[] fireNames(Plant plant, boolean ahead, boolean behind) {
        String name = plant.getName();
        if (Plant.isPeaPod(name)) {
            int stack = plant.getStackCount();
            if (stack > 1) {
                return new String[]{"attack " + stack, "attack", "idle" + stack};
            }
            return new String[]{"attack", "idle"};
        }
        if ("Split Pea".equals(name)) {
            return splitPeaFireNames(plant);
        }
        if ("Puff-shroom".equals(name)) {
            return new String[]{"special_stage" + plant.pamStage(), "special", "idle_stage" + plant.pamStage()};
        }
        if ("Bowling Bulb".equals(name)) {
            return new String[]{
                    BowlingBulbAbility.fireClipForAmmo(plant.getBowlingAmmo()),
                    "attack",
                    "idle"
            };
        }
        if ("Mega Gatling Pea".equals(name) && plant.isMegaGatlingBoosted()) {
            return new String[]{"attack_stage2", "attack", "idle_stage2", "idle"};
        }
        if ("Starfruit".equals(name)) {
            return new String[]{"attack", "idle"};
        }
        if ("Sea-shroom".equals(name)) {
            return new String[]{"attack", "idle", "idle2"};
        }
        if ("Citron".equals(name) || "Caulipower".equals(name) || "Electric Blueberry".equals(name)) {
            return new String[]{"attack", "idle"};
        }
        return new String[]{"attack", "special", "idle"};
    }

    private static String[] splitPeaFireNames(Plant plant) {
        return switch (plant.getSplitFireVisual()) {
            case BACKWARD -> new String[]{"attack3", "idle"};
            case FORWARD -> new String[]{"attack", "idle"};
            case BOTH -> new String[]{"attack2", "idle"};
            default -> new String[]{"idle"};
        };
    }

    private static String[] plantFoodNames(Plant plant) {
        String name = plant.getName();
        if ("Sun-shroom".equals(name)) {
            int stage = plant.maxGrowthStage() + 1;
            return new String[]{"plantfood_stage" + stage, "idle_stage" + stage};
        }
        if ("Rotobaga".equals(name)) {
            return new String[]{"plantfood_on", "attack", "idle"};
        }
        if ("Repeater".equals(name)) {
            if (plant.isPlantFoodFinale()) {
                return new String[]{"plantfood2", "plantfood", "idle"};
            }
            return new String[]{"plantfood", "plantfood2", "idle"};
        }
        if ("Snow Pea".equals(name)) {
            if (plant.isPlantFoodOutro()) {
                return new String[]{"plantfood_off", "plantfood", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood", "idle"};
            }
            return new String[]{"plantfood", "plantfood_on", "idle"};
        }
        if ("Split Pea".equals(name)) {
            return new String[]{"plantfood", "idle"};
        }
        if ("Citron".equals(name) || "Cactus".equals(name)) {
            return new String[]{"plantfood", "idle"};
        }
        if ("Caulipower".equals(name)) {
            if (plant.isPlantFoodOutro()) {
                return new String[]{"plantfood_end", "plantfood_loop", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_start", "plantfood_loop", "idle"};
            }
            return new String[]{"plantfood_loop", "plantfood_loop2", "idle"};
        }
        if ("Electric Blueberry".equals(name)) {
            return new String[]{"plantfood", "idle"};
        }
        if ("Bowling Bulb".equals(name)) {
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood_idle", "idle"};
            }
            return new String[]{"plantfood_idle", "plantfood1", "idle"};
        }
        if ("Fire Peashooter".equals(name)) {
            if (plant.isPlantFoodFinale()) {
                return new String[]{"plantfood_end", "plantfood_loop", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood", "plantfood_loop", "idle"};
            }
            return new String[]{"plantfood_loop", "plantfood", "idle"};
        }
        if ("Starfruit".equals(name)) {
            if (plant.isPlantFoodOutro()) {
                return new String[]{"plantfood_off", "plantfood", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood", "idle"};
            }
            return new String[]{"plantfood", "plantfood_on", "idle"};
        }
        if ("Goo Peashooter".equals(name)) {
            return new String[]{"plantfood", "idle"};
        }
        if ("Mega Gatling Pea".equals(name)) {
            return new String[]{"plantfood", "idle_stage2", "idle"};
        }
        if ("Sea-shroom".equals(name)) {
            return new String[]{"pf", "idle", "idle2"};
        }
        if ("Explode-o-nut".equals(name)) {
            if (plant.isPlantFoodOutro()) {
                return new String[]{"plantfood_off", "plantfood", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood", "idle"};
            }
            return new String[]{"plantfood", "plantfood_on", "idle"};
        }
        if ("Magnet-shroom".equals(name)) {
            if (plant.isPlantFoodOutro()) {
                return new String[]{"plantfood_off", "plantfood", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood_collection", "idle"};
            }
            return new String[]{"plantfood_collection", "plantfood", "idle"};
        }
        if ("Torchwood".equals(name)) {
            if (plant.isTorchwoodBoosted()) {
                if (plant.isPlantFoodIntro()) {
                    return new String[]{"plantfood_on_t2", "plantfood_t2", "idle"};
                }
                return new String[]{"plantfood_t2", "plantfood_on_t2", "idle"};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood", "idle"};
            }
            return new String[]{"plantfood", "plantfood_on", "idle"};
        }
        if ("Lily Pad".equals(name)) {
            return new String[]{"plantfood", "idle"};
        }
        if ("Puff-shroom".equals(name)) {
            if (plant.isPlantFoodOutro()) {
                return new String[]{"plantfood_off", "plantfood", "idle_stage" + plant.pamStage()};
            }
            if (plant.isPlantFoodIntro()) {
                return new String[]{"plantfood_on", "plantfood", "idle_stage" + plant.pamStage()};
            }
            return new String[]{"plantfood", "plantfood_on", "idle_stage" + plant.pamStage()};
        }
        if (plant.isPlantFoodOutro()) {
            return new String[]{"plantfood_off", "plantfood", "idle"};
        }
        if (plant.isPlantFoodIntro()) {
            return new String[]{"plantfood_on", "plantfood", "idle"};
        }
        return new String[]{"plantfood", "plantfood_on", "idle"};
    }

    public static String[] idleNames(Plant plant) {
        String name = plant.getName();
        if ("Sun-shroom".equals(name) || "Puff-shroom".equals(name)) {
            return new String[]{"idle_stage" + plant.pamStage(), "idle"};
        }
        if (Plant.isPeaPod(name) && plant.getStackCount() > 1) {
            return new String[]{"idle" + plant.getStackCount(), "idle"};
        }
        if ("Caulipower".equals(name)) {
            return new String[]{"idle" + plant.getVisualIdleVariant() + "_1", "idle1_1", "idle"};
        }
        if ("Electric Blueberry".equals(name)) {
            int variant = plant.getVisualIdleVariant();
            return new String[]{"idle" + variant + "_1", "idle1_1", "idle"};
        }
        if ("Cactus".equals(name)) {
            return new String[]{"idle", "idle2", "idle3"};
        }
        if ("Mega Gatling Pea".equals(name) && plant.isMegaGatlingBoosted()) {
            return new String[]{"idle_stage2", "idle"};
        }
        if ("Sea-shroom".equals(name)) {
            return new String[]{"idle", "idle2"};
        }
        if ("Garlic".equals(name) || "Sweet Potato".equals(name)) {
            return new String[]{WallDamageSupport.idleDamageClip(plant), "idle", "idle2"};
        }
        if ("Explode-o-nut".equals(name)) {
            return new String[]{WallDamageSupport.damageClip(plant), "idle"};
        }
        if ("Pumpkin".equals(name)) {
            if (plant.getPumpkinShellTier() > 0) {
                int tier = Math.min(4, plant.getPumpkinShellTier());
                return new String[]{"idle_plantfood" + tier, "idle_plantfood", "idle"};
            }
            int variant = Math.min(3, 1 + WallDamageSupport.damageTier(plant));
            return new String[]{"idle" + variant, "idle"};
        }
        if ("Magnet-shroom".equals(name)) {
            if (plant.getMagnetStealAnimTicks() > 0) {
                return new String[]{"catch", "idle"};
            }
            if (plant.getMagnetHeldMetalTicks() > 0) {
                return new String[]{"special", "idle"};
            }
            if (plant.getMagnetBusyTicks() > 0) {
                return new String[]{"busy", "idle2", "idle"};
            }
            return new String[]{"idle", "idle2"};
        }
        if ("Ice-shroom".equals(plant.getName()) && plant.getIceShroomAttackTicks() > 0) {
            return new String[]{"attack", "idle"};
        }
        if ("Imitater".equals(name) && plant.getImitaterMorphTicks() > 0) {
            return new String[]{"attack", "idle"};
        }
        if ("Lily Pad".equals(name)) {
            int variant = Math.max(1, Math.min(5, plant.getVisualIdleVariant()));
            return new String[]{"idle" + variant, "idle"};
        }
        if ("Grave Buster".equals(name)) {
            return new String[]{"attack1"};
        }
        return new String[]{"idle1_1", "idle"};
    }

    public static boolean cactusDown(Plant plant, List<Zombie> zombies) {
        if (plant == null || !"Cactus".equals(plant.getName()) || zombies == null) {
            return false;
        }
        int col = plant.getCol();
        int row = plant.getRow();
        for (Zombie zombie : zombies) {
            if (zombie == null || !zombie.isAlive()) {
                continue;
            }
            if (!zombie.occupiesRow(row)) {
                continue;
            }
            if ((int) Math.floor(zombie.getX()) == col) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAhead(Plant plant, List<Zombie> zombies) {
        return hasNeighbor(plant, zombies, true);
    }

    private static boolean hasBehind(Plant plant, List<Zombie> zombies) {
        return hasNeighbor(plant, zombies, false);
    }

    private static boolean hasNeighbor(Plant plant, List<Zombie> zombies, boolean ahead) {
        if (zombies == null) {
            return false;
        }
        for (Zombie zombie : zombies) {
            if (zombie == null || !zombie.isAlive() || zombie.isHypnotized()
                    || !zombie.occupiesRow(plant.getRow())) {
                continue;
            }
            if (ahead && zombie.getX() >= plant.getCol()) {
                return true;
            }
            if (!ahead && zombie.getX() < plant.getCol()) {
                return true;
            }
        }
        return false;
    }

    private static EntityAnimationCatalog.ClipSpec specializedClip(
            Plant plant, PlantClips clips, boolean justFired, GameSession session) {
        if (plant == null) {
            return null;
        }
        if (plant.isPotatoMine()) {
            return potatoMineClip(plant, clips, session);
        }
        if (plant.isSquash()) {
            return clips.squashIdle();
        }
        if (plant.isTangleKelp()) {
            return clips.tangleKelpIdle();
        }
        if (plant.isIcebergLettuce()) {
            return clips.icebergLettuceIdle();
        }
        if (plant.isBonkChoy()) {
            return clips.bonkChoyIdle();
        }
        if (plant.isWasabiWhip()) {
            return clips.wasabiWhipIdle();
        }
        if (plant.isKiwibeast()) {
            return clips.kiwibeastIdle(plant);
        }
        if (plant.isWallNut()) {
            return clips.wallNutIdle(plant);
        }
        if (plant.isTallNut()) {
            return clips.tallNutIdle(plant);
        }
        if (plant.isEndurian()) {
            return clips.endurianIdle(plant);
        }
        if (plant.isChomper() && !plant.isAttacking() && !plant.isPlantFooding()) {
            return clips.chomperIdle();
        }
        if (plant.isPlantFooding() && usesBranchPlantFood(plant)) {
            return clips.plantFood(plant);
        }
        if ((justFired || plant.isAttacking())
                && !plant.isDisabled()
                && !plant.isCatTransformed()
                && usesBranchAction(plant)
                && clips.hasAction(plant)) {
            return clips.action(plant);
        }
        return null;
    }

    private static boolean usesBranchPlantFood(Plant plant) {
        return plant.isFumeShroom()
                || plant.isPhatBeet()
                || plant.isChomper()
                || plant.isCabbagePult()
                || plant.isKernelPult()
                || plant.isMelonPult()
                || plant.isWinterMelon()
                || plant.isPepperPult()
                || plant.isPeaPod();
    }

    private static boolean usesBranchAction(Plant plant) {
        return plant.isFumeShroom()
                || plant.isChomper()
                || plant.isPeaPod()
                || plant.isCabbagePult()
                || plant.isKernelPult()
                || plant.isMelonPult()
                || plant.isWinterMelon()
                || plant.isPepperPult();
    }

    private static EntityAnimationCatalog.ClipSpec potatoMineClip(
            Plant plant, PlantClips clips, GameSession session) {
        if (plant.isPlantFooding()) {
            return clips.potatoMinePlantFood(plant);
        }
        if (plant.isAttacking()) {
            return clips.potatoMineAttack(plant);
        }
        if (plant.isArmedTrap()) {
            if (session != null && PotatoMineProximity.inRecoverRadius(session, plant)) {
                return clips.potatoMineArmedIdle(plant);
            }
            return clips.potatoMineUnarmedIdle(plant);
        }
        return clips.potatoMineUnarmedIdle(plant);
    }

    public static boolean isAction(EntityAnimationCatalog.ClipSpec spec) {
        return spec != null && isActionClip(spec.clip());
    }

    public static boolean isActionClip(String clipName) {
        if (clipName == null || clipName.isBlank()) {
            return false;
        }
        return clipName.startsWith("attack")
                || clipName.startsWith("bite")
                || clipName.startsWith("special")
                || "recover".equals(clipName)
                || clipName.startsWith("plantfood");
    }
}
