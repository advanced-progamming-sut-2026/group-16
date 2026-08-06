package io.github.finalwave.model.quest;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LawnMower;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.quest.condition.QuestConditions;
import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.quest.event.GameEventBus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class QuestTracker {

    private final List<Quest> quests = new ArrayList<>();
    private final Consumer<Quest> onQuestCompleted;
    private GameEventBus.Subscriber busSubscriber;
    private GameEventBus registeredBus;

    public QuestTracker(Consumer<Quest> onQuestCompleted) {
        this.onQuestCompleted = onQuestCompleted == null ? q -> {
        } : onQuestCompleted;
    }

    public void addQuest(Quest quest) {
        quests.add(quest);
    }

    public void setQuests(List<Quest> quests) {
        this.quests.clear();
        this.quests.addAll(quests);
    }

    public List<Quest> getQuests() {
        return List.copyOf(quests);
    }

    public void registerOn(GameEventBus bus) {
        if (bus == null) {
            return;
        }
        unregister();
        busSubscriber = this::handleEvent;
        registeredBus = bus;
        bus.subscribe(busSubscriber);
    }

    public void unregister() {
        if (registeredBus != null && busSubscriber != null) {
            registeredBus.unsubscribe(busSubscriber);
        }
        registeredBus = null;
        busSubscriber = null;
    }

    public void beginSession() {
        beginSession(null);
    }

    public void beginSession(GameSession session) {
        quests.forEach(Quest::startSession);
        if (session != null) {
            prepareNearVictoryMowers(session);
        }
    }

    public void endSession() {
    }

    public void resetDailyQuests() {
        for (Quest quest : quests) {
            quest.resetForNewDay();
        }
    }

    public void prepareNearVictoryMowers(GameSession session) {
        if (session == null) {
            return;
        }
        Set<Integer> rowsWithMowers = new HashSet<>();
        List<LawnMower> mowers = session.getLawnMowers();
        for (int row = 0; row < mowers.size(); row++) {
            if (!mowers.get(row).isUsed()) {
                rowsWithMowers.add(row);
            }
        }
        for (Quest quest : quests) {
            if (quest.getCondition() instanceof QuestConditions.KillInFirstColumnNoMowerCondition near) {
                near.setRowsWithMowers(rowsWithMowers);
            }
        }
    }

    public void prepareBoardSnapshots(GameSession session) {
        if (session == null) {
            return;
        }
        String[][] snapshot = snapshotPlantTypes(session.getBoard());
        for (Quest quest : quests) {
            if (quest.getCondition() instanceof QuestConditions.SymmetricBoardCondition s) {
                s.setBoardSnapshot(snapshot);
            } else if (quest.getCondition() instanceof QuestConditions.AsymmetricBoardCondition a) {
                a.setBoardSnapshot(snapshot);
            }
        }
    }

    private static String[][] snapshotPlantTypes(GameBoard board) {
        String[][] grid = new String[board.getRows()][board.getCols()];
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Plant plant = board.getPlantAt(col, row);
                grid[row][col] = plant == null ? null : plant.getName();
            }
        }
        return grid;
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

    public long completedCount(Quest.Category category) {
        if (category == null) {
            return 0;
        }
        return quests.stream()
                .filter(q -> q.getCategory() == category && q.isCompleted())
                .count();
    }

    public long completedDailyCount() {
        return completedCount(Quest.Category.DAILY);
    }

    public long completedNonDailyCount() {
        return completedCount(Quest.Category.MAIN)
                + completedCount(Quest.Category.EPIC_CHALLENGE);
    }

    public int totalCount() {
        return quests.size();
    }
}
