package io.github.finalwave.model.quest;

import io.github.finalwave.model.quest.condition.QuestCondition;
import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.quest.reward.QuestReward;

public final class Quest {

    public enum Category {
        DAILY,
        MAIN,
        EPIC_CHALLENGE
    }

    public enum Priority {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    private final String id;
    private final String title;
    private final String description;
    private final Category category;
    private final Priority priority;
    private final QuestCondition condition;
    private final QuestReward reward;

    private boolean completed;
    private boolean rewardClaimed;

    public Quest(String id, String title, Category category,
                 Priority priority, QuestCondition condition, QuestReward reward) {
        this(id, title, condition.describe(), category, priority, condition, reward);
    }

    public Quest(String id, String title, String description, Category category,
                 Priority priority, QuestCondition condition, QuestReward reward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.condition = condition;
        this.reward = reward;
    }

    public void startSession() {
        if (completed) {
            return;
        }
        if (!condition.persistsAcrossSessions()) {
            condition.reset();
        }
    }

    public void resetForNewDay() {
        if (category != Category.DAILY) {
            return;
        }
        completed = false;
        rewardClaimed = false;
        condition.reset();
    }

    public boolean onEvent(GameEvent event) {
        if (completed) {
            return false;
        }
        condition.onEvent(event);
        if (condition.isMet()) {
            completed = true;
            return true;
        }
        return false;
    }

    public QuestCondition getCondition() {
        return condition;
    }

    public void restoreState(boolean completed, boolean rewardClaimed, String progressBlob) {
        this.completed = completed;
        this.rewardClaimed = rewardClaimed;
        if (progressBlob != null && !progressBlob.isBlank()) {
            condition.deserializeProgress(progressBlob);
        }
    }

    public String exportProgressBlob() {
        return condition.serializeProgress();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public QuestReward getReward() {
        return reward;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void markRewardClaimed() {
        rewardClaimed = true;
    }

    public String getProgressDescription() {
        return condition.describe();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s) | %s | Reward: %s", completed ? "✓" : " ",
                title, category, priority, condition.describe(), reward.describe());
    }
}
