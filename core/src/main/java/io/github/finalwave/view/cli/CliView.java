package io.github.finalwave.view.cli;

import io.github.finalwave.util.AnsiColors;
import io.github.finalwave.view.api.View;

public class CliView implements View {
    @Override
    public void displayMessage(String line) {
        System.out.println(line);
    }

    @Override
    public void displayError(String line) {
        System.out.println(AnsiColors.color(AnsiColors.RED, line));
    }
}
