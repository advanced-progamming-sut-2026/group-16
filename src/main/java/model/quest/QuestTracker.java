package model.quest;

import model.quest.event.GameEvent;
import model.quest.event.GameEventBus;
import model.quest.reward.QuestReward;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class QuestTracker {

    private final List<Quest> quests = new ArrayList<>();

    private final Consumer<Quest> onQuestCompleted;

    public QuestTracker(Consumer<Quest> onQuestCompleted) {
        this.onQuestCompleted = onQuestCompleted;
    }

    public void addQuest(Quest quest) {
        quests.add(quest);
    }

    public void setQuests(List<Quest> quests) {
        this.quests.clear();
        this.quests.addAll(quests);
    }

    public void registerOn(GameEventBus bus) {
        bus.subscribe(this::handleEvent);
    }

    public void beginSession() {
        quests.forEach(Quest::startSession);
    }

    public void endSession() {
        // Future: persist updated quest state to UserDatabase
    }

    private void handleEvent(GameEvent event) {
        for (Quest quest : quests) {
            boolean justCompleted = quest.onEvent(event);
            if (justCompleted) {
                onQuestCompleted.accept(quest);
            }
        }
    }

    public List<Quest> getSortedQuests() {
        return quests.stream()
                .sorted(Comparator
                        .comparingInt((Quest q) -> q.getPriority().ordinal())
                        .thenComparing(q -> q.isCompleted() ? 1 : 0))
                .toList();
    }

    public List<Quest> getDailyQuests() {
        return getSortedQuests().stream()
                .filter(q -> q.getCategory() == Quest.Category.DAILY)
                .toList();
    }

    public List<Quest> getMainQuests() {
        return getSortedQuests().stream()
                .filter(q -> q.getCategory() == Quest.Category.MAIN)
                .toList();
    }

    public List<Quest> getEpicQuests() {
        return getSortedQuests().stream()
                .filter(q -> q.getCategory() == Quest.Category.EPIC_CHALLENGE)
                .toList();
    }

    public long completedCount() {
        return quests.stream().filter(Quest::isCompleted).count();
    }

    public int totalCount() {
        return quests.size();
    }
}
