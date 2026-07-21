package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.SaveOurSeedsHandler;
import model.game.SaveOurSeedsLayout;
import model.game.SaveOurSeedsLayoutFactory;
import model.game.entity.plant.Plant;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.SaveOurSeedsView;

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
