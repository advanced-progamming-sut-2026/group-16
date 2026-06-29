package model.game.entity.zombie;

import model.game.entity.Entity;
import model.game.entity.GameContext;
import model.game.entity.Plant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Zombie extends Entity {

    private final String type;
    private final double baseSpeed;
    private final int damage;          // EatDPS from definition
    private final int waveCost;
    private final List<Armor> armorLayers;
    private final List<ZombieBehavior> behaviors;
    private double currentSpeed;
    private boolean glowing;
    private ZombieState state;
    private int tickAge;

    private Zombie(Builder b) {
        super(b.alias, b.maxHealth, b.x, b.y);
        this.type = b.alias;
        this.baseSpeed = b.speed;
        this.currentSpeed = b.speed;
        this.damage = b.damage;
        this.waveCost = b.waveCost;
        this.glowing = b.glowing;
        this.armorLayers = List.copyOf(b.armors);
        this.behaviors = b.behaviors.isEmpty()
                ? List.of()
                : Collections.unmodifiableList(b.behaviors);
        this.state = ZombieState.SPAWNING;
        this.tickAge = 0;
    }

    @Override
    public void onTickUpdate(GameContext context) {
        if (isDead()) {
            return;
        }
        tickAge++;
        if (state == ZombieState.SPAWNING) {
            state = ZombieState.MOVING;
        }
        for (ZombieBehavior behavior : behaviors) {
            behavior.execute(this, context);
        }
    }

    @Override
    protected void onDeath() {
        state = ZombieState.DYING;
    }

    @Override
    public void takeDamage(int amount) {
        int remaining = amount;
        for (Armor armor : armorLayers) {
            if (remaining <= 0) break;
            if (!armor.isDestroyed()) {
                remaining = armor.absorbDamage(remaining);
            }
        }
        if (remaining > 0) {
            super.takeDamage(remaining);
        }
    }

    /**
     * Removes and returns the first magnetic armor, or {@code null}.
     */
    public Armor stripArmorViaMagnet() {
        for (int i = 0; i < armorLayers.size(); i++) {
            Armor a = armorLayers.get(i);
            if (!a.isDestroyed() && a.isMagneticRemovable()) {
                // Armor list is immutable; return a copy view
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
        return currentSpeed;
    }

    public void setCurrentSpeed(double s) {
        this.currentSpeed = s;
    }

    public int getDamage() {
        return damage;
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

    public List<Armor> getArmorLayers() {
        return armorLayers;
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

    public boolean hasArmor() {
        return armorLayers.stream().anyMatch(a -> !a.isDestroyed());
    }

//    public double getHealthRatio() {
//        return (double) getHealth() / getMaxHealth();
//    }

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