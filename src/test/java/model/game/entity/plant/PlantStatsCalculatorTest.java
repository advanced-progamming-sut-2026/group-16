package model.game.entity.plant;

import model.definition.plant.PlantDefinition;
import model.definition.plant.PlantUpgrade;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlantStatsCalculatorTest {

    @Test
    void mergesSpecialTagFromNonSpecialMechanicUpgrades() {
        PlantDefinition definition = new PlantDefinition(
                1, "Test", "SHOOTER", List.of(),
                100, 300, 20, 1.5, 5.0,
                "PROJECTILE_ATTACK", 1.0,
                "NONE", 0.0,
                List.of(
                        new PlantUpgrade(2, "BUFF_DAMAGE", 10, "ADDITIONAL_PIERCE"),
                        new PlantUpgrade(3, "SPECIAL_MECHANIC", 0.5, "CHARGE_REDUCTION")
                ));
        PlantStats stats = PlantStatsCalculator.compute(definition, 3);
        assertEquals(30, stats.damage());
        assertEquals(10.0, stats.specialModifier("ADDITIONAL_PIERCE"));
        assertEquals(0.5, stats.specialModifier("CHARGE_REDUCTION"));
    }

    @Test
    void appliesAllBuffUpgradeTypes() {
        PlantDefinition definition = new PlantDefinition(
                2, "BuffTest", "SHOOTER", List.of(),
                50, 100, 10, 2.0, 3.0,
                "SUN_PRODUCTION", 25.0,
                "NONE", 0.0,
                List.of(
                        new PlantUpgrade(2, "BUFF_COST", 25, null),
                        new PlantUpgrade(2, "BUFF_HP", 50, null),
                        new PlantUpgrade(2, "BUFF_DAMAGE", 5, null),
                        new PlantUpgrade(2, "BUFF_ACTION_INTERVAL", -0.5, null),
                        new PlantUpgrade(2, "BUFF_RECHARGE", -1.0, null)
                ));
        PlantStats stats = PlantStatsCalculator.compute(definition, 2);
        assertEquals(75, stats.cost());
        assertEquals(150, stats.maxHealth());
        assertEquals(15, stats.damage());
        assertEquals(1.5, stats.actionInterval(), 0.001);
        assertEquals(2.0, stats.recharge(), 0.001);
    }
}
