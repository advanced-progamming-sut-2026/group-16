package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.PlantWhatYouGetHandler;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.PlantWhatYouGetView;

import java.util.Set;

public final class PlantWhatYouGetLevelController extends SpecialLevelController {

    public PlantWhatYouGetLevelController(User user,
                                          UserDatabase userDatabase,
                                          AdventureController adventureController,
                                          AdventureMode adventureMode,
                                          GameSession session,
                                          ChapterConfig chapter,
                                          LevelConfig level,
                                          Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new PlantWhatYouGetHandler());
    }

    PlantWhatYouGetLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureController adventureController,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants,
                                   PlantWhatYouGetHandler handler) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                handler);
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        if (getSession().isPlantWhatYouGetActive()) {
            int startingSun = getHandler() instanceof PlantWhatYouGetHandler(int sun)
                    ? sun
                    : PlantWhatYouGetHandler.DEFAULT_STARTING_SUN;
            getPlantWhatYouGetView().showPlantWhatYouGetRule(startingSun);
            if (getSession().isPrepPhaseActive()) {
                getPlantWhatYouGetView().showPrepPhaseHint();
            }
        }
    }

    @Override
    public void onPlantWhatYouGetWavesStarted() {
        getPlantWhatYouGetView().showWavesStartedFromPrep();
    }

    private PlantWhatYouGetView getPlantWhatYouGetView() {
        return (PlantWhatYouGetView) getView();
    }
}
