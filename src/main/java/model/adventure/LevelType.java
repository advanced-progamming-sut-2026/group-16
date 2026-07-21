package model.adventure;

public enum LevelType {
    NORMAL,
    CONVEYOR_BELT,
    LOCKED_PLANTS,
    SAVE_OUR_SEEDS,
    TIMED_WAR,
    NIGHT_OPS,
    DEAD_LINE,
    LOVE_YOUR_PLANTS,
    PLANT_WHAT_YOU_GET,
    BOSS;

    public boolean isSpecial() {
        return this != NORMAL && this != BOSS;
    }

    public boolean isPlayableNow() {
        return this == NORMAL;
    }
}
