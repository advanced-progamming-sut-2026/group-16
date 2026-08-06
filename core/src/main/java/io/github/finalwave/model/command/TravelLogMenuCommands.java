package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TravelLogMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    TRAVEL_LOG_PAGE("^travel log page\\s+(?<pageName>.+)$");

    private final Pattern compiledPattern;

    TravelLogMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}