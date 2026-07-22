package model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum VaseBreakerMenuCommands implements Command {
    SMASH_VASE("^smash vase\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    PLANT_SEED("^plant seed\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    ADVANCE_TIME("^advance time\\s+-t\\s+(?<count>\\S+)\\s+ticks$"),
    SHOW_MAP("^show map$"),
    ZOMBIES_INFO("^zombies info$"),
    RELEASE_THE_NUKE("^release the nuke$"),
    MENU_EXIT("^menu exit$");

    private final Pattern compiledPattern;

    VaseBreakerMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
