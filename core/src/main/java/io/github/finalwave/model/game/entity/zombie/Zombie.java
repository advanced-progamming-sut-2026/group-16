package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
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
    private int freezeTicksRemaining;
    private int chillTicksRemaining;
    private int poisonTicksRemaining;
    private int poisonDamagePerTick;
    private Action actionThisTick = Action.NONE;
    private GameContext lastContext;
    private boolean deathBehaviorsRun;
    private boolean movingRight;
    private boolean stationary;
    private boolean trapImmune;
    private boolean bypassDisabledPlants;
    private boolean dodoBypass;
    private boolean submerged;
    private final boolean basicKnightTarget;
    private final String knightTargetKey;
    private int rowSpan = 1;
    private boolean boss;
    private int stunTicksRemaining;
    private int bossPhase = 1;
    private String presentationClip = "idle";

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
        for (ZombieBehavior behavior : behaviors) {
            if (behavior.isMovementBehavior()) {
                behavior.execute(this, context);
            }
        }
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
    }

    public void takeDirectDamage(int amount) {
        if (boss) {
            applyBossDamage(amount);
            return;
        }
        if (amount > 0) {
            super.takeDamage(amount);
        }
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

    public void moveRight(double amount) {
        setX(getX() + amount);
    }

    public void setRow(int row) {
        setY(row);
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

    public void applyFreeze(int ticks) {
        freezeTicksRemaining = Math.max(freezeTicksRemaining, ticks);
    }

    public void applyChill(int ticks) {
        chillTicksRemaining = Math.max(chillTicksRemaining, ticks);
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
        if (isDead() || actionThisTick != Action.NONE) {
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