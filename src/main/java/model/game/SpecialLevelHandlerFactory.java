package model.game;

import model.adventure.LevelType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;


public final class SpecialLevelHandlerFactory {

    private static final Map<LevelType, SpecialLevelHandler> HANDLERS = new EnumMap<>(LevelType.class);

    static {
        HANDLERS.put(LevelType.CONVEYOR_BELT, new ConveyBeltHandler());
        HANDLERS.put(LevelType.LOCKED_PLANTS, new LockedPlantsHandler());
        HANDLERS.put(LevelType.SAVE_OUR_SEEDS, new SaveOurSeedsHandler());
        HANDLERS.put(LevelType.TIMED_WAR, new TimedWarHandler());
        HANDLERS.put(LevelType.NIGHT_OPS, new NightOpsHandler());
        HANDLERS.put(LevelType.DEAD_LINE, new DeadLineHandler());
        HANDLERS.put(LevelType.LOVE_YOUR_PLANTS, new LoveYourPlantsHandler());
        HANDLERS.put(LevelType.PLANT_WHAT_YOU_GET, new PlantWhatYouGetHandler());
    }

    private SpecialLevelHandlerFactory() {
    }

    public static Optional<SpecialLevelHandler> create(LevelType type) {
        if (type == null || type == LevelType.NORMAL || type == LevelType.BOSS) {
            return Optional.empty();
        }
        return Optional.ofNullable(HANDLERS.get(type));
    }
}
