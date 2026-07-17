package view.api;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;

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
