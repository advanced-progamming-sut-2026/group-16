package io.github.finalwave.view.gui.widget;

import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.model.quest.condition.QuestCondition;
import io.github.finalwave.model.quest.condition.QuestConditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuestProgressDisplay {
    private static final Pattern FIRST_INT = Pattern.compile("(\\d+)");
    private static final Pattern LABELED = Pattern.compile(
            "(?i)(?:collected|kills|killed|planted|streak)\\s*:\\s*(\\d+)");

    private QuestProgressDisplay() {
    }

    public record Amount(int current, int target) {
        public String label() {
            return current + "/" + target;
        }

        public float fraction() {
            if (target <= 0) {
                return 0f;
            }
            return Math.min(1f, current / (float) target);
        }
    }

    public static Amount of(Quest quest) {
        if (quest == null) {
            return new Amount(0, 1);
        }
        Amount amount = fromCondition(quest.getCondition());
        if (quest.isCompleted() || quest.getCondition().isMet()) {
            return new Amount(amount.target, amount.target);
        }
        return amount;
    }

    private static Amount fromCondition(QuestCondition condition) {
        if (condition instanceof QuestConditions.CollectSunCondition
                || condition instanceof QuestConditions.KillZombiesInChapterCondition
                || condition instanceof QuestConditions.KillOnlyWithPlantCondition
                || condition instanceof QuestConditions.SpeedKillCondition
                || condition instanceof QuestConditions.PlantExplosivesCondition
                || condition instanceof QuestConditions.WinStreakCondition
                || condition instanceof QuestConditions.KillInFirstColumnNoMowerCondition
                || condition instanceof QuestConditions.LawnMowerKillsCondition
                || condition instanceof QuestConditions.LimitedSunProducersCondition) {
            return counted(condition);
        }
        return binary(condition.isMet());
    }

    private static Amount counted(QuestCondition condition) {
        int target = firstInt(condition.describe());
        if (target <= 0) {
            return binary(condition.isMet());
        }
        int current = parseInt(condition.serializeProgress());
        if (current < 0) {
            current = labeledInt(condition.describe());
        }
        if (current < 0) {
            current = condition.isMet() ? target : 0;
        }
        if (condition.isMet()) {
            current = target;
        }
        return new Amount(Math.max(0, Math.min(current, target)), target);
    }

    private static Amount binary(boolean met) {
        return new Amount(met ? 1 : 0, 1);
    }

    private static int firstInt(String text) {
        if (text == null) {
            return -1;
        }
        Matcher matcher = FIRST_INT.matcher(text);
        if (matcher.find()) {
            return parseInt(matcher.group(1));
        }
        return -1;
    }

    private static int labeledInt(String text) {
        if (text == null) {
            return -1;
        }
        Matcher matcher = LABELED.matcher(text);
        if (matcher.find()) {
            return parseInt(matcher.group(1));
        }
        return -1;
    }

    private static int parseInt(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
