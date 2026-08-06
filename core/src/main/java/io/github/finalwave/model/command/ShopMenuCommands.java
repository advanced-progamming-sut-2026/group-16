package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ShopMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    SHOP_LIST("^shop list$"),
    SHOP_DAILY("^shop daily$"),
    SHOP_BUY("^shop buy\\s+-i\\s+(?<itemId>\\S+)\\s+-n\\s+(?<count>\\S+)(?:\\s+-t\\s+(?<plantType>.+))?$");

    private final Pattern compiledPattern;

    ShopMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}