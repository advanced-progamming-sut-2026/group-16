package model.definition.zombie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ZombieDefinitionDeserializer.class)
public final class ZombieDefinition {

    private static final Pattern RTID_PATTERN = Pattern.compile("^RTID\\(([^@]+)@[^)]*\\)$");

    private final String alias;
    private final String objClass;

    private final int hitpoints;
    private final int eatDps;
    private final double speed;
    private final int wavePointCost;
    private final int weight;
    private final int cost;
    private final boolean canSpawnPlantFood;
    private final String groundTrackName;

    private final Rect attackRect;
    private final Rect hitRect;
    private final Point2D artCenter;
    private final ShadowOffset shadowOffset;
    private final List<ScaledProp> scaledProps;
    private final List<ZombieStat> zombieStats;

    private final List<String> armorAliases;

    private final Map<String, Object> extraProps = new LinkedHashMap<>();

    ZombieDefinition(String alias,
                     String objClass,
                     int hitpoints,
                     int eatDps,
                     double speed,
                     int wavePointCost,
                     int weight,
                     int cost,
                     boolean canSpawnPlantFood,
                     String groundTrackName,
                     Rect attackRect,
                     Rect hitRect,
                     Point2D artCenter,
                     ShadowOffset shadowOffset,
                     List<ScaledProp> scaledProps,
                     List<ZombieStat> zombieStats,
                     List<String> armorRtids,
                     Map<String, Object> extraProps) {
        this.alias = alias;
        this.objClass = objClass;
        this.hitpoints = hitpoints;
        this.eatDps = eatDps;
        this.speed = speed;
        this.wavePointCost = wavePointCost;
        this.weight = weight;
        this.cost = cost;
        this.canSpawnPlantFood = canSpawnPlantFood;
        this.groundTrackName = groundTrackName;
        this.attackRect = attackRect;
        this.hitRect = hitRect;
        this.artCenter = artCenter;
        this.shadowOffset = shadowOffset;
        this.scaledProps = scaledProps == null ? List.of() : List.copyOf(scaledProps);
        this.zombieStats = zombieStats == null ? List.of() : List.copyOf(zombieStats);
        this.armorAliases = resolveArmorAliases(armorRtids);
        if (extraProps != null) {
            this.extraProps.putAll(extraProps);
        }
    }

    private static List<String> resolveArmorAliases(List<String> rtids) {
        if (rtids == null || rtids.isEmpty()) {
            return List.of();
        }
        List<String> resolved = new ArrayList<>(rtids.size());
        for (String rtid : rtids) {
            Matcher m = RTID_PATTERN.matcher(rtid);
            resolved.add(m.matches() ? m.group(1) : rtid);
        }
        return Collections.unmodifiableList(resolved);
    }

    public String getAlias() {
        return alias;
    }

    public String getObjClass() {
        return objClass;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public int getEatDps() {
        return eatDps;
    }

    public double getSpeed() {
        return speed;
    }

    public int getWavePointCost() {
        return wavePointCost;
    }

    public int getWeight() {
        return weight;
    }

    public int getCost() {
        return cost;
    }

    public boolean canSpawnPlantFood() {
        return canSpawnPlantFood;
    }

    public String getGroundTrackName() {
        return groundTrackName;
    }

    public Rect getAttackRect() {
        return attackRect;
    }

    public Rect getHitRect() {
        return hitRect;
    }

    public Point2D getArtCenter() {
        return artCenter;
    }

    public ShadowOffset getShadowOffset() {
        return shadowOffset;
    }

    public List<ScaledProp> getScaledProps() {
        return scaledProps;
    }

    public List<ZombieStat> getZombieStats() {
        return zombieStats;
    }

    public boolean hasArmor() {
        return !armorAliases.isEmpty();
    }

    public List<String> getArmorAliases() {
        return armorAliases;
    }

    public String getToughnessLabel() {
        return zombieStats.stream()
                .filter(s -> "toughness".equalsIgnoreCase(s.getType()))
                .map(ZombieStat::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getSpeedLabel() {
        return zombieStats.stream()
                .filter(s -> "speed".equalsIgnoreCase(s.getType()))
                .map(ZombieStat::getValue)
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> getExtraProps() {
        return Collections.unmodifiableMap(extraProps);
    }

    public boolean hasExtra(String key) {
        return extraProps.containsKey(key);
    }

    public Object getExtra(String key) {
        return extraProps.get(key);
    }

    public <T> T getExtra(String key, Class<T> type) {
        Object value = extraProps.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public Double getExtraAsDouble(String key) {
        Object value = extraProps.get(key);
        return (value instanceof Number n) ? n.doubleValue() : null;
    }

    @Override
    public String toString() {
        return "ZombieDefinition{" +
                "alias='" + alias + '\'' +
                ", objClass='" + objClass + '\'' +
                ", hitpoints=" + hitpoints +
                ", eatDps=" + eatDps +
                ", speed=" + speed +
                ", wavePointCost=" + wavePointCost +
                ", weight=" + weight +
                ", armorAliases=" + armorAliases +
                ", extraPropsCount=" + extraProps.size() +
                '}';
    }
}
