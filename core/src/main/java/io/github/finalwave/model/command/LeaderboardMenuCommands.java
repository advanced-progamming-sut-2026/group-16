package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LeaderboardMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SORT("^sort\\s+-c\\s+(?<column>\\S+)(?:\\s+-o\\s+(?<order>asc|desc))?$"),
    REFRESH("^refresh$");

    private final Pattern compiledPattern;

    LeaderboardMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
