package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GameMenuCommands implements Command {
    ENTER_CHAPTER("^menu enter chapter\\s+-c\\s+(?<chapterName>.+)$"),
    MENU_ENTER("^menu enter\\s+(?<menuName>.+)$"),
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    GREENHOUSE("^menu greenhouse$"),
    TRAVEL_LOG("^menu travel-log$"),
    LEADERBOARD("^menu leaderboard$"),
    COIN_WALLET("^menu coin-wallet$"),
    GEM_WALLET("^menu gem-wallet$"),
    CHEAT_ADD("^menu cheat add\\s+(?<n>\\S+)\\s+(?<type>\\S+)$");

    private final Pattern compiledPattern;

    GameMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}