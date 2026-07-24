package model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum BeghouledMenuCommands implements Command {
    SWAP_PLANTS("^swap plants\\s+-a\\s+\\((?<ax>[^,]+),\\s*(?<ay>[^)]+)\\)"
            + "\\s+-b\\s+\\((?<bx>[^,]+),\\s*(?<by>[^)]+)\\)$"),
    SHOW_UPGRADES("^show upgrades$"),
    UPGRADE_PLANT("^upgrade plant\\s+-t\\s+(?<type>.+)$"),
    ADVANCE_TIME("^advance time\\s+-t\\s+(?<count>\\S+)\\s+ticks$"),
    SHOW_MAP("^show map$"),
    ZOMBIES_INFO("^zombies info$"),
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$");

    private final Pattern compiledPattern;

    BeghouledMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
