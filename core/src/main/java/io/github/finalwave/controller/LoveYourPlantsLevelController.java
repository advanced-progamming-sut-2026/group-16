package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LoveYourPlantsHandler;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.LoveYourPlantsView;

import java.util.Set;

public final class LoveYourPlantsLevelController extends SpecialLevelController {

    public LoveYourPlantsLevelController(User user,
                                         UserDatabase userDatabase,
                                         AdventureMode adventureMode,
                                         GameSession session,
                                         ChapterConfig chapter,
                                         LevelConfig level,
                                         Set<String> boostedPlants) {
        this(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                new LoveYourPlantsHandler());
    }

    LoveYourPlantsLevelController(User user,
                                  UserDatabase userDatabase,
                                  AdventureMode adventureMode,
                                  GameSession session,
                                  ChapterConfig chapter,
                                  LevelConfig level,
                                  Set<String> boostedPlants,
                                  LoveYourPlantsHandler handler) {
        super(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                handler);
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        if (getSession().isLoveYourPlantsActive()) {
            getLoveYourPlantsView().showLoveYourPlantsRule(getSession().getLoveYourPlantsMaxLoss());
        }
    }

    @Override
    public void onPlantDestroyed(Plant plant, int x, int y) {
        super.onPlantDestroyed(plant, x, y);
        if (getSession().isLoveYourPlantsActive()) {
            getLoveYourPlantsView().showPlantLossStatus(
                    getSession().getPlantsLost(),
                    getSession().getLoveYourPlantsMaxLoss());
        }
    }

    @Override
    public void onLoveYourPlantsLimitReached(int plantsLost, int maxAllowed) {
        getLoveYourPlantsView().showLoveYourPlantsLimitReached(plantsLost, maxAllowed);
    }

    private LoveYourPlantsView getLoveYourPlantsView() {
        return (LoveYourPlantsView) getView();
    }
}
