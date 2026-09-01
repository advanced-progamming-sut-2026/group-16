package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.definition.zombie.ZombieDefinition;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.GargantuarImpThrow;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;
import io.github.finalwave.model.game.entity.zombie.ZombieBehaviorDefaults;

public final class GargantuarImpThrowBehavior implements ZombieBehavior {

    private static final String DEFAULT_IMP = "ZombieImp";
    private static final String THROW_CLIP = "cannon_fire";
    private static final String THROW_CLIP_FALLBACK = "fire";
    private static final double DEFAULT_HEALTH_THRESHOLD = 0.5;
    private static final int DEFAULT_MIN_LAND_COLUMN = 2;
    private static final int DEFAULT_MAX_LAND_COLUMN = 4;
    private static final double SPAWN_OFFSET_X_TILES = 0.35;
    private static final double SPAWN_FORWARD_TILES = 0.0;
    private static final double SPAWN_LIFT_TILES = 0.94;
    private static final int IMP_RELEASE_TICKS_AFTER_START = 0;

    private final String impAlias;
    private final double healthThreshold;
    private final int throwTicks;
    private final int flyTicks;
    private final int landTicks;
    private final int minLandColumn;
    private final int maxLandColumn;
    private final int minGargColumnToThrow;
    private final double spawnOffsetXTiles;
    private final double spawnForwardTiles;
    private final double spawnLiftTiles;
    private final double impApexScale;
    private boolean released;

    private GargantuarImpThrowBehavior(
            String impAlias,
            double healthThreshold,
            int throwTicks,
            int flyTicks,
            int landTicks,
            int minLandColumn,
            int maxLandColumn,
            int minGargColumnToThrow,
            double spawnOffsetXTiles,
            double spawnForwardTiles,
            double spawnLiftTiles,
            double impApexScale) {
        this.impAlias = impAlias;
        this.healthThreshold = healthThreshold;
        this.throwTicks = throwTicks;
        this.flyTicks = flyTicks;
        this.landTicks = landTicks;
        this.minLandColumn = minLandColumn;
        this.maxLandColumn = maxLandColumn;
        this.minGargColumnToThrow = minGargColumnToThrow;
        this.spawnOffsetXTiles = spawnOffsetXTiles;
        this.spawnForwardTiles = spawnForwardTiles;
        this.spawnLiftTiles = spawnLiftTiles;
        this.impApexScale = impApexScale;
    }

    public static GargantuarImpThrowBehavior fromDefinition(ZombieDefinition definition) {
        double threshold = DEFAULT_HEALTH_THRESHOLD;
        Object layers = definition.getExtra("HealthThresholdToImpAmmoLayers");
        if (layers instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof java.util.Map<?, ?> map) {
                Object val = map.get("HealthPercentThrowImp");
                if (val instanceof Number number) {
                    threshold = number.doubleValue();
                }
            }
        }
        int minLand = ZombieBehaviorDefaults.integer(definition, "ImpTargetColumn", DEFAULT_MIN_LAND_COLUMN);
        int throwTicks = ZombieBehaviorDefaults.ticks(definition, "ThrowImpDuration", 1.0);
        int flyTicks = Math.max(
                ZombieBehaviorDefaults.ticks(definition, "ImpFlightTime", 1.5),
                15);
        int landTicks = 6;
        double spawnOffsetX = SPAWN_OFFSET_X_TILES;
        double spawnLift = SPAWN_LIFT_TILES;
        Object offset = definition.getExtra("ImpSpawnOffset");
        if (offset instanceof java.util.Map<?, ?> map) {
            Object x = map.get("x");
            Object z = map.get("z");
            if (x instanceof Number xNum) {
                spawnOffsetX = Math.abs(xNum.doubleValue()) / 155.0;
            }
            if (z instanceof Number zNum) {
                spawnLift = Math.max(SPAWN_LIFT_TILES, zNum.doubleValue() / 170.0);
            }
        }
        double apexScale = 0.85;
        Double impApex = definition.getExtraAsDouble("ImpApex");
        if (impApex != null && impApex > 0) {
            apexScale = Math.min(1.15, impApex / 250.0);
        }
        return new GargantuarImpThrowBehavior(
                DEFAULT_IMP,
                threshold,
                throwTicks,
                flyTicks,
                landTicks,
                minLand,
                DEFAULT_MAX_LAND_COLUMN,
                minLand + 1,
                spawnOffsetX,
                SPAWN_FORWARD_TILES,
                spawnLift,
                apexScale);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (released || zombie.isGargantuarImpSpent()) {
            return;
        }
        if (zombie.getHealthRatio() > healthThreshold) {
            return;
        }
        if (!canThrowFromPosition(zombie, context)) {
            return;
        }
        int landColumn = resolveLandColumn(zombie, context);
        double pocketX = resolveImpPocketX(zombie.getX(), context.getColCount());
        double distance = Math.abs(pocketX - (landColumn + 0.5));
        double arcApex = Math.min(impApexScale, 0.32 + distance * 0.11);
        if (!beginThrowAbility(zombie)) {
            return;
        }
        zombie.queueGargantuarImpThrow(new GargantuarImpThrow(
                impAlias,
                landColumn,
                spawnOffsetXTiles,
                spawnForwardTiles,
                spawnLiftTiles,
                arcApex,
                flyTicks,
                landTicks,
                throwTicks,
                IMP_RELEASE_TICKS_AFTER_START));
        released = true;
    }

    private boolean beginThrowAbility(Zombie zombie) {
        if (zombie.beginAbility(THROW_CLIP, throwTicks)) {
            return true;
        }
        return zombie.beginAbility(THROW_CLIP_FALLBACK, throwTicks);
    }

    private double resolveImpPocketX(double gargX, int cols) {
        double x = gargX - spawnOffsetXTiles - spawnForwardTiles;
        return Math.max(0, Math.min(cols - 1, x));
    }

    private boolean canThrowFromPosition(Zombie zombie, GameContext context) {
        int cols = context.getColCount();
        if (cols <= 0) {
            return false;
        }
        int gargColumn = (int) Math.floor(zombie.getX());
        if (gargColumn < minGargColumnToThrow) {
            return false;
        }
        int rightEdgeColumn = cols - 1;
        return gargColumn < rightEdgeColumn;
    }

    private int resolveLandColumn(Zombie zombie, GameContext context) {
        int gargColumn = (int) Math.floor(zombie.getX());
        int target = gargColumn - 3;
        int minCol = Math.max(1, minLandColumn - 1);
        int maxCol = Math.min(minLandColumn, context.getColCount() - 1);
        return Math.max(minCol, Math.min(maxCol, target));
    }
}
