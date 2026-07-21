package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.adventure.LevelType;
import model.game.GameSession;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;

import java.util.Set;

public final class SpecialLevelControllerFactory {

    private SpecialLevelControllerFactory() {
    }

    public static SpecialLevelController create(LevelType type,
                                                 User user,
                                                 UserDatabase userDatabase,
                                                 AdventureController adventureController,
                                                 AdventureMode adventureMode,
                                                 GameSession session,
                                                 ChapterConfig chapter,
                                                 LevelConfig level,
                                                 Set<String> boostedPlants) {
        return switch (type) {
            case CONVEYOR_BELT -> new ConveyBeltLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case LOCKED_PLANTS -> new LockedPlantsLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case SAVE_OUR_SEEDS -> new SaveOurSeedsLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case TIMED_WAR -> new TimedWarLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case NIGHT_OPS -> new NightOpsLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case DEAD_LINE -> new DeadLineLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case LOVE_YOUR_PLANTS -> new LoveYourPlantsLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            case PLANT_WHAT_YOU_GET -> new PlantWhatYouGetLevelController(
                    user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
            default -> throw new IllegalArgumentException("Not a special level type: " + type);
        };
    }
}
