package model.game.entity.plant;

import java.util.Map;

public record PlantStats(
        int cost,
        int maxHealth,
        int damage,
        double actionInterval,
        double recharge,
        Map<String, Double> specialModifiers) {

    public double specialModifier(String tag) {
        return specialModifiers.getOrDefault(tag, 0.0);
    }

    public boolean hasSpecialModifier(String tag) {
        return specialModifiers.containsKey(tag);
    }
}
