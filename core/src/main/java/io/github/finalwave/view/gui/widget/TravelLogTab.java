package io.github.finalwave.view.gui.widget;

import io.github.finalwave.model.quest.Quest;

public enum TravelLogTab {
    DAILY("Daily", true),
    MAIN("Main", true),
    EPIC("Epic", true),
    MINIGAME("Minigame", false);

    private final String label;
    private final boolean questTab;

    TravelLogTab(String label, boolean questTab) {
        this.label = label;
        this.questTab = questTab;
    }

    public String label() {
        return label;
    }

    public boolean questTab() {
        return questTab;
    }

    public boolean green() {
        return this != MINIGAME;
    }

    public Quest.Category questCategory() {
        return switch (this) {
            case DAILY -> Quest.Category.DAILY;
            case MAIN -> Quest.Category.MAIN;
            case EPIC -> Quest.Category.EPIC_CHALLENGE;
            case MINIGAME -> null;
        };
    }
}
