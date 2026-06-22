package model.definition.plant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class PlantDefinition {
    private final int id;
    private final String name;
    private final String category;
    private final List<String> tags;

    private final int cost;
    private final int baseHp;
    private final int damage;
    private final double actionInterval;
    private final double recharge;

    private final String abilityType;
    private final double abilityValue;

    private final String plantFoodType;
    private final double plantFoodValue;

    private final List<PlantUpgrade> upgrades;

    @JsonCreator
    public PlantDefinition(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("category") String category,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("cost") int cost,
            @JsonProperty("baseHp") int baseHp,
            @JsonProperty("damage") int damage,
            @JsonProperty("actionInterval") double actionInterval,
            @JsonProperty("recharge") double recharge,
            @JsonProperty("abilityType") String abilityType,
            @JsonProperty("abilityValue") double abilityValue,
            @JsonProperty("plantFoodType") String plantFoodType,
            @JsonProperty("plantFoodValue") double plantFoodValue,
            @JsonProperty("upgrades") List<PlantUpgrade> upgrades) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        this.cost = cost;
        this.baseHp = baseHp;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
        this.abilityType = abilityType;
        this.abilityValue = abilityValue;
        this.plantFoodType = plantFoodType;
        this.plantFoodValue = plantFoodValue;
        this.upgrades = upgrades == null ? List.of() : List.copyOf(upgrades);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getCost() {
        return cost;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getDamage() {
        return damage;
    }

    public double getActionInterval() {
        return actionInterval;
    }

    public double getRecharge() {
        return recharge;
    }

    public String getAbilityType() {
        return abilityType;
    }

    public double getAbilityValue() {
        return abilityValue;
    }

    public String getPlantFoodType() {
        return plantFoodType;
    }

    public double getPlantFoodValue() {
        return plantFoodValue;
    }

    public List<PlantUpgrade> getUpgrades() {
        return upgrades;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public PlantUpgrade getUpgrade(int level) {
        for (PlantUpgrade u : upgrades) {
            if (u.getLevel() == level) {
                return u;
            }
        }
        return null;
    }

    public int getMaxLevel() {
        return 1 + upgrades.size();
    }

    public String getUpgradeSummary(int currentLevel) {
        PlantUpgrade next = getUpgrade(currentLevel + 1);
        if (next == null) {
            return "Max level reached";
        }
        return next.getType() + " (" + next.getValue() + ")"
                + (next.hasSpecialTag() ? " [" + next.getSpecialTag() + "]" : "");
    }

    public boolean hasPlantFoodEffect() {
        return plantFoodType != null && !plantFoodType.equalsIgnoreCase("NONE");
    }

    @Override
    public String toString() {
        return "PlantDefinition{id=" + id
                + ", name='" + name + '\''
                + ", category='" + category + '\''
                + ", cost=" + cost
                + ", baseHp=" + baseHp
                + ", damage=" + damage
                + ", actionInterval=" + actionInterval
                + ", recharge=" + recharge
                + ", abilityType='" + abilityType + '\''
                + ", upgrades=" + upgrades.size()
                + '}';
    }
}
