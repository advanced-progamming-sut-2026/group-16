package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.plant.support.PlantLaneSupport;
import io.github.finalwave.model.game.entity.plant.support.SunBeanSupport;
import io.github.finalwave.model.game.entity.projectile.Projectile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class Zombie extends Entity {

    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final String type;
    private final double baseSpeed;
    private int damage;          // EatDPS from definition
    private final int waveCost;
    private final List<Armor> armorLayers;
    private final List<ZombieBehavior> behaviors;
    private double permanentSpeedMultiplier = 1.0;
    private double permanentEatingDamageMultiplier = 1.0;
    private boolean glowing;
    private ZombieState state;
    private int tickAge;
    private boolean hypnotized;
    private int sunBeanInfections;
    private double sunBeanBank;
    private int garlicDivertTicks;
    private int garlicDivertCol = -1;
    private int garlicDivertRow = -1;
    private int freezeTicksRemaining;
    private int chillTicksRemaining;
    private int poisonTicksRemaining;
    private int poisonDamagePerTick;
    private boolean suppressHitFlash;
    private Action actionThisTick = Action.NONE;
    private GameContext lastContext;
    private boolean deathBehaviorsRun;
    private boolean movingRight;
    private boolean stationary;
    private boolean trapImmune;
    private boolean laneLocked;
    private boolean bypassDisabledPlants;
    private boolean dodoBypass;
    private boolean dragLocked;
    private double dragStep;
    private boolean swallowed;
    private boolean submerged;
    private final boolean basicKnightTarget;
    private final String knightTargetKey;
    private int rowSpan = 1;
    private boolean boss;
    private int stunTicksRemaining;
    private int bossPhase = 1;
    private String presentationClip = "idle";
    private int abilityTicksRemaining;
    private int blastTicksRemaining;
    private int flyTicksRemaining;
    private int flyTicksTotal;
    private int landTicksRemaining;
    private double arcFromX;
    private double arcToX;
    private double arcApex;
    private double arcSpawnLift;
    private boolean afterArcMoveRight;
    private boolean staffSunConcealed;
    private boolean torchLit = true;
    private boolean juggling;
    private boolean sandstormSpawn;
    private GargantuarImpThrow pendingGargantuarImpThrow;
    private boolean gargantuarImpSpent;
    private boolean thrownByGargantuar;
    private int throwIntroTicksRemaining;
    private String throwIntroClip = "pop";

    private Zombie(Builder b) {
        super(b.alias + "-" + NEXT_ID.incrementAndGet(), b.maxHealth, b.x, b.y);
        this.type = b.alias;
        this.baseSpeed = b.speed;
        this.damage = b.damage;
        this.waveCost = b.waveCost;
        this.glowing = b.glowing;
        this.armorLayers = new ArrayList<>(b.armors);
        this.behaviors = b.behaviors.isEmpty()
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(b.behaviors));
        this.state = ZombieState.SPAWNING;
        this.tickAge = 0;
        this.basicKnightTarget = b.basicKnightTarget;
        this.knightTargetKey = b.knightTargetKey;
        this.permanentSpeedMultiplier = b.speedMultiplier;
    }

    @Override
    public void onTickUpdate(GameContext context) {
        if (isDead()) {
            return;
        }
        lastContext = context;
        tickAge++;
        actionThisTick = Action.NONE;
        if (boss && stunTicksRemaining > 0) {
            stunTicksRemaining--;
            if (stunTicksRemaining > 0) {
                state = ZombieState.ABILITY;
                presentationClip = "stun";
                return;
            }
            presentationClip = "idle";
        }
        if (abilityTicksRemaining > 0) {
            tickHeldAbility();
            if (abilityTicksRemaining > 0) {
                return;
            }
            finishHeldAbility();
        }
        state = ZombieState.MOVING;
        if (hypnotized) {
            actAsHypnotized(context);
            return;
        }
        for (ZombieBehavior behavior : behaviors) {
            if (!behavior.isMovementBehavior()) {
                behavior.execute(this, context);
            }
        }
        if (abilityTicksRemaining > 0) {
            return;
        }
        for (ZombieBehavior behavior : behaviors) {
            if (behavior.isMovementBehavior()) {
                behavior.execute(this, context);
            }
        }
        tickGarlicDivert(context);
    }

    @Override
    protected void onDeath() {
        state = ZombieState.DYING;
        runDeathBehaviors(lastContext);
    }

    public void bindContext(GameContext context) {
        if (context == null) {
            return;
        }
        lastContext = context;
        if (isDead()) {
            runDeathBehaviors(context);
        }
    }

    public void runDeathBehaviors(GameContext context) {
        if (!isDead() || deathBehaviorsRun || context == null) {
            return;
        }
        deathBehaviorsRun = true;
        for (ZombieBehavior behavior : behaviors) {
            behavior.onDeath(this, context);
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (boss) {
            applyBossDamage(amount);
            return;
        }
        int remaining = amount;
        for (Armor armor : armorLayers) {
            if (remaining <= 0) {
                break;
            }
            if (!armor.isDestroyed()) {
                remaining = armor.absorbDamage(remaining);
            }
        }
        if (remaining > 0) {
            super.takeDamage(remaining);
        }
        notifySunBeanDamage(amount);
    }

    public void takeDirectDamage(int amount) {
        if (boss) {
            applyBossDamage(amount);
            return;
        }
        if (amount > 0) {
            super.takeDamage(amount);
            notifySunBeanDamage(amount);
        }
    }

    private void notifySunBeanDamage(int amount) {
        if (lastContext != null && amount > 0 && sunBeanInfections > 0) {
            SunBeanSupport.onZombieDamaged(this, amount, lastContext);
        }
    }

    public int getSunBeanInfections() {
        return sunBeanInfections;
    }

    public void addSunBeanInfection() {
        sunBeanInfections++;
        glowing = true;
    }

    public double getSunBeanBank() {
        return sunBeanBank;
    }

    public void addSunBeanBank(double amount) {
        sunBeanBank += amount;
    }

    public void scheduleGarlicDivert(int col, int row, int delayTicks) {
        if (delayTicks <= 0 || garlicDivertTicks > 0) {
            return;
        }
        garlicDivertTicks = delayTicks + 1;
        garlicDivertCol = col;
        garlicDivertRow = row;
    }

    private void tickGarlicDivert(GameContext context) {
        if (garlicDivertTicks <= 0) {
            return;
        }
        garlicDivertTicks--;
        if (garlicDivertTicks != 0 || context == null) {
            return;
        }
        Plant plant = context.getPlantAt(garlicDivertCol, garlicDivertRow);
        if (plant != null && plant.isAlive() && "Garlic".equals(plant.getName())) {
            PlantLaneSupport.divertBiter(this, plant, context);
        } else if (garlicDivertRow >= 0) {
            PlantLaneSupport.divertBiter(this, garlicDivertRow, context);
        }
        garlicDivertCol = -1;
        garlicDivertRow = -1;
    }

    private void applyBossDamage(int amount) {
        if (isDead() || stunTicksRemaining > 0 || amount <= 0) {
            return;
        }
        int phaseHp = Math.max(1, getMaxHealth() / 3);
        int floorHp = bossPhase < 3 ? (3 - bossPhase) * phaseHp : 0;
        int remainingInPhase = Math.max(0, getHealth() - floorHp);
        boolean phaseBreak = bossPhase < 3 && amount >= remainingInPhase && remainingInPhase > 0;
        int applied = phaseBreak ? remainingInPhase : amount;
        super.takeDamage(applied);
        if (isDead() || !phaseBreak) {
            return;
        }
        bossPhase++;
        stunTicksRemaining = 50;
        presentationClip = "stun";
        state = ZombieState.ABILITY;
    }

    public void configureAsBoss(int span) {
        boss = true;
        rowSpan = Math.max(1, span);
        stationary = true;
        trapImmune = true;
        bossPhase = 1;
        stunTicksRemaining = 0;
        presentationClip = "intro";
        state = ZombieState.MOVING;
    }

    public boolean isBoss() {
        return boss;
    }

    public boolean occupiesRow(int row) {
        if (rowSpan <= 1) {
            return getRow() == row;
        }
        int primary = getRow();
        return row == primary || row == primary + 1;
    }

    public int[] occupiedRows() {
        if (rowSpan <= 1) {
            return new int[]{getRow()};
        }
        return new int[]{getRow(), getRow() + 1};
    }

    public boolean isStunned() {
        return stunTicksRemaining > 0;
    }

    public int getStunTicksRemaining() {
        return stunTicksRemaining;
    }

    public int getBossPhase() {
        return bossPhase;
    }

    public String getPresentationClip() {
        return presentationClip;
    }

    public void setPresentationClip(String clip) {
        if (clip != null && !clip.isBlank()) {
            presentationClip = clip;
        }
    }

    public boolean isAbilityHeld() {
        return abilityTicksRemaining > 0;
    }

    public void concealStaffSun() {
        staffSunConcealed = true;
    }

    public boolean isStaffSunConcealed() {
        return staffSunConcealed;
    }

    public void setTorchLit(boolean torchLit) {
        this.torchLit = torchLit;
    }

    public boolean isTorchLit() {
        return torchLit;
    }

    public void setJuggling(boolean juggling) {
        this.juggling = juggling;
    }

    public boolean isJuggling() {
        return juggling;
    }

    public boolean beginAbility(String clip, int holdTicks) {
        if (isDead() || abilityTicksRemaining > 0 || actionThisTick != Action.NONE) {
            return false;
        }
        actionThisTick = Action.ABILITY;
        state = ZombieState.ABILITY;
        if (clip != null && !clip.isBlank()) {
            presentationClip = clip;
        }
        abilityTicksRemaining = Math.max(1, holdTicks);
        return true;
    }

    public void queueGargantuarImpThrow(GargantuarImpThrow plan) {
        pendingGargantuarImpThrow = plan;
    }

    public boolean isGargantuarImpSpent() {
        return gargantuarImpSpent;
    }

    public boolean shouldHideGargantuarImpAmmo() {
        return gargantuarImpSpent || hasPendingGargantuarImpThrow();
    }

    public boolean hasPendingGargantuarImpThrow() {
        return pendingGargantuarImpThrow != null;
    }

    public boolean wasThrownByGargantuar() {
        return thrownByGargantuar;
    }

    public boolean beginImpThrowArc(
            double fromX, double toX, double spawnLift, double apex, int flyTicks, int landTicks) {
        int fly = Math.max(1, flyTicks);
        int land = Math.max(1, landTicks);
        thrownByGargantuar = true;
        throwIntroTicksRemaining = 0;
        arcFromX = fromX;
        arcToX = toX;
        arcSpawnLift = Math.max(0, spawnLift);
        arcApex = Math.max(0.25, apex);
        setX(fromX);
        flyTicksTotal = fly;
        flyTicksRemaining = fly;
        landTicksRemaining = land;
        blastTicksRemaining = 0;
        afterArcMoveRight = false;
        return beginAbility("fly", fly + land);
    }

    public boolean beginThrownFlight(double toX, int flyTicks, int landTicks) {
        int fly = Math.max(1, flyTicks);
        int land = Math.max(1, landTicks);
        if (!beginAbility("fly", fly + land)) {
            return false;
        }
        arcFromX = getX();
        arcToX = toX;
        arcApex = 0;
        flyTicksTotal = fly;
        flyTicksRemaining = fly;
        landTicksRemaining = land;
        blastTicksRemaining = 0;
        afterArcMoveRight = false;
        return true;
    }

    public boolean beginBlastOffFlight(double toX, int blastTicks, int flyTicks, int landTicks) {
        int blast = Math.max(1, blastTicks);
        int fly = Math.max(1, flyTicks);
        int land = Math.max(1, landTicks);
        if (!beginAbility("blastoff", blast + fly + land)) {
            return false;
        }
        blastTicksRemaining = blast;
        flyTicksTotal = fly;
        flyTicksRemaining = 0;
        landTicksRemaining = land;
        arcFromX = getX();
        arcToX = toX;
        arcApex = 0;
        afterArcMoveRight = true;
        return true;
    }

    public boolean isInFlightArc() {
        return flyTicksTotal > 0 && flyTicksRemaining > 0;
    }

    public double arcProgress(float tickFraction) {
        if (flyTicksTotal <= 0) {
            return 0;
        }
        double flown = flyTicksTotal - flyTicksRemaining;
        if (flyTicksRemaining > 0 && tickFraction > 0f) {
            flown += tickFraction;
        }
        return Math.min(1.0, Math.max(0.0, flown / flyTicksTotal));
    }

    public double arcDisplayX(float tickFraction) {
        double t = arcProgress(tickFraction);
        if (!thrownByGargantuar) {
            t = easeInOut(t);
        }
        return arcFromX + (arcToX - arcFromX) * t;
    }

    public double arcLiftAt(double normalizedProgress) {
        if (flyTicksTotal <= 0 || arcApex <= 0) {
            return 0;
        }
        double p = Math.min(1.0, Math.max(0.0, normalizedProgress));
        return parabolicLift(p);
    }

    public double arcLiftForX(double x) {
        if (flyTicksTotal <= 0 || arcApex <= 0) {
            return 0;
        }
        double span = arcFromX - arcToX;
        if (Math.abs(span) < 0.001) {
            return 0;
        }
        double p = Math.min(1.0, Math.max(0.0, (arcFromX - x) / span));
        return parabolicLift(p);
    }

    private double parabolicLift(double progress) {
        double p = Math.min(1.0, Math.max(0.0, progress));
        return arcSpawnLift * (1.0 - p) + 4.0 * arcApex * p * (1.0 - p);
    }

    public float arcTangentAngleDegrees(float tickFraction) {
        if (!thrownByGargantuar || flyTicksTotal <= 0 || arcApex <= 0) {
            return 0f;
        }
        double x = arcDisplayX(tickFraction);
        double span = arcFromX - arcToX;
        if (Math.abs(span) < 0.001) {
            return 0f;
        }
        double p = Math.min(1.0, Math.max(0.0, (arcFromX - x) / span));
        double dhdp = -arcSpawnLift + 4.0 * arcApex * (1.0 - 2.0 * p);
        double dhdx = dhdp * (-1.0 / span);
        return (float) Math.toDegrees(Math.atan2(dhdx, 1.0));
    }

    private static double easeInOut(double t) {
        if (t <= 0) {
            return 0;
        }
        if (t >= 1) {
            return 1;
        }
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    public double arcLift(float tickFraction) {
        if (thrownByGargantuar && flyTicksTotal > 0) {
            if (flyTicksRemaining > 0 || tickFraction > 0f) {
                return arcLiftForX(arcDisplayX(tickFraction));
            }
            return 0;
        }
        return arcLiftAt(arcProgress(tickFraction));
    }

    public double flightLift() {
        if (arcApex > 0 && flyTicksTotal > 0 && flyTicksRemaining > 0) {
            return arcLift(0f);
        }
        if (flyTicksTotal <= 0 || flyTicksRemaining <= 0) {
            return 0;
        }
        double progress = 1.0 - (flyTicksRemaining / (double) flyTicksTotal);
        return Math.sin(Math.PI * progress) * 0.4;
    }

    private void tickHeldAbility() {
        maybeReleaseGargantuarImpEarly();
        abilityTicksRemaining--;
        state = ZombieState.ABILITY;
        if (throwIntroTicksRemaining > 0) {
            if (throwIntroClip != null && !throwIntroClip.isBlank()) {
                presentationClip = throwIntroClip;
            }
            throwIntroTicksRemaining--;
            if (throwIntroTicksRemaining == 0 && flyTicksTotal > 0) {
                flyTicksRemaining = flyTicksTotal;
                presentationClip = "fly";
            }
            return;
        }
        if (blastTicksRemaining > 0) {
            presentationClip = "blastoff";
            blastTicksRemaining--;
            if (blastTicksRemaining == 0 && flyTicksTotal > 0) {
                presentationClip = "fly";
                flyTicksRemaining = flyTicksTotal;
                arcFromX = getX();
            }
            return;
        }
        if (flyTicksRemaining > 0) {
            presentationClip = "fly";
            flyTicksRemaining--;
            double linear = 1.0 - (flyTicksRemaining / (double) flyTicksTotal);
            double t = thrownByGargantuar ? linear : easeInOut(linear);
            setX(arcFromX + (arcToX - arcFromX) * t);
            if (flyTicksRemaining == 0) {
                setX(arcToX);
                presentationClip = "land";
            }
            return;
        }
        if (landTicksRemaining > 0 && flyTicksTotal > 0) {
            presentationClip = "land";
            landTicksRemaining--;
            setX(arcToX);
            if (landTicksRemaining == 0 && afterArcMoveRight) {
                setMovingRight(true);
            }
        }
    }

    private void finishHeldAbility() {
        releasePendingGargantuarImpThrow();
        if (afterArcMoveRight) {
            setMovingRight(true);
            setX(arcToX);
        }
        if (!boss) {
            presentationClip = "idle";
        }
        flyTicksTotal = 0;
        flyTicksRemaining = 0;
        landTicksRemaining = 0;
        blastTicksRemaining = 0;
        afterArcMoveRight = false;
        throwIntroTicksRemaining = 0;
        throwIntroClip = "pop";
        arcApex = 0;
        arcSpawnLift = 0;
    }

    private void maybeReleaseGargantuarImpEarly() {
        GargantuarImpThrow plan = pendingGargantuarImpThrow;
        if (plan == null || lastContext == null) {
            return;
        }
        int elapsed = plan.throwHoldTicks() - abilityTicksRemaining;
        if (elapsed >= Math.max(0, plan.releaseTicksAfterStart())) {
            releasePendingGargantuarImpThrow();
        }
    }

    private void releasePendingGargantuarImpThrow() {
        GargantuarImpThrow plan = pendingGargantuarImpThrow;
        if (plan == null || lastContext == null) {
            return;
        }
        pendingGargantuarImpThrow = null;
        gargantuarImpSpent = true;
        int row = Math.max(0, Math.min(lastContext.getRowCount() - 1, getRow()));
        double fromX = Math.max(
                0,
                Math.min(
                        lastContext.getColCount() - 1,
                        getX() - plan.spawnOffsetX() - plan.spawnForwardTiles()));
        double toX = Math.max(0, Math.min(lastContext.getColCount() - 1, plan.landX()));
        Zombie imp = lastContext.spawnZombieOfType(plan.impAlias(), row, fromX);
        if (imp != null) {
            imp.beginImpThrowArc(
                    fromX, toX, plan.spawnLift(), plan.arcApex(), plan.flyTicks(), plan.landTicks());
        }
    }

    public void moveRight(double amount) {
        setX(getX() + amount);
    }

    public void setRow(int row) {
        if (laneLocked) {
            return;
        }
        setY(row);
    }

    public void lockLane() {
        laneLocked = true;
        setY(getRow());
    }

    public void setVisualY(double y) {
        setY(y);
    }

    public void setPosition(double x, int row) {
        setX(x);
        setY(row);
    }
    
    public Armor stripArmorViaMagnet() {
        for (int i = 0; i < armorLayers.size(); i++) {
            Armor a = armorLayers.get(i);
            if (!a.isDestroyed() && a.isMagneticRemovable()) {
                a.destroy();
                return a;
            }
        }
        return null;
    }

    public void moveLeft(double amount) {
        setX(getX() - amount);
    }

    public void attackPlant(Plant target, int damage) {
        if (target != null && target.isAlive()) {
            target.takeDamage(damage);
            int reflected = (int) target.getStats()
                    .specialModifier("REFLECT_DAMAGE_BUFF");
            if (reflected > 0) {
                takeDirectDamage(reflected);
            }
        }
    }

    public int getRow() {
        return (int) getY();
    }

    public int getTickAge() {
        return tickAge;
    }

    public String getType() {
        return type;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public double getCurrentSpeed() {
        if (freezeTicksRemaining > 0) {
            return 0.0;
        }
        double coldMultiplier = chillTicksRemaining > 0 ? 0.5 : 1.0;
        return baseSpeed * permanentSpeedMultiplier * coldMultiplier;
    }

    public void setCurrentSpeed(double s) {
        if (!Double.isFinite(s) || s < 0) {
            throw new IllegalArgumentException("speed must be finite and non-negative");
        }
        permanentSpeedMultiplier = baseSpeed == 0 ? 1.0 : s / baseSpeed;
    }

    public void multiplySpeed(double multiplier) {
        validateMultiplier(multiplier);
        permanentSpeedMultiplier *= multiplier;
    }

    public double getPermanentSpeedMultiplier() {
        return permanentSpeedMultiplier;
    }

    public int getDamage() {
        return Math.max(0, (int) Math.round(damage * permanentEatingDamageMultiplier));
    }

    public void multiplyEatingDamage(double multiplier) {
        validateMultiplier(multiplier);
        permanentEatingDamageMultiplier *= multiplier;
    }

    public double getPermanentEatingDamageMultiplier() {
        return permanentEatingDamageMultiplier;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean g) {
        this.glowing = g;
    }

    public void markSandstormSpawn() {
        sandstormSpawn = true;
    }

    public boolean isSandstormSpawn() {
        return sandstormSpawn;
    }

    public boolean isHypnotized() {
        return hypnotized;
    }

    public void setHypnotized(boolean hypnotized) {
        this.hypnotized = hypnotized;
    }

    public void hypnotize(double healthMultiplier, double damageMultiplier) {
        if (boss || hypnotized) {
            return;
        }
        hypnotized = true;
        int oldMaxHealth = getMaxHealth();
        int newMaxHealth = Math.max(oldMaxHealth,
                (int) Math.round(oldMaxHealth * Math.max(1.0, healthMultiplier)));
        setMaxHealth(newMaxHealth);
        heal(newMaxHealth - oldMaxHealth);
        damage = Math.max(damage,
                (int) Math.round(damage * Math.max(1.0, damageMultiplier)));
        state = ZombieState.MOVING;
    }

    private void actAsHypnotized(GameContext context) {
        Zombie target = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Zombie candidate : context.getZombiesInRow(getRow())) {
            if (candidate == this || candidate.isDead() || candidate.isHypnotized()) {
                continue;
            }
            double distance = Math.abs(candidate.getX() - getX());
            if (distance < nearestDistance) {
                target = candidate;
                nearestDistance = distance;
            }
        }
        if (target != null && nearestDistance <= 0.6) {
            state = ZombieState.EATING;
            target.takeDamage(Math.max(1, getDamage() / context.getTicksPerSecond()));
        } else {
            state = ZombieState.MOVING;
            moveRight(getCurrentSpeed() / context.getTicksPerSecond());
        }
    }

    public int getFreezeTicksRemaining() {
        return freezeTicksRemaining;
    }

    public int getChillTicksRemaining() {
        return chillTicksRemaining;
    }

    public int getPoisonTicksRemaining() {
        return poisonTicksRemaining;
    }

    public void setSuppressHitFlash(boolean suppressHitFlash) {
        this.suppressHitFlash = suppressHitFlash;
    }

    public boolean consumeSuppressHitFlash() {
        if (!suppressHitFlash) {
            return false;
        }
        suppressHitFlash = false;
        return true;
    }

    public void applyFreeze(int ticks) {
        freezeTicksRemaining = Math.max(freezeTicksRemaining, ticks);
    }

    public void applyChill(int ticks) {
        chillTicksRemaining = Math.max(chillTicksRemaining, ticks);
    }

    public void applyStun(int ticks) {
        stunTicksRemaining = Math.max(stunTicksRemaining, ticks);
        if (stunTicksRemaining > 0) {
            presentationClip = "stun";
        }
    }

    public void applyPoison(int ticks, int damagePerTick) {
        poisonTicksRemaining = Math.max(poisonTicksRemaining, ticks);
        poisonDamagePerTick = Math.max(poisonDamagePerTick, damagePerTick);
    }

    public void clearColdStatuses() {
        freezeTicksRemaining = 0;
        chillTicksRemaining = 0;
    }

    public void tickStatuses() {
        if (freezeTicksRemaining > 0) {
            freezeTicksRemaining--;
        } else if (chillTicksRemaining > 0) {
            chillTicksRemaining--;
        }
        if (poisonTicksRemaining > 0) {
            poisonTicksRemaining--;
            takeDirectDamage(poisonDamagePerTick);
        }
    }

    public List<Armor> getArmorLayers() {
        return Collections.unmodifiableList(armorLayers);
    }

    public void addArmor(Armor armor) {
        if (armor == null) {
            throw new IllegalArgumentException("armor must not be null");
        }
        armorLayers.add(armor);
    }

    public void grantArmor(Armor armor) {
        addArmor(armor);
    }

    public List<ZombieBehavior> getBehaviors() {
        return behaviors;
    }

    public ZombieState getState() {
        return state;
    }

    public void setState(ZombieState s) {
        this.state = s;
    }

    public boolean tryBeginAbilityAction() {
        if (isDead() || actionThisTick != Action.NONE || abilityTicksRemaining > 0) {
            return false;
        }
        actionThisTick = Action.ABILITY;
        state = ZombieState.ABILITY;
        return true;
    }

    public boolean tryBeginMovementAction() {
        if (isDead() || actionThisTick != Action.NONE) {
            return false;
        }
        actionThisTick = Action.MOVEMENT;
        return true;
    }

    public boolean hasArmor() {
        return armorLayers.stream().anyMatch(a -> !a.isDestroyed());
    }

    public boolean hasArmorAlias(String alias) {
        return armorLayers.stream().anyMatch(a -> !a.isDestroyed() && a.getAlias().equals(alias));
    }

    public boolean interceptProjectile(Projectile projectile, GameContext context) {
        for (ZombieBehavior behavior : behaviors) {
            if (behavior.interceptProjectile(this, projectile, context)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }

    public boolean isStationary() {
        return stationary;
    }

    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }

    public boolean isTrapImmune() {
        return trapImmune;
    }

    public void setTrapImmune(boolean trapImmune) {
        this.trapImmune = trapImmune;
    }

    public void setBypassDisabledPlants(boolean bypassDisabledPlants) {
        this.bypassDisabledPlants = bypassDisabledPlants;
    }

    public void setDodoBypass(boolean dodoBypass) {
        this.dodoBypass = dodoBypass;
        this.trapImmune = dodoBypass;
    }

    public boolean isDodoBypass() {
        return dodoBypass;
    }

    public boolean isDragLocked() {
        return dragLocked;
    }

    public void setDragLocked(boolean dragLocked) {
        this.dragLocked = dragLocked;
        if (!dragLocked) {
            dragStep = 0;
        }
    }

    public double getDragStep() {
        return dragStep;
    }

    public void setDragStep(double dragStep) {
        this.dragStep = dragStep;
    }

    public boolean isSwallowed() {
        return swallowed;
    }

    public void setSwallowed(boolean swallowed) {
        this.swallowed = swallowed;
    }

    public boolean shouldBypass(Plant plant) {
        if (plant == null) {
            return false;
        }
        if (plant.isCatTransformed()) {
            return true;
        }
        if (bypassDisabledPlants && plant.isDisabled()) {
            return true;
        }
        if (!dodoBypass || "Tall-nut".equalsIgnoreCase(plant.getName())) {
            return false;
        }
        return plant.hasTag(PlantTag.TRAP)
                || plant.hasTag(PlantTag.MOVE_ZOMBIE)
                || plant.getName().toLowerCase().contains("wall-nut")
                || plant.getName().toLowerCase().contains("spike");
    }

    public boolean isValidKnightTarget(java.util.Set<String> validTargetKeys) {
        return basicKnightTarget && knightTargetKey != null
                && validTargetKeys.contains(knightTargetKey);
    }

    public boolean isSubmerged() {
        return submerged && state != ZombieState.EATING;
    }

    public void setSubmerged(boolean submerged) {
        this.submerged = submerged;
    }

    private static void validateMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier < 0) {
            throw new IllegalArgumentException("multiplier must be finite and non-negative");
        }
    }

    private enum Action {
        NONE,
        MOVEMENT,
        ABILITY
    }

    @Override
    public String toString() {
        return "Zombie[%s hp=%d/%d x=%.1f row=%d state=%s]"
                .formatted(type, getHealth(), getMaxHealth(), getX(), getRow(), state);
    }

    public static final class Builder {
        private final String alias;
        private final List<Armor> armors = new ArrayList<>();
        private final List<ZombieBehavior> behaviors = new ArrayList<>();
        private int maxHealth = 100;
        private double speed = 0.5;
        private int damage = 100;
        private int waveCost = 1;
        private double x = 0;
        private double y = 0;
        private boolean glowing = false;
        private boolean basicKnightTarget;
        private String knightTargetKey;
        private double speedMultiplier = 1.0;

        public Builder(String alias) {
            if (alias == null || alias.isBlank()) {
                throw new IllegalArgumentException("alias must not be blank");
            }
            this.alias = alias;
        }

        public Builder maxHealth(int v) {
            this.maxHealth = v;
            return this;
        }

        public Builder speed(double v) {
            this.speed = v;
            return this;
        }

        public Builder damage(int v) {
            this.damage = v;
            return this;
        }

        public Builder waveCost(int v) {
            this.waveCost = v;
            return this;
        }

        public Builder position(double x, double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder glowing(boolean v) {
            this.glowing = v;
            return this;
        }

        public Builder knightTarget(boolean basic, String key) {
            this.basicKnightTarget = basic;
            this.knightTargetKey = key;
            return this;
        }

        public Builder speedMultiplier(double v) {
            if (!Double.isFinite(v) || v < 0) {
                throw new IllegalArgumentException("multiplier must be finite and non-negative");
            }
            this.speedMultiplier = v;
            return this;
        }

        public Builder armor(Armor a) {
            this.armors.add(a);
            return this;
        }

        public Builder addBehavior(ZombieBehavior b) {
            if (b != null) this.behaviors.add(b);
            return this;
        }

        public Zombie build() {
            return new Zombie(this);
        }
    }
}