package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SettingMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    CHANGE_DIFFICULTY("^menu settings change-difficulty\\s+-l\\s+(?<difficultyLevel>\\S+)$");

    private final Pattern compiledPattern;

    SettingMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}