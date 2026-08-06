package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.SaveOurSeedsHandler;
import io.github.finalwave.model.game.SaveOurSeedsLayout;
import io.github.finalwave.model.game.SaveOurSeedsLayoutFactory;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.SaveOurSeedsView;

import java.util.Set;

public final class SaveOurSeedsLevelController extends SpecialLevelController {

    public SaveOurSeedsLevelController(User user,
                                       UserDatabase userDatabase,
                                       AdventureController adventureController,
                                       AdventureMode adventureMode,
                                       GameSession session,
                                       ChapterConfig chapter,
                                       LevelConfig level,
                                       Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                SaveOurSeedsLayoutFactory.create(chapter, level));
    }

    public SaveOurSeedsLevelController(User user,
                                       UserDatabase userDatabase,
                                       AdventureController adventureController,
                                       AdventureMode adventureMode,
                                       GameSession session,
                                       ChapterConfig chapter,
                                       LevelConfig level,
                                       Set<String> boostedPlants,
                                       SaveOurSeedsLayout layout) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new SaveOurSeedsHandler(layout));
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        SaveOurSeedsView view = getSaveOurSeedsView();
        view.showProtectedSeeds(getSession().getProtectedSeedPlacements());
        view.showDangerRows(getSession().getDangerRows());
    }

    @Override
    public void onProtectedSeedDestroyed(Plant plant, int x, int y) {
        getSaveOurSeedsView().showProtectedSeedDestroyed(plant.getName(), x, y);
    }

    private SaveOurSeedsView getSaveOurSeedsView() {
        return (SaveOurSeedsView) getView();
    }
}
