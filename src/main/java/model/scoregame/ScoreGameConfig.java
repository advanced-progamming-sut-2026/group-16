package model.scoregame;

import model.adventure.LevelConfig;

import java.util.List;

public final class ScoreGameConfig {
    public static final int DIFFICULTY = 3;
    public static final String CHAPTER_ID = "score-game";
    public static final String LEVEL_ID = "score-game-daily";

    private static final List<String> ZOMBIE_POOL = List.of(
            "ZombieDefault", "ZombieArmor1", "ZombieArmor2",
            "ZombieRa", "ZombieExplorer", "ZombieTombRaiser");

    private ScoreGameConfig() {
    }

    public static LevelConfig level() {
        return LevelConfig.normal(1, 3, 50, 300, ZOMBIE_POOL);
    }
}
