package io.github.finalwave.model.definition.armor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ArmorDefinitionDeserializer.class)
public final class ArmorDefinition {

    private final String alias;

    private final String armorType;
    private final int baseHealth;
    private final List<String> armorFlags;
    private final List<String> armorLayers;
    private final List<Double> armorLayerHealth;

    private final String impactSoundEvent;

    private final Map<String, Object> extraProps = new LinkedHashMap<>();

    ArmorDefinition(String alias,
                    String armorType,
                    int baseHealth,
                    List<String> armorFlags,
                    List<String> armorLayers,
                    List<Double> armorLayerHealth,
                    String impactSoundEvent,
                    Map<String, Object> extraProps) {
        this.alias = alias;
        this.armorType = armorType;
        this.baseHealth = baseHealth;
        this.armorFlags = armorFlags == null ? List.of() : List.copyOf(armorFlags);
        this.armorLayers = armorLayers == null ? List.of() : List.copyOf(armorLayers);
        this.armorLayerHealth = armorLayerHealth == null ? List.of() : List.copyOf(armorLayerHealth);
        this.impactSoundEvent = impactSoundEvent;
        if (extraProps != null) {
            this.extraProps.putAll(extraProps);
        }
    }

    public String getAlias() {
        return alias;
    }

    public String getArmorType() {
        return armorType;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public List<String> getArmorFlags() {
        return armorFlags;
    }

    public List<String> getArmorLayers() {
        return armorLayers;
    }

    public List<Double> getArmorLayerHealth() {
        return armorLayerHealth;
    }

    public String getImpactSoundEvent() {
        return impactSoundEvent;
    }

    public boolean isMagnetic() {
        return armorFlags.contains("metallic");
    }

    public boolean isHelm() {
        return armorFlags.contains("helm");
    }

    public boolean isDamageable() {
        return armorFlags.contains("damageable");
    }

    public boolean isDroppable() {
        return armorFlags.contains("droppable");
    }

    public boolean isPassDamage() {
        return armorFlags.contains("passdamage");
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

    @Override
    public String toString() {
        return "ArmorDefinition{" +
                "alias='" + alias + '\'' +
                ", armorType='" + armorType + '\'' +
                ", baseHealth=" + baseHealth +
                ", armorFlags=" + armorFlags +
                ", extraPropsCount=" + extraProps.size() +
                '}';
    }
}
