package model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum WalnutBowlingMenuCommands implements Command {
    PLANT_PLANT("^plant plant\\s+-t\\s+(?<type>.+?)\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    SHOW_CONVEYOR_BELT("^show conveyor belt$"),
    ADVANCE_TIME("^advance time\\s+-t\\s+(?<count>\\S+)\\s+ticks$"),
    SHOW_MAP("^show map$"),
    ZOMBIES_INFO("^zombies info$"),
    MENU_EXIT("^menu exit$");

    private final Pattern compiledPattern;

    WalnutBowlingMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
