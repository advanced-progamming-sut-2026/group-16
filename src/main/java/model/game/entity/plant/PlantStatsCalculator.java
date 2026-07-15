package model.game.entity.plant;

import model.definition.plant.PlantDefinition;
import model.definition.plant.PlantUpgrade;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlantStatsCalculator {

    private PlantStatsCalculator() {
    }

    public static PlantStats compute(PlantDefinition definition, int level) {
        int cost = definition.getCost();
        int maxHealth = definition.getBaseHp();
        int damage = definition.getDamage();
        double actionInterval = definition.getActionInterval();
        double recharge = definition.getRecharge();
        Map<String, Double> specialModifiers = new LinkedHashMap<>();

        for (PlantUpgrade upgrade : definition.getUpgrades()) {
            if (upgrade.getLevel() > level) {
                continue;
            }
            switch (upgrade.getType()) {
                case "BUFF_COST" -> cost += (int) upgrade.getValue();
                case "BUFF_HP" -> maxHealth += (int) upgrade.getValue();
                case "BUFF_DAMAGE" -> damage += (int) upgrade.getValue();
                case "BUFF_ACTION_INTERVAL" -> actionInterval += upgrade.getValue();
                case "BUFF_RECHARGE" -> recharge += upgrade.getValue();
                case "SPECIAL_MECHANIC" -> mergeSpecialModifier(specialModifiers, upgrade);
                default -> throw new IllegalStateException("Unknown upgrade type: " + upgrade.getType());
            }
            if (upgrade.hasSpecialTag() && !"SPECIAL_MECHANIC".equals(upgrade.getType())) {
                mergeSpecialModifier(specialModifiers, upgrade);
            }
        }

        return new PlantStats(
                Math.max(cost, 0),
                Math.max(maxHealth, 1),
                Math.max(damage, 0),
                Math.max(actionInterval, 0.0),
                Math.max(recharge, 0.0),
                Map.copyOf(specialModifiers));
    }

    private static void mergeSpecialModifier(Map<String, Double> specialModifiers, PlantUpgrade upgrade) {
        if (upgrade.hasSpecialTag()) {
            specialModifiers.merge(upgrade.getSpecialTag(), upgrade.getValue(), Double::sum);
        }
    }
}
