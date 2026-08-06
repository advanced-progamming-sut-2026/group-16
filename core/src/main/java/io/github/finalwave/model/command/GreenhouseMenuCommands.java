package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GreenhouseMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SHOW_GREENHOUSE("^show greenhouse$"),
    PLANT_POT_AT("^plant pot at\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    COLLECT("^collect\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    GROW("^grow\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    ENTER_SHOP("^enter shop$");

    private final Pattern compiledPattern;

    GreenhouseMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}