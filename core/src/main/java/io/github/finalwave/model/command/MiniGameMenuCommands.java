package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MiniGameMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SHOW_GAMES("^show games$"),
    ENTER_GAME("^enter game\\s+-n\\s+(?<name>.+)$"),
    SHOW_STAGES("^show stages$"),
    START_STAGE("^start stage\\s+-n\\s+(?<stage>\\S+)$");

    private final Pattern compiledPattern;

    MiniGameMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
