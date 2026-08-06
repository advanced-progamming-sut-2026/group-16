package io.github.finalwave.view.api;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;

import java.util.List;

public interface AdventureView extends View {

    void showAdventureMenu(ChapterConfig chapter);

    void showLevels(ChapterConfig chapter, List<LevelConfig> levels);

    void showProgress(String progressText);

    void showCurrentMenu(String chapterName);

    void errorChapterLocked(String chapterName);

    void errorLevelLocked(int level);

    void errorLevelNotFound(int level);

    void errorSpecialNotImplemented(String levelType);

    void errorBossNotImplemented();

    void errorInvalidCommand();

    void errorInvalidLevelNumber();
}
