package model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ZombotanyMenuCommands implements Command {
    ADVANCE_TIME("^advance time\\s+-t\\s+(?<count>\\S+)\\s+ticks$"),
    COLLECT_SUN("^collect sun\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    SHOW_SUN_AMOUNT("^show sun amount$"),
    PLANT_PLANT("^plant plant\\s+-t\\s+(?<type>.+?)\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    PLUCK_PLANT("^pluck plant\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    SHOW_MAP("^show map$"),
    ZOMBIES_INFO("^zombies info$"),
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$");

    private final Pattern compiledPattern;

    ZombotanyMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
