package io.github.finalwave.model.definition;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.plant.PlantStats;
import io.github.finalwave.model.game.entity.plant.PlantStatsCalculator;

import java.util.List;

public final class PlantLevelStats {

    private final String plantName;
    private final String category;
    private final List<String> tags;
    private final int level;
    private final PlantStats stats;
    private final String nextUpgradeSummary;

    public PlantLevelStats(PlantDefinition definition, int level) {
        this.plantName = definition.getName();
        this.category = definition.getCategory();
        this.tags = definition.getTags();
        this.level = level;
        this.stats = PlantStatsCalculator.compute(definition, level);
        this.nextUpgradeSummary = definition.getUpgradeSummary(level);
    }

    public static PlantLevelStats atLevel(PlantDefinition definition, int level) {
        return new PlantLevelStats(definition, level);
    }

    public int getLevel() {
        return level;
    }

    public String getPlantName() {
        return plantName;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getNextUpgradeSummary() {
        return nextUpgradeSummary;
    }

    public int getCost() {
        return stats.cost();
    }

    public int getMaxHealth() {
        return stats.maxHealth();
    }

    public int getDamage() {
        return stats.damage();
    }

    public double getActionInterval() {
        return stats.actionInterval();
    }

    public double getRecharge() {
        return stats.recharge();
    }

    public PlantStats getStats() {
        return stats;
    }
}
