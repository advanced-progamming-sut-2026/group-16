package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.ability.DoomShroomAbility;
import io.github.finalwave.model.game.entity.plant.ability.ExplosiveAbility;
import io.github.finalwave.model.game.entity.plant.ability.PlantAbility;
import io.github.finalwave.model.game.entity.plant.ability.PlantAbilityFactory;
import io.github.finalwave.model.game.entity.plant.ability.GrapeshotAbility;
import io.github.finalwave.model.game.entity.plant.ability.JalapenoAbility;
import io.github.finalwave.model.game.entity.plant.ability.BonkChoyAbility;
import io.github.finalwave.model.game.entity.plant.ability.WasabiWhipAbility;
import io.github.finalwave.model.game.entity.plant.ability.PhatBeetAbility;
import io.github.finalwave.model.game.entity.plant.ability.KiwibeastAbility;
import io.github.finalwave.model.game.entity.plant.ability.EndurianAbility;
import io.github.finalwave.model.game.entity.plant.ability.ChomperAbility;
import io.github.finalwave.model.game.entity.plant.ability.IcebergLettuceAbility;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpAbility;
import io.github.finalwave.model.game.entity.plant.ability.SquashAbility;
import io.github.finalwave.model.game.entity.plant.food.BonkChoyPlantFood;
import io.github.finalwave.model.game.entity.plant.food.WasabiWhipPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PhatBeetPlantFood;
import io.github.finalwave.model.game.entity.plant.food.KiwibeastPlantFood;
import io.github.finalwave.model.game.entity.plant.food.WallNutPlantFood;
import io.github.finalwave.model.game.entity.plant.food.EndurianPlantFood;
import io.github.finalwave.model.game.entity.plant.food.CabbagePultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.FumeShroomPlantFood;
import io.github.finalwave.model.game.entity.plant.food.KernelPultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.MelonPultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PeaPodPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PepperPultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PotatoMinePlantFood;
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

    public static final int MAX_PEA_POD_STACK = 5;
    public static final String PEA_POD = "Pea Pod";
    public static final String FUME_SHROOM = "Fume-shroom";
    public static final String CABBAGE_PULT = "Cabbage-pult";
    public static final String KERNEL_PULT = "Kernel-pult";
    public static final String MELON_PULT = "Melon-pult";
    public static final String WINTER_MELON = "Winter Melon";
    public static final String PEPPER_PULT = "Pepper-pult";
    public static final String POTATO_MINE = "Potato Mine";
    public static final String PRIMAL_POTATO_MINE = "Primal Potato Mine";
    public static final String CHERRY_BOMB = "Cherry Bomb";
    public static final String SQUASH = "Squash";
    public static final String GRAPESHOT = "Grapeshot";
    public static final String JALAPENO = "Jalapeno";
    public static final String DOOM_SHROOM = "Doom-shroom";
    public static final String TANGLE_KELP = "Tangle Kelp";
    public static final String ICEBERG_LETTUCE = "Iceberg Lettuce";
    public static final String BONK_CHOY = "Bonk Choy";
    public static final String WASABI_WHIP = "Wasabi Whip";
    public static final String PHAT_BEET = "Phat Beet";
    public static final String KIWIBEAST = "Kiwibeast";
    public static final String WALL_NUT = "Wall-nut";
    public static final String TALL_NUT = "Tall-nut";
    public static final String ENDURIAN = "Endurian";
    public static final String CHOMPER = "Chomper";

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
    private boolean torchwoodBoosted;
    private boolean sunBeanPowered;
    private int pumpkinShellTier;
    private boolean explodeONutPfArmor;
    private int magnetBusyTicks;
    private int magnetStealAnimTicks;
    private int magnetHeldMetalTicks;
    private String magnetHeldMetalAlias;
    private String imitatedPlantName;
    private int imitaterMorphTicks;
    private boolean imitaterCopy;
    private int iceShroomAttackTicks;
    private PlantFoodEffect imitaterFoodEffect;
    private int visualIdleVariant = 1;
    private SplitFireVisual splitFireVisual = SplitFireVisual.NONE;
    private boolean plantFoodSpawned;
    private final PeaPodPlantFood peaPodPlantFood = new PeaPodPlantFood();
    private final FumeShroomPlantFood fumeShroomPlantFood = new FumeShroomPlantFood();
    private final CabbagePultPlantFood cabbagePultPlantFood = new CabbagePultPlantFood();
    private final KernelPultPlantFood kernelPultPlantFood = new KernelPultPlantFood();
    private final MelonPultPlantFood melonPultPlantFood = new MelonPultPlantFood();
    private final PepperPultPlantFood pepperPultPlantFood = new PepperPultPlantFood();
    private final PotatoMinePlantFood potatoMinePlantFood = new PotatoMinePlantFood();
    private final BonkChoyPlantFood bonkChoyPlantFood = new BonkChoyPlantFood();
    private final WasabiWhipPlantFood wasabiWhipPlantFood = new WasabiWhipPlantFood();
    private final PhatBeetPlantFood phatBeetPlantFood = new PhatBeetPlantFood();
    private final KiwibeastPlantFood kiwibeastPlantFood = new KiwibeastPlantFood();
    private final WallNutPlantFood wallNutPlantFood = new WallNutPlantFood();
    private final EndurianPlantFood endurianPlantFood = new EndurianPlantFood();

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

    public void primeActionCooldown() {
        actionCooldownTicks = 1;
    }

    public void initializeAfterImitaterMorph(GameContext context) {
        lastContext = context;
        initializeCooldown(context.getTicksPerSecond());
        imitaterCopy = true;
        ability.onPlanted(this, context);
    }

    public void onPlanted(GameContext context) {
        lastContext = context;
        initializeCooldown(context.getTicksPerSecond());
        PlantBehaviorSupport.onPlanted(this, context, context.getTicksPerSecond());
        if (hasTag(PlantTag.CHARGE) && !"Bowling Bulb".equals(getName())) {
            actionCooldownTicks = 0;
        }
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
            activeFoodEffect().tick(this, context);
            plantFoodTicksRemaining--;
            if (plantFoodTicksRemaining == 0) {
                activeFoodEffect().end(this, context);
                imitaterFoodEffect = null;
            }
        }
        if (magnetBusyTicks > 0) {
            magnetBusyTicks--;
        }
        if (magnetStealAnimTicks > 0) {
            magnetStealAnimTicks--;
        }
        if (iceShroomAttackTicks > 0) {
            iceShroomAttackTicks--;
            if (iceShroomAttackTicks == 0) {
                setAttacking(false);
            }
        }
        if (imitaterMorphTicks > 0) {
            imitaterMorphTicks--;
            if (imitaterMorphTicks == 0) {
                setAttacking(false);
                if ("Imitater".equals(getName()) && imitatedPlantName != null && !imitatedPlantName.isBlank()) {
                    context.completeImitaterMorph(this);
                }
            }
        }
        peaPodPlantFood.tick(this, context);
        fumeShroomPlantFood.tick(this, context);
        cabbagePultPlantFood.tick(this, context);
        kernelPultPlantFood.tick(this, context);
        melonPultPlantFood.tick(this, context);
        pepperPultPlantFood.tick(this, context);
        potatoMinePlantFood.tick(this, context);
        bonkChoyPlantFood.tick(this, context);
        wasabiWhipPlantFood.tick(this, context);
        phatBeetPlantFood.tick(this, context);
        kiwibeastPlantFood.tick(this, context);
        wallNutPlantFood.tick(this, context);
        endurianPlantFood.tick(this, context);
        if (ability instanceof ExplosiveAbility explosive) {
            explosive.tickDetonation(this, context);
        }
        if (ability instanceof SquashAbility squash) {
            squash.tickSmash(this, context);
        }
        if (ability instanceof GrapeshotAbility grapeshot) {
            grapeshot.tick(this, context);
        }
        if (ability instanceof JalapenoAbility jalapeno) {
            jalapeno.tick(this, context);
        }
        if (ability instanceof DoomShroomAbility doomShroom) {
            doomShroom.tick(this, context);
        }
        if (ability instanceof TangleKelpAbility tangleKelp) {
            tangleKelp.tick(this, context);
        }
        if (ability instanceof IcebergLettuceAbility iceberg) {
            iceberg.tick(this, context);
        }
        if (ability instanceof ChomperAbility chomper) {
            chomper.tick(this, context);
        }
        ability.onTick(this, context);
        if (isDead()) {
            return;
        }
        if (isPlantFooding()) {
            PlantBehaviorSupport.tick(this, context.getTicksPerSecond());
            return;
        }
        if (ability instanceof ChomperAbility) {
            PlantBehaviorSupport.tick(this, context.getTicksPerSecond());
            return;
        }
        if (ability instanceof WasabiWhipAbility whip) {
            PlantBehaviorSupport.tick(this, context.getTicksPerSecond());
            if (!isDisabled() && PlantBehaviorSupport.canAct(this)) {
                whip.tryAction(this, context);
            }
            return;
        }
        if (isDisabled() || (stats.actionInterval() <= 0 && !isDoomShroom() && !isTangleKelp() && !isIcebergLettuce())) {
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
                        if ("Citron".equals(getName())) {
                            chargeTicksRemaining = 0;
                        } else {
                            chargeTicksRemaining = PlantBehaviorSupport.chargeTicks(
                                    this, context.getTicksPerSecond());
                        }
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
                actionCooldownTicks = 0;
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (amount <= 0) {
            return;
        }
        if (ability instanceof ExplosiveAbility explosive && explosive.isDetonating()) {
            return;
        }
        if (ability instanceof SquashAbility squash && squash.isSmashing()) {
            return;
        }
        if (ability instanceof DoomShroomAbility doomShroom && doomShroom.isDetonating()) {
            return;
        }
        if (ability instanceof TangleKelpAbility tangleKelp && tangleKelp.isGrabbing()) {
            return;
        }
        if (ability instanceof IcebergLettuceAbility iceberg && iceberg.isFreezing()) {
            return;
        }
        boolean hadPfArmor = hasExplodeONutPfArmor() && hasIntactArmor();
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
        if (hadPfArmor && hasExplodeONutPfArmor() && !hasIntactArmor() && lastContext != null) {
            lastContext.explode(this, getStats().damage(), 1.0);
            setExplodeONutPfArmor(false);
        }
        if (ability instanceof KiwibeastAbility kiwi) {
            kiwi.onDamaged(this, lastContext);
        }
        if (isDead() && ability instanceof ChomperAbility chomper) {
            chomper.releaseDragged(lastContext);
        }
    }

    private boolean hasIntactArmor() {
        for (PlantArmor armor : armorLayers) {
            if (!armor.isDestroyed()) {
                return true;
            }
        }
        return false;
    }

    public void activatePlantFoodEffect(GameContext context) {
        lastContext = context;
        if ("Imitater".equals(getName()) && imitatedPlantName != null && !imitatedPlantName.isBlank()) {
            PlantDefinition imitated = context.findPlantDefinition(imitatedPlantName);
            if (imitated != null) {
                imitaterFoodEffect = PlantFoodEffectFactory.create(imitated, imitatedPlantName);
                imitaterFoodEffect.apply(this, context);
                return;
            }
        }
        if (isPeaPod()) {
            peaPodPlantFood.start((int) Math.round(definition.getPlantFoodValue()));
            setAttacking(false);
            return;
        }
        if (isFumeShroom()) {
            fumeShroomPlantFood.start(this, context);
            setAttacking(false);
            return;
        }
        if (isCabbagePult()) {
            cabbagePultPlantFood.start((int) Math.round(definition.getPlantFoodValue()));
            setAttacking(false);
            return;
        }
        if (isKernelPult()) {
            kernelPultPlantFood.start((int) Math.round(definition.getPlantFoodValue()));
            setAttacking(false);
            return;
        }
        if (isMelonPult() || isWinterMelon()) {
            melonPultPlantFood.start((int) Math.round(definition.getPlantFoodValue()));
            setAttacking(false);
            return;
        }
        if (isPepperPult()) {
            pepperPultPlantFood.start((int) Math.round(definition.getPlantFoodValue()));
            setAttacking(false);
            return;
        }
        if (isPotatoMine()) {
            potatoMinePlantFood.start((int) Math.round(definition.getPlantFoodValue()));
            setAttacking(false);
            return;
        }
        if (isSquash() && ability instanceof SquashAbility squash) {
            squash.startPlantFoodSmash();
            setAttacking(false);
            return;
        }
        if (isDoomShroom()) {
            context.triggerDoomShroomPlantFood(this);
            return;
        }
        if (isTangleKelp() && ability instanceof TangleKelpAbility tangleKelp) {
            tangleKelp.startPlantFood(this, context);
            return;
        }
        if (isIcebergLettuce() && ability instanceof IcebergLettuceAbility iceberg) {
            iceberg.startPlantFood(this, context);
            return;
        }
        if (isBonkChoy()) {
            bonkChoyPlantFood.start();
            setAttacking(true);
            return;
        }
        if (isWasabiWhip()) {
            if (ability instanceof WasabiWhipAbility whip) {
                whip.cancelWindup();
            }
            wasabiWhipPlantFood.start();
            setAttacking(true);
            return;
        }
        if (isPhatBeet()) {
            if (ability instanceof PhatBeetAbility beet) {
                beet.cancelWindup();
            }
            phatBeetPlantFood.start();
            setAttacking(false);
            return;
        }
        if (isKiwibeast()) {
            if (ability instanceof KiwibeastAbility kiwi) {
                kiwi.cancelWindup();
            }
            kiwibeastPlantFood.start(this, context);
            setAttacking(false);
            return;
        }
        if (isWallNut()) {
            wallNutPlantFood.start(this);
            setAttacking(false);
            return;
        }
        if (isEndurian()) {
            if (ability instanceof EndurianAbility endurian) {
                endurian.cancelWindup();
            }
            endurianPlantFood.start(this);
            setAttacking(false);
            return;
        }
        if (isChomper()) {
            if (ability instanceof ChomperAbility chomper) {
                chomper.startPlantFood(this, context);
            }
            return;
        }
        foodEffect.apply(this, context);
    }

    private PlantFoodEffect activeFoodEffect() {
        return imitaterFoodEffect != null ? imitaterFoodEffect : foodEffect;
    }

    public boolean isPlantFooding() {
        return peaPodPlantFood.isActive() || fumeShroomPlantFood.isActive()
                || cabbagePultPlantFood.isActive() || kernelPultPlantFood.isActive()
                || melonPultPlantFood.isActive() || pepperPultPlantFood.isActive()
                || potatoMinePlantFood.isActive() || isTangleKelpPlantFooding() || isIcebergLettucePlantFooding()
                || bonkChoyPlantFood.isActive() || wasabiWhipPlantFood.isActive()
                || phatBeetPlantFood.isActive() || kiwibeastPlantFood.isActive()
                || wallNutPlantFood.isActive()
                || endurianPlantFood.isActive()
                || isChomperPlantFooding();
    }

    public PeaPodPlantFood.Phase plantFoodPhase() {
        if (potatoMinePlantFood.isActive()) {
            return mapPotatoMinePhase(potatoMinePlantFood.phase());
        }
        if (fumeShroomPlantFood.isActive()) {
            return fumeShroomPlantFood.phase();
        }
        if (cabbagePultPlantFood.isActive()) {
            return cabbagePultPlantFood.phase();
        }
        if (kernelPultPlantFood.isActive()) {
            return kernelPultPlantFood.phase();
        }
        if (melonPultPlantFood.isActive()) {
            return melonPultPlantFood.phase();
        }
        if (pepperPultPlantFood.isActive()) {
            return pepperPultPlantFood.phase();
        }
        return peaPodPlantFood.phase();
    }

    public PotatoMinePlantFood.Phase potatoMinePlantFoodPhase() {
        return potatoMinePlantFood.phase();
    }

    private static PeaPodPlantFood.Phase mapPotatoMinePhase(PotatoMinePlantFood.Phase phase) {
        return switch (phase) {
            case ON -> PeaPodPlantFood.Phase.ON;
            case LOOP -> PeaPodPlantFood.Phase.LOOP;
            case OFF -> PeaPodPlantFood.Phase.OFF;
            case NONE -> PeaPodPlantFood.Phase.NONE;
        };
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

    public void grantSmashArmor(int armorValue) {
        if (armorValue <= 0) {
            return;
        }
        armorLayers.removeIf(PlantArmor::absorbsSmash);
        armorLayers.add(new PlantArmor(armorValue, true));
    }

    public boolean tryAbsorbSmash() {
        for (PlantArmor armor : armorLayers) {
            if (armor.absorbSmash()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSmashArmor() {
        for (PlantArmor armor : armorLayers) {
            if (armor.absorbsSmash() && !armor.isDestroyed()) {
                return true;
            }
        }
        return false;
    }

    public int smashArmorHealth() {
        int health = 0;
        for (PlantArmor armor : armorLayers) {
            if (armor.absorbsSmash() && !armor.isDestroyed()) {
                health += armor.getHealth();
            }
        }
        return health;
    }

    public int smashArmorMax() {
        int max = 0;
        for (PlantArmor armor : armorLayers) {
            if (armor.absorbsSmash()) {
                max += armor.getMaxHealth();
            }
        }
        return max;
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

    public boolean isPlantFoodSpawned() {
        return plantFoodSpawned;
    }

    public void setPlantFoodSpawned(boolean plantFoodSpawned) {
        this.plantFoodSpawned = plantFoodSpawned;
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

    public boolean isTorchwoodBoosted() {
        return torchwoodBoosted;
    }

    public void setTorchwoodBoosted(boolean torchwoodBoosted) {
        this.torchwoodBoosted = torchwoodBoosted;
    }

    public boolean isSunBeanPowered() {
        return sunBeanPowered;
    }

    public void setSunBeanPowered(boolean sunBeanPowered) {
        this.sunBeanPowered = sunBeanPowered;
    }

    public int getPumpkinShellTier() {
        return pumpkinShellTier;
    }

    public void setPumpkinShellTier(int pumpkinShellTier) {
        this.pumpkinShellTier = Math.max(0, pumpkinShellTier);
    }

    public boolean hasExplodeONutPfArmor() {
        return explodeONutPfArmor;
    }

    public void setExplodeONutPfArmor(boolean explodeONutPfArmor) {
        this.explodeONutPfArmor = explodeONutPfArmor;
    }

    public int getMagnetBusyTicks() {
        return magnetBusyTicks;
    }

    public void setMagnetBusyTicks(int magnetBusyTicks) {
        this.magnetBusyTicks = Math.max(0, magnetBusyTicks);
    }

    public int getMagnetStealAnimTicks() {
        return magnetStealAnimTicks;
    }

    public void setMagnetStealAnimTicks(int magnetStealAnimTicks) {
        this.magnetStealAnimTicks = Math.max(0, magnetStealAnimTicks);
    }

    public int getMagnetHeldMetalTicks() {
        return magnetHeldMetalTicks;
    }

    public void setMagnetHeldMetalTicks(int magnetHeldMetalTicks) {
        this.magnetHeldMetalTicks = Math.max(0, magnetHeldMetalTicks);
    }

    public void decrementMagnetHeldMetalTicks() {
        if (magnetHeldMetalTicks > 0) {
            magnetHeldMetalTicks--;
        }
    }

    public String getMagnetHeldMetalAlias() {
        return magnetHeldMetalAlias;
    }

    public void setMagnetHeldMetalAlias(String magnetHeldMetalAlias) {
        this.magnetHeldMetalAlias = magnetHeldMetalAlias;
    }

    public void tuneCombatStats(int minHealth, int damage, double actionIntervalSeconds, GameContext context) {
        int health = Math.max(minHealth, Math.max(getMaxHealth(), stats.maxHealth()));
        setMaxHealth(health);
        if (getHealth() <= 0) {
            restoreHealth(health);
        }
        stats = new PlantStats(
                stats.cost(),
                health,
                damage,
                actionIntervalSeconds,
                stats.recharge(),
                stats.specialModifiers());
        if (context != null) {
            initializeCooldown(context.getTicksPerSecond());
        }
    }

    public String getImitatedPlantName() {
        return imitatedPlantName;
    }

    public void setImitatedPlantName(String imitatedPlantName) {
        this.imitatedPlantName = imitatedPlantName;
    }

    public int getImitaterMorphTicks() {
        return imitaterMorphTicks;
    }

    public void setImitaterMorphTicks(int imitaterMorphTicks) {
        this.imitaterMorphTicks = Math.max(0, imitaterMorphTicks);
    }

    public boolean isImitaterCopy() {
        return imitaterCopy;
    }

    public int getIceShroomAttackTicks() {
        return iceShroomAttackTicks;
    }

    public void setIceShroomAttackTicks(int iceShroomAttackTicks) {
        this.iceShroomAttackTicks = Math.max(0, iceShroomAttackTicks);
    }

    public String displayPlantName() {
        if ("Imitater".equals(getName()) && imitatedPlantName != null && !imitatedPlantName.isBlank()) {
            return imitatedPlantName;
        }
        return getName();
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
        return isAlive() && !isCatTransformed()
                && !("Imitater".equals(getName()) && imitaterMorphTicks > 0);
    }

    public int addHostileIceStack(String sourceId) {
        if (hasTag(PlantTag.FIRE)) {
            return hostileIceHits;
        }
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

    public boolean isPeaPod() {
        return PEA_POD.equals(getName());
    }

    public boolean isFumeShroom() {
        return FUME_SHROOM.equals(getName());
    }

    public boolean isCabbagePult() {
        return CABBAGE_PULT.equals(getName());
    }

    public boolean isKernelPult() {
        return KERNEL_PULT.equals(getName());
    }

    public boolean isMelonPult() {
        return MELON_PULT.equals(getName());
    }

    public boolean isWinterMelon() {
        return WINTER_MELON.equals(getName());
    }

    public boolean isPepperPult() {
        return PEPPER_PULT.equals(getName());
    }

    public boolean isPotatoMine() {
        return POTATO_MINE.equals(getName()) || PRIMAL_POTATO_MINE.equals(getName());
    }

    public boolean isPrimalPotatoMine() {
        return PRIMAL_POTATO_MINE.equals(getName());
    }

    public boolean isCherryBomb() {
        return CHERRY_BOMB.equals(getName());
    }

    public boolean isSquash() {
        return SQUASH.equals(getName());
    }

    public boolean isGrapeshot() {
        return GRAPESHOT.equals(getName());
    }

    public boolean isJalapeno() {
        return JALAPENO.equals(getName());
    }

    public boolean isDoomShroom() {
        return DOOM_SHROOM.equals(getName());
    }

    public boolean isTangleKelp() {
        return TANGLE_KELP.equals(getName());
    }

    public boolean isTangleKelpGrabbing() {
        return ability instanceof TangleKelpAbility tangleKelp && tangleKelp.isGrabbing();
    }

    public boolean isTangleKelpPlantFooding() {
        return ability instanceof TangleKelpAbility tangleKelp && tangleKelp.isPlantFoodActive();
    }

    public TangleKelpAbility.Phase tangleKelpPhase() {
        return ability instanceof TangleKelpAbility tangleKelp ? tangleKelp.phase() : TangleKelpAbility.Phase.IDLE;
    }

    public boolean isIcebergLettuce() {
        return ICEBERG_LETTUCE.equals(getName());
    }

    public boolean isIcebergLettuceFreezing() {
        return ability instanceof IcebergLettuceAbility iceberg && iceberg.isFreezing();
    }

    public boolean isIcebergLettucePlantFooding() {
        return ability instanceof IcebergLettuceAbility iceberg && iceberg.isPlantFoodActive();
    }

    public IcebergLettuceAbility.Phase icebergLettucePhase() {
        return ability instanceof IcebergLettuceAbility iceberg
                ? iceberg.phase() : IcebergLettuceAbility.Phase.IDLE;
    }

    public boolean isBonkChoy() {
        return BONK_CHOY.equals(getName());
    }

    public boolean isWasabiWhip() {
        return WASABI_WHIP.equals(getName());
    }

    public boolean isPhatBeet() {
        return PHAT_BEET.equals(getName());
    }

    public boolean isKiwibeast() {
        return KIWIBEAST.equals(getName());
    }

    public boolean isWallNut() {
        return WALL_NUT.equals(getName());
    }

    public boolean isTallNut() {
        return TALL_NUT.equals(getName());
    }

    public boolean isEndurian() {
        return ENDURIAN.equals(getName());
    }

    public boolean isChomper() {
        return CHOMPER.equals(getName());
    }

    public boolean isChomperPlantFooding() {
        return ability instanceof ChomperAbility chomper && chomper.isPlantFoodActive();
    }

    public boolean isChomperChewing() {
        return ability instanceof ChomperAbility chomper && chomper.isChewing();
    }

    public ChomperAbility.Phase chomperPhase() {
        return ability instanceof ChomperAbility chomper
                ? chomper.phase() : ChomperAbility.Phase.IDLE;
    }

    public PhatBeetPlantFood.Phase phatBeetPlantFoodPhase() {
        return phatBeetPlantFood.phase();
    }

    public KiwibeastPlantFood.Phase kiwibeastPlantFoodPhase() {
        return kiwibeastPlantFood.phase();
    }

    public WallNutPlantFood.Phase wallNutPlantFoodPhase() {
        return wallNutPlantFood.phase();
    }

    public EndurianPlantFood.Phase endurianPlantFoodPhase() {
        return endurianPlantFood.phase();
    }

    public int wallNutDamageStage() {
        int max = getMaxHealth();
        if (max <= 0) {
            return 3;
        }
        double ratio = getHealth() / (double) max;
        if (ratio > 0.75) {
            return 0;
        }
        if (ratio > 0.50) {
            return 1;
        }
        if (ratio > 0.25) {
            return 2;
        }
        return 3;
    }

    public int tallNutDamageStage() {
        int max = getMaxHealth();
        int health = getHealth();
        if (max <= 0) {
            return 2;
        }
        if (health * 3 > max * 2) {
            return 0;
        }
        if (health * 3 > max) {
            return 1;
        }
        return 2;
    }

    public int wallNutArmorStage() {
        if (!hasSmashArmor()) {
            return 0;
        }
        int max = smashArmorMax();
        int health = smashArmorHealth();
        if (max <= 0) {
            return 3;
        }
        if (health * 3 > max * 2) {
            return 1;
        }
        if (health * 3 > max) {
            return 2;
        }
        return 3;
    }

    public int endurianDamageStage() {
        int max = getMaxHealth();
        if (max <= 0) {
            return 3;
        }
        double ratio = getHealth() / (double) max;
        if (ratio > 0.75) {
            return 0;
        }
        if (ratio > 0.50) {
            return 1;
        }
        if (ratio > 0.25) {
            return 2;
        }
        return 3;
    }

    public int endurianArmorStage() {
        if (!hasSmashArmor()) {
            return 0;
        }
        int max = smashArmorMax();
        int health = smashArmorHealth();
        if (max <= 0) {
            return 3;
        }
        if (health * 3 > max * 2) {
            return 1;
        }
        if (health * 3 > max) {
            return 2;
        }
        return 3;
    }

    public int kiwibeastStage() {
        if (ability instanceof KiwibeastAbility kiwi) {
            return kiwi.stage(this);
        }
        return 1;
    }

    public int kiwibeastGrowthToken() {
        return ability instanceof KiwibeastAbility kiwi ? kiwi.growthToken() : 0;
    }

    public BonkChoyPlantFood.Phase bonkChoyPlantFoodPhase() {
        return bonkChoyPlantFood.phase();
    }

    public WasabiWhipPlantFood.Phase wasabiWhipPlantFoodPhase() {
        return wasabiWhipPlantFood.phase();
    }

    public WasabiWhipAbility.WhipStyle wasabiWhipStyle() {
        if (wasabiWhipPlantFood.isActive()) {
            return wasabiWhipPlantFood.whipStyle();
        }
        if (ability instanceof WasabiWhipAbility whip) {
            return whip.whipStyle();
        }
        return WasabiWhipAbility.WhipStyle.RIGHT;
    }

    public BonkChoyAbility.PunchStyle bonkChoyPunchStyle() {
        if (bonkChoyPlantFood.isActive()) {
            return bonkChoyPlantFood.punchStyle();
        }
        if (ability instanceof BonkChoyAbility bonk) {
            return bonk.punchStyle();
        }
        return BonkChoyAbility.PunchStyle.RIGHT;
    }

    public boolean isBonkChoyPunching() {
        return isAttacking() && isBonkChoy();
    }

    public boolean isDoomShroomProximityAlert() {
        return ability instanceof DoomShroomAbility doomShroom && doomShroom.isProximityAlert();
    }

    public boolean isDoomShroomDetonating() {
        return ability instanceof DoomShroomAbility doomShroom && doomShroom.isDetonating();
    }

    public boolean isDoomShroomPlantFoodTransforming() {
        return ability instanceof DoomShroomAbility doomShroom && doomShroom.isPlantFoodTransforming();
    }

    public boolean isDoomShroomPlantFooding() {
        return ability instanceof DoomShroomAbility doomShroom && doomShroom.isPlantFoodActive();
    }

    public int getDoomShroomGrowthStage() {
        return ability instanceof DoomShroomAbility doomShroom ? doomShroom.growthStage(this) : 0;
    }

    public boolean tryAddStack() {
        if (!isPeaPod() || stackCount >= MAX_PEA_POD_STACK) {
            return false;
        }
        stackCount++;
        return true;
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
