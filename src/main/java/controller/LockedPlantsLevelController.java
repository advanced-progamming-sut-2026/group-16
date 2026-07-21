package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.LockedPlantsHandler;
import model.game.LockedPlantsRules;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.LockedPlantsView;

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
