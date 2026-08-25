package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.BossHandler;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.ConveyBeltView;

import java.util.Collection;
import java.util.Set;

public final class BossLevelController extends SpecialLevelController {

    public BossLevelController(User user,
                               UserDatabase userDatabase,
                               AdventureMode adventureMode,
                               GameSession session,
                               ChapterConfig chapter,
                               LevelConfig level,
                               Set<String> boostedPlants,
                               Collection<String> availablePlants) {
        super(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                new BossHandler(chapter == null ? null : chapter.getId(),
                        availablePlants, session.getRandom()));
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

    @Override
    public void onBossPhaseChanged(int phase) {
    }

    @Override
    public void onBossDefeated() {
    }

    private ConveyBeltView getConveyBeltView() {
        return (ConveyBeltView) getView();
    }
}
