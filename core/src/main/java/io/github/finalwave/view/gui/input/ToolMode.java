package io.github.finalwave.view.gui.input;


public sealed interface ToolMode {
    record None() implements ToolMode {
    }

    record Seed(String plantName) implements ToolMode {
    }

    record Shovel() implements ToolMode {
    }

    record PlantFood() implements ToolMode {
    }

    record Zombie(String alias) implements ToolMode {
    }

    record VaseTap() implements ToolMode {
    }
}
