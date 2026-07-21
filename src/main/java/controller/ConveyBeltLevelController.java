package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.ConveyBeltHandler;
import model.game.GameSession;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.ConveyBeltView;

import java.util.Collection;
import java.util.Set;

public final class ConveyBeltLevelController extends SpecialLevelController {

    public ConveyBeltLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureController adventureController,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                Set.of());
    }

    public ConveyBeltLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureController adventureController,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants,
                                     Collection<String> availablePlants) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new ConveyBeltHandler(availablePlants, session.getRandom()));
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        getConveyBeltView().showConveyorBelt(getSession().getConveyorBeltPlants());
    }

    @Override
    public void onConveyorBeltPlantArrived(String plantName) {
        getConveyBeltView().showConveyorBeltPlantArrived(plantName);
    }

    private ConveyBeltView getConveyBeltView() {
        return (ConveyBeltView) getView();
    }
}
