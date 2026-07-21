package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.LoveYourPlantsHandler;
import model.game.entity.plant.Plant;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.LoveYourPlantsView;

import java.util.Set;

public final class LoveYourPlantsLevelController extends SpecialLevelController {

    public LoveYourPlantsLevelController(User user,
                                         UserDatabase userDatabase,
                                         AdventureController adventureController,
                                         AdventureMode adventureMode,
                                         GameSession session,
                                         ChapterConfig chapter,
                                         LevelConfig level,
                                         Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new LoveYourPlantsHandler());
    }

    LoveYourPlantsLevelController(User user,
                                  UserDatabase userDatabase,
                                  AdventureController adventureController,
                                  AdventureMode adventureMode,
                                  GameSession session,
                                  ChapterConfig chapter,
                                  LevelConfig level,
                                  Set<String> boostedPlants,
                                  LoveYourPlantsHandler handler) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
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
