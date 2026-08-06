package io.github.finalwave.model.command;

import java.util.regex.Matcher;

public interface Command {
    public Matcher getMatcher(String input);
}
