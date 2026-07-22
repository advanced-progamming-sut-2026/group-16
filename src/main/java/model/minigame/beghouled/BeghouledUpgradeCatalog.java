package model.minigame.beghouled;

import java.util.List;
import java.util.Optional;

public final class BeghouledUpgradeCatalog {

    private final List<BeghouledUpgradeRule> rules;

    public BeghouledUpgradeCatalog(List<BeghouledUpgradeRule> rules) {
        this.rules = rules == null ? List.of() : List.copyOf(rules);
    }

    public List<BeghouledUpgradeRule> getRules() {
        return rules;
    }

    public Optional<BeghouledUpgradeRule> findRule(String fromPlant) {
        if (fromPlant == null) {
            return Optional.empty();
        }
        for (BeghouledUpgradeRule rule : rules) {
            if (fromPlant.equals(rule.fromPlant())) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    public static BeghouledUpgradeCatalog allRules() {
        return new BeghouledUpgradeCatalog(List.of(
                new BeghouledUpgradeRule("Peashooter", "Repeater", 500),
                new BeghouledUpgradeRule("Repeater", "Mega Gatling Pea", 1500),
                new BeghouledUpgradeRule("Wall-nut", "Tall-nut", 500),
                new BeghouledUpgradeRule("Puff-shroom", "Fume-shroom", 250),
                new BeghouledUpgradeRule("Cabbage-pult", "Melon-pult", 1000),
                new BeghouledUpgradeRule("Melon-pult", "Winter Melon", 750)
        ));
    }

    public static BeghouledUpgradeCatalog stageOne() {
        return new BeghouledUpgradeCatalog(List.of(
                new BeghouledUpgradeRule("Peashooter", "Repeater", 500),
                new BeghouledUpgradeRule("Wall-nut", "Tall-nut", 500)
        ));
    }

    public static BeghouledUpgradeCatalog stageTwo() {
        return new BeghouledUpgradeCatalog(List.of(
                new BeghouledUpgradeRule("Repeater", "Mega Gatling Pea", 1500),
                new BeghouledUpgradeRule("Puff-shroom", "Fume-shroom", 250),
                new BeghouledUpgradeRule("Melon-pult", "Winter Melon", 750)
        ));
    }

    public static BeghouledUpgradeCatalog stageThree() {
        return allRules();
    }
}
