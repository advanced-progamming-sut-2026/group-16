package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.util.Set;

public final class SpecialLevelControllerFactory {

    private SpecialLevelControllerFactory() {
    }

    public static SpecialLevelController create(LevelType type,
                                                 User user,
                                                 UserDatabase userDatabase,
                                                 AdventureMode adventureMode,
                                                 GameSession session,
                                                 ChapterConfig chapter,
                                                 LevelConfig level,
                                                 Set<String> boostedPlants) {
        return switch (type) {
            case CONVEYOR_BELT -> new ConveyBeltLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case LOCKED_PLANTS -> new LockedPlantsLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case SAVE_OUR_SEEDS -> new SaveOurSeedsLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case TIMED_WAR -> new TimedWarLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case NIGHT_OPS -> new NightOpsLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case DEAD_LINE -> new DeadLineLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case LOVE_YOUR_PLANTS -> new LoveYourPlantsLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case PLANT_WHAT_YOU_GET -> new PlantWhatYouGetLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
            case BOSS -> new BossLevelController(
                    user, userDatabase, adventureMode, session, chapter, level, boostedPlants, Set.of());
            default -> throw new IllegalArgumentException("Not a special level type: " + type);
        };
    }
}
