package model.game;

import model.adventure.LevelType;

public final class LockedPlantsHandler implements SpecialLevelHandler {

    private final LockedPlantsRules rules;

    public LockedPlantsHandler(LockedPlantsRules rules) {
        this.rules = rules;
    }

    public LockedPlantsHandler() {
        this(null);
    }

    public LockedPlantsRules getRules() {
        return rules;
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.LOCKED_PLANTS;
    }

    @Override
    public void onLevelStart(GameSession session) {
        if (rules != null) {
            session.activateLockedPlants(rules);
        }
    }
}
