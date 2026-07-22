package model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ScoreGameMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$");

    private final Pattern compiledPattern;

    ScoreGameMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
