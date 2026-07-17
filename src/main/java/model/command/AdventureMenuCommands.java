package model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum AdventureMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SHOW_LEVELS("^show levels$"),
    START_LEVEL("^start level\\s+-n\\s+(?<level>\\S+)$"),
    SHOW_PROGRESS("^show progress$");

    private final Pattern compiledPattern;

    AdventureMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
