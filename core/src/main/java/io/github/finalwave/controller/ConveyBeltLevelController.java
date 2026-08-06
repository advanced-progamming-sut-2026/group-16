package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.ConveyBeltHandler;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.ConveyBeltView;

import java.util.Collection;
import java.util.Set;

public final class ConveyBeltLevelController extends SpecialLevelController {

    public ConveyBeltLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants) {
        this(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                Set.of());
    }

    public ConveyBeltLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants,
                                     Collection<String> availablePlants) {
        super(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
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
