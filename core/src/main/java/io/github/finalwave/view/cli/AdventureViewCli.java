package io.github.finalwave.view.cli;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.view.api.AdventureView;

import java.util.List;

public class AdventureViewCli extends CliView implements AdventureView {

    @Override
    public void showAdventureMenu(ChapterConfig chapter) {
        displayMessage("Adventure: " + chapter.getDisplayName());
        displayMessage("Commands: show levels | start level -n <n> | show progress | menu exit");
    }

    @Override
    public void showLevels(ChapterConfig chapter, List<LevelConfig> levels) {
        displayMessage("Levels in " + chapter.getDisplayName() + ":");
        for (LevelConfig level : levels) {
            displayMessage("- Level " + level.getIndex()
                    + " [" + level.getType() + "]"
                    + (level.isPlayableNow() ? " (playable)" : " (not implemented)"));
        }
    }

    @Override
    public void showProgress(String progressText) {
        displayMessage(progressText);
    }

    @Override
    public void showCurrentMenu(String chapterName) {
        displayMessage("Current menu: adventure (" + chapterName + ")");
    }

    @Override
    public void errorChapterLocked(String chapterName) {
        displayError("Chapter " + chapterName + " is locked.");
    }

    @Override
    public void errorLevelLocked(int level) {
        displayError("Level " + level + " is locked.");
    }

    @Override
    public void errorLevelNotFound(int level) {
        displayError("Level " + level + " does not exist.");
    }

    @Override
    public void errorSpecialNotImplemented(String levelType) {
        displayError("Special level " + levelType + " is not implemented yet.");
    }

    @Override
    public void errorBossNotImplemented() {
        displayError("Boss level is not implemented yet.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid adventure command.");
    }

    @Override
    public void errorInvalidLevelNumber() {
        displayError("Invalid level number.");
    }
}
