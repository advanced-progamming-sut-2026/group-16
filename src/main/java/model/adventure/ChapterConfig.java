package model.adventure;

import java.util.List;

public final class ChapterConfig {

    private final ChapterId id;
    private final ChapterRules rules;
    private final List<LevelConfig> levels;

    public ChapterConfig(ChapterId id, ChapterRules rules, List<LevelConfig> levels) {
        if (id == null) {
            throw new IllegalArgumentException("chapter id must not be null");
        }
        if (rules == null) {
            throw new IllegalArgumentException("rules must not be null");
        }
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("levels must not be empty");
        }
        this.id = id;
        this.rules = rules;
        this.levels = List.copyOf(levels);
    }

    public ChapterId getId() {
        return id;
    }

    public String getDisplayName() {
        return id.getDisplayName();
    }

    public ChapterRules getRules() {
        return rules;
    }

    public List<LevelConfig> getLevels() {
        return levels;
    }

    public LevelConfig getLevel(int index) {
        for (LevelConfig level : levels) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

    public LevelConfig getFirstNormalLevel() {
        for (LevelConfig level : levels) {
            if (level.getType() == LevelType.NORMAL) {
                return level;
            }
        }
        return null;
    }
}
