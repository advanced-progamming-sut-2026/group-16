package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ProfileMenuCommands implements Command {
    MENU_SHOW_CURRENT("^menu show current$"),
    MENU_EXIT("^menu exit$"),
    CHANGE_USERNAME("^menu profile change-username\\s+-u\\s+(?<username>\\S+)$"),
    CHANGE_NICKNAME("^menu profile change-nickname\\s+-u\\s+(?<nickname>\\S+)$"),
    CHANGE_EMAIL("^menu profile change-email\\s+-e\\s+(?<email>\\S+)$"),
    CHANGE_PASSWORD("^menu profile change-password\\s+-p\\s+(?<newPassword>\\S+)\\s+-o\\s+(?<oldPassword>\\S+)$"),
    SHOW_INFO("^menu profile show-info$");

    private final Pattern compiledPattern;

    ProfileMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}