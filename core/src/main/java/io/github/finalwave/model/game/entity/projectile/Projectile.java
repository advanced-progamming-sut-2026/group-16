package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.entity.plant.Plant;

import java.util.HashSet;
import java.util.Set;

public final class Projectile {

    private static final java.util.concurrent.atomic.AtomicLong NEXT_ID =
            new java.util.concurrent.atomic.AtomicLong(1);

    private final String id = "proj-" + NEXT_ID.getAndIncrement();
    private double y;
    private double x;
    private final int damage;
    private final ProjectileProfile profile;
    private final ProjectileEffect effect;
    private final Plant source;
    private int pierceRemaining;
    private int lifetimeTicks;
    private boolean fromZombie;
    private boolean reverse;
    private boolean directed;
    private double vx;
    private double vy;
    private float visualScale = 1f;
    private double visualLaneOffset;
    private float visualAnchorY = -1f;
    private int bowlingBouncesRemaining;
    private String hostileSourceId;
    private boolean torchwoodBoosted;
    private String visualClip;
    private final Set<String> hitEntityIds = new HashSet<>();

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining) {
        this.y = row;
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
        return fromZombie(row, x, damage, projectileType, "hostile");
    }

    public static Projectile fromZombie(int row, double x, int damage, String projectileType,
                                        String sourceId) {
        Projectile p = new Projectile(row, x, damage, ProjectileProfile.straight(),
                ProjectileEffect.fromString(projectileType), null, 0);
        p.fromZombie = true;
        p.hostileSourceId = sourceId == null || sourceId.isBlank() ? "hostile" : sourceId;
        return p;
    }

    public static Projectile reflected(int row, double x, Projectile original, String sourceId) {
        Projectile p = new Projectile(row, x, original.getDamage(), original.getProfile(),
                original.getEffect(), null, 0);
        p.fromZombie = true;
        p.hostileSourceId = sourceId == null || sourceId.isBlank() ? "reflected" : sourceId;
        p.visualScale = original.visualScale;
        p.visualLaneOffset = original.visualLaneOffset;
        if (original.directed) {
            p.setVelocity(-original.vx, original.vy);
        }
        return p;
    }

    public String getId() {
        return id;
    }

    public int getRow() {
        return (int) Math.round(y);
    }

    public double getY() {
        return y;
    }

    public void setRow(int row) {
        this.y = row;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getDamage() {
        return torchwoodBoosted ? damage * 2 : damage;
    }

    public ProjectileProfile getProfile() {
        return profile;
    }

    public ProjectileEffect getEffect() {
        return torchwoodBoosted ? ProjectileEffect.FIRE : effect;
    }

    public boolean isTorchwoodBoosted() {
        return torchwoodBoosted;
    }

    public void applyTorchwoodBoost() {
        if (torchwoodBoosted || fromZombie || directed) {
            return;
        }
        if (effect == ProjectileEffect.PEA
                || effect == ProjectileEffect.GENERIC
                || effect == ProjectileEffect.MEGA_GATLING_PEA) {
            torchwoodBoosted = true;
        }
    }

    public Plant getSource() {
        return source;
    }

    public boolean isFromZombie() {
        return fromZombie;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setVelocity(double vx, double vy) {
        this.directed = true;
        this.vx = vx;
        this.vy = vy;
        if (vx < 0) {
            this.reverse = true;
        }
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public float getVisualScale() {
        return visualScale;
    }

    public void setVisualScale(float visualScale) {
        this.visualScale = Math.max(0.1f, visualScale);
    }

    public double getVisualLaneOffset() {
        return visualLaneOffset;
    }

    public void setVisualLaneOffset(double visualLaneOffset) {
        this.visualLaneOffset = visualLaneOffset;
    }

    public float getVisualAnchorY() {
        return visualAnchorY;
    }

    public void setVisualAnchorY(float visualAnchorY) {
        this.visualAnchorY = visualAnchorY;
    }

    public int getBowlingBouncesRemaining() {
        return bowlingBouncesRemaining;
    }

    public void setBowlingBouncesRemaining(int bowlingBouncesRemaining) {
        this.bowlingBouncesRemaining = Math.max(0, bowlingBouncesRemaining);
    }

    public void consumeBowlingBounce() {
        if (bowlingBouncesRemaining > 0) {
            bowlingBouncesRemaining--;
        }
    }

    public void setVisualClip(String visualClip) {
        this.visualClip = visualClip == null || visualClip.isBlank() ? null : visualClip;
    }

    public String getVisualClip() {
        return visualClip;
    }

    public String getHostileSourceId() {
        return hostileSourceId == null ? "hostile" : hostileSourceId;
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
        return lifetimeTicks <= 0 || x < -0.75 || x > 10.5 || y < -0.75 || y > 5.5;
    }
}
