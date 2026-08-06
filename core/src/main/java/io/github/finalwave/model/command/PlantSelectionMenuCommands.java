package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PlantSelectionMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SHOW_ALL_PLANTS("^show all plants$"),
    SHOW_AVAILABLE_PLANTS("^show available plants$"),
    ADD_PLANT("^add plant\\s+-t\\s+(?<type>.+)$"),
    REMOVE_PLANT("^remove plant\\s+-t\\s+(?<type>.+)$"),
    BOOST_PLANT("^boost plant\\s+-t\\s+(?<type>.+)$"),
    START_GAME("^start game$");

    private final Pattern compiledPattern;

    PlantSelectionMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}