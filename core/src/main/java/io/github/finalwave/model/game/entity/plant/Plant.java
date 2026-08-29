package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.ability.PlantAbility;
import io.github.finalwave.model.game.entity.plant.ability.PlantAbilityFactory;
import io.github.finalwave.model.game.entity.plant.food.PlantFoodEffect;
import io.github.finalwave.model.game.entity.plant.food.PlantFoodEffectFactory;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public final class Plant extends Entity {

    private final PlantDefinition definition;
    private final PlantCategory category;
    private final Set<PlantTag> tags;
    private final PlantAbility ability;
    private final PlantFoodEffect foodEffect;

    private int level;
    private PlantStats stats;
    private double actionCooldownTicks;
    private final List<PlantArmor> armorLayers = new ArrayList<>();
    private GameContext lastContext;
    private final Set<String> disableSources = new HashSet<>();
    private int hostileIceHits;
    private String transformingWizardId;

    private int growthStage;
    private int growthTicksRemaining;
    private int chargeTicksRemaining;
    private int lifespanTicksRemaining = -1;
    private boolean armedTrap;
    private boolean attacking;
    private boolean graveBusting;
    private int producingSunTicks;
    private int growthAdvanceTicks;
    private int consumeDelayTicks;
    private int stackCount = 1;
    private int plantFoodTicksRemaining;
    private int plantFoodDurationTicks;
    private int plantFoodSetupTicks;
    private int plantFoodFinaleTicks;
    private int recoveryTicksRemaining;
    private int reloadTicksRemaining;
    private int bowlingAmmo = 1;
    private boolean bowlingReloading;
    private boolean megaGatlingBoosted;
    private int visualIdleVariant = 1;
    private SplitFireVisual splitFireVisual = SplitFireVisual.NONE;

    public enum SplitFireVisual {
        NONE,
        FORWARD,
        BACKWARD,
        BOTH
    }

    public Plant(String id, PlantDefinition definition, int level, int col, int row) {
        super(id, PlantStatsCalculator.compute(definition, level).maxHealth(), col, row);
        this.definition = definition;
        this.category = PlantCategory.fromDefinition(definition.getCategory());
        this.tags = resolveTags(definition);
        this.ability = PlantAbilityFactory.create(definition, category);
        this.foodEffect = PlantFoodEffectFactory.create(definition);
        this.level = level;
        this.stats = PlantStatsCalculator.compute(definition, level);
        this.actionCooldownTicks = stats.actionInterval() * 10;
    }

    private static Set<PlantTag> resolveTags(PlantDefinition definition) {
        Set<PlantTag> resolved = EnumSet.noneOf(PlantTag.class);
        for (String tag : definition.getTags()) {
            resolved.add(PlantTag.fromDefinition(tag));
        }
        return resolved;
    }

    public void initializeCooldown(int ticksPerSecond) {
        actionCooldownTicks = PlantBehaviorSupport.actionIntervalTicks(this, ticksPerSecond);
    }

    public void onPlanted(GameContext context) {
        lastContext = context;
        initializeCooldown(context.getTicksPerSecond());
        PlantBehaviorSupport.onPlanted(this, context, context.getTicksPerSecond());
        if ("Puff-shroom".equals(getName()) || "Sea-shroom".equals(getName())) {
            double lifespanSeconds = 60.0
                    + stats.specialModifier(PlantSpecialModifiers.LIFESPAN_EXT);
            lifespanTicksRemaining = (int) Math.ceil(
                    lifespanSeconds * context.getTicksPerSecond());
        }
        ability.onPlanted(this, context);
        if (isAlive() && shouldAutoActivatePlantFood()) {
            activatePlantFoodEffect(context);
        }
    }

    @Override
    public void onTickUpdate(GameContext context) {
        lastContext = context;
        if (producingSunTicks > 0) {
            producingSunTicks--;
        }
        if (growthAdvanceTicks > 0) {
            growthAdvanceTicks--;
        }
        if (consumeDelayTicks > 0) {
            consumeDelayTicks--;
            if (consumeDelayTicks == 0) {
                ability.onConsumeDelayFinished(this, context);
                return;
            }
        }
        if (plantFoodTicksRemaining > 0) {
            foodEffect.tick(this, context);
            plantFoodTicksRemaining--;
            if (plantFoodTicksRemaining == 0) {
                foodEffect.end(this, context);
            }
        }
        ability.onTick(this, context);
        if (isDead() || isDisabled() || stats.actionInterval() <= 0) {
            return;
        }
        if (isUsingPlantFood()) {
            PlantBehaviorSupport.tick(this, context.getTicksPerSecond());
            return;
        }
        PlantBehaviorSupport.tick(this, context.getTicksPerSecond());
        if (lifespanTicksRemaining > 0 && --lifespanTicksRemaining == 0) {
            consumeInstantly();
            return;
        }
        actionCooldownTicks -= 1;
        if (actionCooldownTicks <= 0) {
            if (PlantBehaviorSupport.canAct(this)) {
                if (ability.tryAction(this, context)) {
                    if (hasTag(PlantTag.CHARGE)) {
                        chargeTicksRemaining = (int) Math.ceil(
                                PlantBehaviorSupport.actionIntervalTicks(this, context.getTicksPerSecond()));
                        actionCooldownTicks = 0;
                    } else {
                        double interval = PlantBehaviorSupport.actionIntervalTicks(
                                this, context.getTicksPerSecond());
                        actionCooldownTicks = Math.max(1, interval - ability.actionWindupTicks());
                    }
                } else {
                    actionCooldownTicks = 0;
                }
            } else {
                actionCooldownTicks = PlantBehaviorSupport.actionIntervalTicks(
                        this, context.getTicksPerSecond());
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        int remaining = amount;
        for (PlantArmor armor : armorLayers) {
            if (remaining <= 0) {
                break;
            }
            if (!armor.isDestroyed()) {
                remaining = armor.absorb(remaining);
            }
        }
        if (remaining > 0) {
            super.takeDamage(remaining);
        }
    }

    public void activatePlantFoodEffect(GameContext context) {
        lastContext = context;
        foodEffect.apply(this, context);
    }

    private boolean shouldAutoActivatePlantFood() {
        if (stats.hasSpecialModifier(PlantSpecialModifiers.AUTO_PLANTFOOD_ON_ENTER)) {
            return true;
        }
        double chance = stats.specialModifier(PlantSpecialModifiers.AUTO_PLANT_FOOD_CHANCE);
        return chance > 0 && ThreadLocalRandom.current().nextDouble() < chance;
    }

    public void consumeInstantly() {
        takeDamage(getMaxHealth());
    }

    public void consumeAfter(int ticks) {
        consumeDelayTicks = Math.max(1, ticks);
    }

    public void beginSunProduce(int ticks) {
        producingSunTicks = Math.max(producingSunTicks, Math.max(1, ticks));
    }

    public void beginPlantFood(int durationTicks, int setupTicks) {
        beginPlantFood(durationTicks, setupTicks, 0);
    }

    public void beginPlantFood(int durationTicks, int setupTicks, int finaleTicks) {
        this.plantFoodDurationTicks = Math.max(1, durationTicks);
        this.plantFoodTicksRemaining = this.plantFoodDurationTicks;
        this.plantFoodSetupTicks = Math.max(0, setupTicks);
        this.plantFoodFinaleTicks = Math.max(0, finaleTicks);
    }

    public boolean isUsingPlantFood() {
        return plantFoodTicksRemaining > 0;
    }

    public boolean isPlantFoodIntro() {
        return isUsingPlantFood() && plantFoodElapsed() < plantFoodSetupTicks;
    }

    public boolean isPlantFoodFinale() {
        return isUsingPlantFood() && plantFoodFinaleTicks > 0
                && plantFoodTicksRemaining <= plantFoodFinaleTicks;
    }

    public boolean isPlantFoodOutro() {
        if (!isUsingPlantFood() || plantFoodSetupTicks <= 0) {
            return false;
        }
        int outro = Math.min(5, Math.max(1, plantFoodDurationTicks / 5));
        return plantFoodTicksRemaining <= outro;
    }

    public int plantFoodElapsed() {
        return plantFoodDurationTicks - plantFoodTicksRemaining;
    }

    public void growToMaxStage(int ticksPerSecond) {
        while (growthStage < maxGrowthStage()) {
            advanceGrowthStage(ticksPerSecond);
        }
    }

    public boolean isProducingSun() {
        return producingSunTicks > 0;
    }

    public boolean isGrowing() {
        return growthAdvanceTicks > 0;
    }

    public int getStackCount() {
        return stackCount;
    }

    public boolean addStack() {
        if (stackCount >= 5) {
            return false;
        }
        stackCount++;
        return true;
    }

    public static boolean isPeaPod(String name) {
        return "Pea Pod".equals(name);
    }

    public boolean upgrade() {
        int nextLevel = level + 1;
        if (nextLevel > definition.getMaxLevel()) {
            return false;
        }
        int oldMax = getMaxHealth();
        level = nextLevel;
        stats = PlantStatsCalculator.compute(definition, level);
        setMaxHealth(stats.maxHealth());
        heal(stats.maxHealth() - oldMax);
        return true;
    }

    public void grantArmor(int armorValue) {
        if (armorValue > 0) {
            armorLayers.add(new PlantArmor(armorValue));
        }
    }

    public List<PlantArmor> getArmorLayers() {
        return List.copyOf(armorLayers);
    }

    public boolean isArmedTrap() {
        return armedTrap;
    }

    public void setArmedTrap(boolean armedTrap) {
        this.armedTrap = armedTrap;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public boolean isGraveBusting() {
        return graveBusting;
    }

    public void setGraveBusting(boolean graveBusting) {
        this.graveBusting = graveBusting;
    }

    public ProjectileEffect projectileEffect() {
        String name = getName();
        if (name != null) {
            ProjectileEffect named = switch (name) {
                case "Pepper-pult" -> ProjectileEffect.PEPPER;
                case "Melon-pult" -> ProjectileEffect.MELON;
                case "Winter Melon" -> ProjectileEffect.WINTER_MELON;
                case "Cabbage-pult" -> ProjectileEffect.CABBAGE;
                case "Kernel-pult" -> ProjectileEffect.KERNEL;
                case "Fume-shroom" -> ProjectileEffect.FUME;
                case "Cactus" -> ProjectileEffect.SPIKE;
                case "Puff-shroom" -> ProjectileEffect.PUFF;
                case "Sea-shroom" -> ProjectileEffect.SEA_SHROOM;
                case "Goo Peashooter" -> ProjectileEffect.GOO;
                case "Mega Gatling Pea" -> ProjectileEffect.MEGA_GATLING_PEA;
                case "Starfruit" -> ProjectileEffect.STAR;
                case "Rotobaga" -> ProjectileEffect.ROTOBAGA;
                case "Citron" -> ProjectileEffect.PLASMA;
                case "Caulipower" -> ProjectileEffect.MAGIC_BEAM;
                case "Electric Blueberry" -> ProjectileEffect.LIGHTNING;
                default -> null;
            };
            if (named != null) {
                return named;
            }
        }
        if (hasTag(PlantTag.FIRE)) {
            return ProjectileEffect.FIRE;
        }
        if (hasTag(PlantTag.ICE)) {
            return ProjectileEffect.ICE;
        }
        if (hasTag(PlantTag.POISON)) {
            return ProjectileEffect.POISON;
        }
        if (hasTag(PlantTag.PEA)) {
            return ProjectileEffect.PEA;
        }
        return ProjectileEffect.GENERIC;
    }

    public int maxGrowthStage() {
        int bonus = (int) stats.specialModifier(PlantSpecialModifiers.GROWTH_STAGE_MAX_UP);
        return 2 + bonus;
    }

    public int getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(int growthStage) {
        this.growthStage = growthStage;
    }

    public int getGrowthTicksRemaining() {
        return growthTicksRemaining;
    }

    public void setGrowthTicksRemaining(int growthTicksRemaining) {
        this.growthTicksRemaining = growthTicksRemaining;
    }

    public void decrementGrowthTicks() {
        growthTicksRemaining--;
    }

    public void advanceGrowthStage(int ticksPerSecond) {
        growthStage++;
        growthAdvanceTicks = Math.max(growthAdvanceTicks, ticksPerSecond);
        if (growthStage < maxGrowthStage()) {
            growthTicksRemaining = (int) Math.ceil(
                    Math.max(0.1, stats.actionInterval()) * ticksPerSecond);
        }
    }

    public int pamStage() {
        return Math.max(1, growthStage + 1);
    }

    public int getChargeTicksRemaining() {
        return chargeTicksRemaining;
    }

    public void setChargeTicksRemaining(int chargeTicksRemaining) {
        this.chargeTicksRemaining = chargeTicksRemaining;
    }

    public void decrementChargeTicks() {
        chargeTicksRemaining--;
    }

    public int getRecoveryTicksRemaining() {
        return recoveryTicksRemaining;
    }

    public void setRecoveryTicksRemaining(int recoveryTicksRemaining) {
        this.recoveryTicksRemaining = Math.max(0, recoveryTicksRemaining);
    }

    public void decrementRecoveryTicks() {
        if (recoveryTicksRemaining > 0) {
            recoveryTicksRemaining--;
        }
    }

    public int getReloadTicksRemaining() {
        return reloadTicksRemaining;
    }

    public void setReloadTicksRemaining(int reloadTicksRemaining) {
        this.reloadTicksRemaining = Math.max(0, reloadTicksRemaining);
    }

    public void decrementReloadTicks() {
        if (reloadTicksRemaining > 0) {
            reloadTicksRemaining--;
        }
    }

    public int getBowlingAmmo() {
        return bowlingAmmo;
    }

    public void setBowlingAmmo(int bowlingAmmo) {
        this.bowlingAmmo = Math.max(1, Math.min(3, bowlingAmmo));
    }

    public boolean isBowlingReloading() {
        return bowlingReloading;
    }

    public void setBowlingReloading(boolean bowlingReloading) {
        this.bowlingReloading = bowlingReloading;
    }

    public boolean isMegaGatlingBoosted() {
        return megaGatlingBoosted;
    }

    public void setMegaGatlingBoosted(boolean megaGatlingBoosted) {
        this.megaGatlingBoosted = megaGatlingBoosted;
    }

    public void resetLifespanTicks(int ticks) {
        if (lifespanTicksRemaining >= 0) {
            lifespanTicksRemaining = Math.max(1, ticks);
        }
    }

    public void setLifespanTicks(int ticks) {
        lifespanTicksRemaining = Math.max(1, ticks);
    }

    public int getLifespanTicksRemaining() {
        return lifespanTicksRemaining;
    }

    public int getVisualIdleVariant() {
        return visualIdleVariant;
    }

    public void setVisualIdleVariant(int visualIdleVariant) {
        this.visualIdleVariant = Math.max(1, visualIdleVariant);
    }

    public void rotateVisualIdleVariant() {
        visualIdleVariant = visualIdleVariant >= 4 ? 1 : visualIdleVariant + 1;
    }

    public SplitFireVisual getSplitFireVisual() {
        return splitFireVisual;
    }

    public void setSplitFireVisual(SplitFireVisual splitFireVisual) {
        this.splitFireVisual = splitFireVisual == null ? SplitFireVisual.NONE : splitFireVisual;
    }

    public boolean hasTag(PlantTag tag) {
        return tags.contains(tag);
    }

    public void disable(String sourceId) {
        if (sourceId != null) {
            disableSources.add(sourceId);
        }
    }

    public void enable(String sourceId) {
        disableSources.remove(sourceId);
    }

    public boolean isDisabled() {
        return !disableSources.isEmpty();
    }

    public boolean isDisabledBy(String sourceId) {
        return disableSources.contains(sourceId);
    }

    public boolean transformIntoCat(String wizardId) {
        if (wizardId == null || wizardId.isBlank() || transformingWizardId != null) {
            return false;
        }
        transformingWizardId = wizardId;
        disable(wizardId);
        return true;
    }

    public boolean restoreFromCat(String wizardId) {
        if (wizardId == null || !wizardId.equals(transformingWizardId)) {
            return false;
        }
        enable(wizardId);
        transformingWizardId = null;
        return true;
    }

    public boolean isCatTransformed() {
        return transformingWizardId != null;
    }

    public boolean isCatTransformedBy(String wizardId) {
        return wizardId != null && wizardId.equals(transformingWizardId);
    }

    public boolean canBeTargetedByZombie() {
        return isAlive() && !isCatTransformed();
    }

    public int addHostileIceStack(String sourceId) {
        return ++hostileIceHits;
    }

    public int getHostileIceStacks(String sourceId) {
        return hostileIceHits;
    }

    public void clearHostileIce() {
        hostileIceHits = 0;
    }

    public String getName() {
        return definition.getName();
    }

    public int getLevel() {
        return level;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public Set<PlantTag> getTags() {
        return tags;
    }

    public PlantDefinition getDefinition() {
        return definition;
    }

    public PlantStats getStats() {
        return stats;
    }

    public PlantAbility getAbility() {
        return ability;
    }

    public int getCol() {
        return (int) getX();
    }

    public int getRow() {
        return (int) getY();
    }

    public void relocate(int col, int row) {
        setX(col);
        setY(row);
    }

    @Override
    protected void onDeath() {
        if (lastContext != null) {
            lastContext.onPlantDestroyed(this);
        }
    }
}
