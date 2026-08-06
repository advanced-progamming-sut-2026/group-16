package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LockedPlantsHandler;
import io.github.finalwave.model.game.LockedPlantsRules;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.LockedPlantsView;

import java.util.ArrayList;
import java.util.Set;

public final class LockedPlantsLevelController extends SpecialLevelController {

    private final LockedPlantsRules rules;

    public LockedPlantsLevelController(User user,
                                       UserDatabase userDatabase,
                                       AdventureController adventureController,
                                       AdventureMode adventureMode,
                                       GameSession session,
                                       ChapterConfig chapter,
                                       LevelConfig level,
                                       Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants, null);
    }

    public LockedPlantsLevelController(User user,
                                       UserDatabase userDatabase,
                                       AdventureController adventureController,
                                       AdventureMode adventureMode,
                                       GameSession session,
                                       ChapterConfig chapter,
                                       LevelConfig level,
                                       Set<String> boostedPlants,
                                       LockedPlantsRules rules) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new LockedPlantsHandler(rules));
        this.rules = rules;
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        if (rules != null) {
            getLockedPlantsView().showLockedPlantsSummary(
                    rules.getMode(), new ArrayList<>(rules.getLockedPlants()));
        }
    }

    private LockedPlantsView getLockedPlantsView() {
        return (LockedPlantsView) getView();
    }
}
