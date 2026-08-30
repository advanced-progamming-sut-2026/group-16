package io.github.finalwave.network.sync;

public final class UpdateAdventurePayload {
    private String unlockedChapter;
    private int difficultyLevel;
    private String completedLevels;

    public UpdateAdventurePayload() {
    }

    public String getUnlockedChapter() {
        return unlockedChapter;
    }

    public void setUnlockedChapter(String unlockedChapter) {
        this.unlockedChapter = unlockedChapter;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getCompletedLevels() {
        return completedLevels;
    }

    public void setCompletedLevels(String completedLevels) {
        this.completedLevels = completedLevels;
    }
}
