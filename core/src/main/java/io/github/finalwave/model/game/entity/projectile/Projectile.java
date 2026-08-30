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
    private final double laneYOffset;
    private final boolean giantPea;
    private final boolean fumePlantFood;
    private final boolean cabbagePlantFood;
    private final boolean melonPlantFood;
    private final boolean pepperPlantFood;
    private int fumeAgeTicks;
    private double landX = Double.NaN;
    private double rowPosition;
    private double velocityX;
    private double velocityY;
    private boolean grapeshotGrape;

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining) {
        this(row, x, damage, profile, effect, source, pierceRemaining, 0);
    }

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining,
                      double laneYOffset) {
        this(row, x, damage, profile, effect, source, pierceRemaining, laneYOffset, false);
    }

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining,
                      double laneYOffset, boolean giantPea) {
        this(row, x, damage, profile, effect, source, pierceRemaining, laneYOffset, giantPea, false);
    }

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining,
                      double laneYOffset, boolean giantPea, boolean fumePlantFood) {
        this(row, x, damage, profile, effect, source, pierceRemaining, laneYOffset, giantPea,
                fumePlantFood, false);
    }

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining,
                      double laneYOffset, boolean giantPea, boolean fumePlantFood,
                      boolean cabbagePlantFood) {
        this(row, x, damage, profile, effect, source, pierceRemaining, laneYOffset, giantPea,
                fumePlantFood, cabbagePlantFood, false);
    }

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining,
                      double laneYOffset, boolean giantPea, boolean fumePlantFood,
                      boolean cabbagePlantFood, boolean melonPlantFood) {
        this(row, x, damage, profile, effect, source, pierceRemaining, laneYOffset, giantPea,
                fumePlantFood, cabbagePlantFood, melonPlantFood, false);
    }

    public Projectile(int row, double x, int damage, ProjectileProfile profile,
                      ProjectileEffect effect, Plant source, int pierceRemaining,
                      double laneYOffset, boolean giantPea, boolean fumePlantFood,
                      boolean cabbagePlantFood, boolean melonPlantFood, boolean pepperPlantFood) {
        this.y = row;
        this.x = x;
        this.damage = damage;
        this.profile = profile;
        this.effect = effect == null ? ProjectileEffect.GENERIC : effect;
        this.source = source;
        this.pierceRemaining = pierceRemaining;
        this.lifetimeTicks = 200;
        this.fromZombie = source == null;
        this.laneYOffset = laneYOffset;
        this.giantPea = giantPea;
        this.fumePlantFood = fumePlantFood;
        this.cabbagePlantFood = cabbagePlantFood;
        this.melonPlantFood = melonPlantFood;
        this.pepperPlantFood = pepperPlantFood;
    }

    public static Projectile grapeshotGrape(Plant source,
                                            double x,
                                            double rowPosition,
                                            double velocityX,
                                            double velocityY,
                                            int damage) {
        int row = (int) Math.round(rowPosition);
        Projectile projectile = new Projectile(
                row,
                x,
                damage,
                ProjectileProfile.bouncing(),
                ProjectileEffect.GRAPE,
                source,
                0);
        projectile.rowPosition = rowPosition;
        projectile.velocityX = velocityX;
        projectile.velocityY = velocityY;
        projectile.grapeshotGrape = true;
        projectile.setLifetimeTicks(GrapeshotMuzzles.grapeLifetimeTicks());
        return projectile;
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
                original.getEffect(), null, 0, original.getLaneYOffset(), original.isGiantPea(),
                original.isFumePlantFood(), original.isCabbagePlantFood(),
                original.isMelonPlantFood(), original.isPepperPlantFood());
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

    public double getLaneYOffset() {
        return laneYOffset;
    }

    public boolean isGiantPea() {
        return giantPea;
    }

    public boolean isFumePlantFood() {
        return fumePlantFood;
    }

    public boolean isCabbagePlantFood() {
        return cabbagePlantFood;
    }

    public boolean isMelonPlantFood() {
        return melonPlantFood;
    }

    public boolean isPepperPlantFood() {
        return pepperPlantFood;
    }

    public boolean isGrapeshotGrape() {
        return grapeshotGrape;
    }

    public double getRowPosition() {
        return grapeshotGrape ? rowPosition : y;
    }

    public void setRowPosition(double rowPosition) {
        this.rowPosition = rowPosition;
        this.y = rowPosition;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocity(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.vx = vx;
        this.vy = vy;
        if (!grapeshotGrape) {
            this.directed = true;
            if (vx < 0) {
                this.reverse = true;
            }
        }
    }

    public boolean hasLandX() {
        return !Double.isNaN(landX);
    }

    public double getLandX() {
        return landX;
    }

    public void setLandX(double landX) {
        this.landX = landX;
    }

    public void advanceFumeAge() {
        fumeAgeTicks++;
    }

    public int getFumeAgeTicks() {
        return fumeAgeTicks;
    }

    public void setLifetimeTicks(int ticks) {
        this.lifetimeTicks = Math.max(1, ticks);
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
        if (grapeshotGrape) {
            return lifetimeTicks <= 0;
        }
        return lifetimeTicks <= 0 || x < -0.75 || x > 10.5 || y < -0.75 || y > 5.5;
    }
}
