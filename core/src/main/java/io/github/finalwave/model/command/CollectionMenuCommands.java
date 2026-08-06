package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CollectionMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SHOW_PLANTS("^menu collection show-plants$"),
    SHOW_ALL_PLANTS("^menu collection show-all-plants$"),
    SHOW_ZOMBIES("^menu collection show-zombies$"),
    SHOW_ALL_ZOMBIES("^menu collection show-all-zombies$"),
    SHOW_PLANT("^menu collection show-plant\\s+-p\\s+(?<plantName>.+)$"),
    SHOW_ZOMBIE("^menu collection show-zombie\\s+-z\\s+(?<zombieName>.+)$"),
    UPGRADE_PLANT("^menu collection upgrade-plant\\s+-p\\s+(?<plantName>.+)$"),
    PURCHASE_PLANT("^menu collection purchase-plant\\s+-p\\s+(?<plantName>.+)$");

    private final Pattern compiledPattern;

    CollectionMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}