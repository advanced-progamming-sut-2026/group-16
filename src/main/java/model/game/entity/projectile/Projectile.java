package model.game.entity.projectile;

import model.game.entity.plant.Plant;

import java.util.HashSet;
import java.util.Set;

public final class Projectile {

    private int row;
    private double x;
    private final int damage;
    private final ProjectileProfile profile;
    private final ProjectileEffect effect;
    private final Plant source;
    private int pierceRemaining;
    private int lifetimeTicks;
    private boolean fromZombie;
    private final Set<String> hitEntityIds = new HashSet<>();

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining) {
        this.row = row;
        this.x = x;
        this.damage = damage;
        this.profile = profile;
        this.effect = effect == null ? ProjectileEffect.GENERIC : effect;
        this.source = source;
        this.pierceRemaining = pierceRemaining;
        this.lifetimeTicks = 200;
        this.fromZombie = source == null;
    }

    public static Projectile fromZombie(int row, double x, int damage, String projectileType) {
        Projectile p = new Projectile(row, x, damage, ProjectileProfile.straight(),
                ProjectileEffect.fromString(projectileType), null, 0);
        p.fromZombie = true;
        return p;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getDamage() {
        return damage;
    }

    public ProjectileProfile getProfile() {
        return profile;
    }

    public ProjectileEffect getEffect() {
        return effect;
    }

    public Plant getSource() {
        return source;
    }

    public boolean isFromZombie() {
        return fromZombie;
    }

    public int getPierceRemaining() {
        return pierceRemaining;
    }

    public void consumePierce() {
        if (pierceRemaining > 0) {
            pierceRemaining--;
        }
    }

    public boolean canPierce() {
        return profile.piercing() && pierceRemaining > 0;
    }

    public boolean hasHit(String entityId) {
        return hitEntityIds.contains(entityId);
    }

    public void recordHit(String entityId) {
        hitEntityIds.add(entityId);
    }

    public int getLifetimeTicks() {
        return lifetimeTicks;
    }

    public void decrementLifetime() {
        lifetimeTicks--;
    }

    public boolean isExpired() {
        return lifetimeTicks <= 0 || x < 0;
    }
}
