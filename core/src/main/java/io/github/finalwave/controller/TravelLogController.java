package io.github.finalwave.controller;

import io.github.finalwave.model.command.TravelLogMenuCommands;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.minigame.MiniGameRegistry;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.model.quest.QuestService;
import io.github.finalwave.model.quest.QuestTracker;
import io.github.finalwave.model.user.MiniGameProgress;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.TravelLogView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class TravelLogController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;

    public TravelLogController(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;
    }

    public User getUser() {
        return user;
    }

    public void back() {
        handleMenuExit();
    }

    public List<Quest> questsFor(Quest.Category category) {
        QuestTracker tracker = refreshedTracker();
        if (category == Quest.Category.DAILY) {
            return tracker.getDailyQuests();
        }
        if (category == Quest.Category.MAIN) {
            return tracker.getMainQuests();
        }
        return tracker.getEpicQuests();
    }

    public List<MiniGameLogEntry> minigameEntries() {
        MiniGameRegistry registry = MiniGameRegistry.getInstance();
        MiniGameProgress progress = user.getMiniGameProgress();
        List<MiniGameLogEntry> entries = new ArrayList<>();
        for (MiniGameId id : registry.getAllMiniGames()) {
            List<MiniGameStageConfig> stages = registry.getStages(id);
            int total = stages.size();
            int completed = 0;
            boolean implemented = false;
            for (MiniGameStageConfig stage : stages) {
                if (progress.isStageCompleted(id, stage.getStageIndex())) {
                    completed++;
                }
                if (stage.isImplemented()) {
                    implemented = true;
                }
            }
            boolean unlocked = minigameUnlocked(id);
            entries.add(new MiniGameLogEntry(
                    id,
                    id.getDisplayName(),
                    flavorFor(id),
                    unlocked,
                    implemented,
                    completed,
                    total));
        }
        return entries;
    }

    public String dailyRefreshLabel() {
        LocalDateTime now = LocalDateTime.now();
        Duration remaining = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay());
        if (remaining.isNegative()) {
            remaining = Duration.ZERO;
        }
        long hours = remaining.toHours();
        long minutes = remaining.minusHours(hours).toMinutes();
        return String.format("Daily Activities refresh in %02dh:%02dmin", hours, minutes);
    }

    public int pendingClaimCount(Quest.Category category) {
        return (int) questsFor(category).stream()
                .filter(Quest::isCompleted)
                .filter(quest -> !quest.isRewardClaimed())
                .count();
    }

    public void claimQuest(Quest quest) {
        if (quest == null || !quest.isCompleted()) {
            getTravelLogView().displayError("This quest is not complete yet.");
            return;
        }
        if (quest.isRewardClaimed()) {
            getTravelLogView().displayMessage("This quest reward was already claimed.");
            return;
        }
        if (QuestService.claimReward(user, quest)) {
            getTravelLogView().displayMessage("Quest reward claimed.");
            getTravelLogView().showCurrentMenu();
        }
    }

    public void claimAll(Quest.Category category) {
        List<Quest> quests = questsFor(category);
        int claimed = QuestService.claimAll(user, quests);
        if (claimed == 0) {
            getTravelLogView().displayMessage("No completed quest rewards to claim.");
            return;
        }
        getTravelLogView().displayMessage("Claimed " + claimed + " quest reward(s).");
        getTravelLogView().showCurrentMenu();
    }

    public void playQuest(Quest quest) {
        if (quest == null) {
            return;
        }
        getTravelLogView().displayMessage("Play adventure levels to progress this quest.");
    }

    public void playMinigame(MiniGameId id) {
        if (id == null) {
            getTravelLogView().errorUnknownPage("minigames");
            return;
        }
        if (!minigameUnlocked(id)) {
            getTravelLogView().displayError(id.getDisplayName() + " is locked.");
            return;
        }
        if (id != MiniGameId.VASE_BREAKER
                && id != MiniGameId.WALNUT_BOWLING
                && id != MiniGameId.I_ZOMBIE
                && id != MiniGameId.BEGHOULED) {
            getTravelLogView().displayMessage(id.getDisplayName() + " is coming soon.");
            return;
        }
        navigator.push(new MiniGameHubController(user, userDatabase, id));
    }

    @Override
    public void displayMenu() {
        getTravelLogView().showCurrentMenu();
        handleTravelLogPage("daily");
    }

    @Override
    public void handleCommand(String input) {
        for (TravelLogMenuCommands cmd : TravelLogMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case TRAVEL_LOG_CLAIM_ALL -> handleClaimAll(matcher.group("pageName"));
                case TRAVEL_LOG_CLAIM -> handleClaim(matcher.group("questId"));
                case TRAVEL_LOG_PAGE -> handleTravelLogPage(matcher.group("pageName"));
            }
            return;
        }
        getTravelLogView().errorInvalidCommand();
    }

    private void handleShowCurrent() {
        getTravelLogView().showCurrentMenu();
    }

    private void handleMenuExit() {
        navigator.pop();
    }

    private void handleClaim(String questId) {
        Quest quest = refreshedTracker().getQuests().stream()
                .filter(candidate -> candidate.getId().equalsIgnoreCase(questId))
                .findFirst()
                .orElse(null);
        if (quest == null) {
            getTravelLogView().displayError("Unknown quest: " + questId);
            return;
        }
        claimQuest(quest);
    }

    private void handleClaimAll(String pageName) {
        Quest.Category category = categoryFor(pageName);
        if (category == null) {
            getTravelLogView().errorUnknownPage(pageName);
            return;
        }
        claimAll(category);
    }

    private void handleTravelLogPage(String pageName) {
        if (pageName == null || pageName.isBlank()) {
            getTravelLogView().errorPageNameRequired();
            return;
        }
        String page = pageName.trim().toLowerCase();
        QuestTracker tracker = user.ensureQuestTracker();
        QuestService.refreshDailyQuestsIfNeeded(user, tracker);
        List<Quest> quests;
        String title;
        switch (page) {
            case "daily" -> {
                quests = tracker.getDailyQuests();
                title = "Daily";
            }
            case "main" -> {
                quests = tracker.getMainQuests();
                title = "Main";
            }
            case "epic", "epic challenge", "epic-challenge" -> {
                quests = tracker.getEpicQuests();
                title = "Epic";
            }
            case "progress", "summary" -> {
                getTravelLogView().showProgressSummary(buildProgressSummary(tracker));
                return;
            }
            case "minigames", "mini-games", "mini games" -> {
                navigator.push(new MiniGameHubController(user, userDatabase));
                return;
            }
            default -> {
                getTravelLogView().errorUnknownPage(pageName);
                return;
            }
        }
        List<String> lines = new ArrayList<>();
        long completed = quests.stream().filter(Quest::isCompleted).count();
        lines.add("Progress: " + completed + "/" + quests.size() + " completed");
        for (Quest quest : quests) {
            lines.add(formatQuestLine(quest));
        }
        getTravelLogView().showTravelLogPage(title, lines);
    }

    private List<String> buildProgressSummary(QuestTracker tracker) {
        List<Quest> daily = tracker.getDailyQuests();
        List<Quest> main = tracker.getMainQuests();
        List<Quest> epic = tracker.getEpicQuests();
        long dailyDone = daily.stream().filter(Quest::isCompleted).count();
        long mainDone = main.stream().filter(Quest::isCompleted).count();
        long epicDone = epic.stream().filter(Quest::isCompleted).count();
        long overallDone = tracker.completedCount();
        int overallTotal = tracker.totalCount();

        int miniCompleted = user.getMiniGameProgress().completedStageCount();
        int miniTotal = 0;
        for (MiniGameId id : MiniGameRegistry.getInstance().getAllMiniGames()) {
            miniTotal += MiniGameRegistry.getInstance().getStages(id).size();
        }

        List<String> lines = new ArrayList<>();
        lines.add("Quests overall: " + overallDone + "/" + overallTotal + " completed");
        lines.add("Daily: " + dailyDone + "/" + daily.size() + " completed");
        lines.add("Main: " + mainDone + "/" + main.size() + " completed");
        lines.add("Epic: " + epicDone + "/" + epic.size() + " completed");
        lines.add("Adventure levels cleared: "
                + user.getChapterProgress().countCompletedLevels());
        lines.add("Minigame stages completed: " + miniCompleted + "/" + miniTotal);
        return lines;
    }

    private static String formatQuestLine(Quest quest) {
        String status = quest.isCompleted() ? "DONE" : "OPEN";
        return String.format("%s | %s | %s | %s | Reward: %s",
                quest.getId(),
                quest.getTitle(),
                quest.getProgressDescription(),
                status,
                quest.getReward().describe());
    }

    private QuestTracker refreshedTracker() {
        QuestTracker tracker = user.ensureQuestTracker();
        QuestService.refreshDailyQuestsIfNeeded(user, tracker);
        return tracker;
    }

    private static Quest.Category categoryFor(String pageName) {
        if (pageName == null) {
            return null;
        }
        return switch (pageName.trim().toLowerCase()) {
            case "daily" -> Quest.Category.DAILY;
            case "main" -> Quest.Category.MAIN;
            case "epic", "epic challenge", "epic-challenge" -> Quest.Category.EPIC_CHALLENGE;
            default -> null;
        };
    }

    private boolean minigameUnlocked(MiniGameId id) {
        return user.isDebugMode() || user.getUnlockedMinigames().contains(id.getKey());
    }

    private static String flavorFor(MiniGameId id) {
        return switch (id) {
            case VASE_BREAKER -> "Smash pots to find plants and zombies. Clear every vase to win!";
            case WALNUT_BOWLING -> "Roll walnuts down the lawn and bowl over incoming zombies!";
            case I_ZOMBIE -> "Play as the zombies and eat every plant on the board!";
            case BEGHOULED -> "Swap plants to make matches and crush the zombie threat!";
            case ZOMBOTANY -> "Plant-headed zombies are on the march. Hold the lawn!";
        };
    }

    private TravelLogView getTravelLogView() {
        return (TravelLogView) view;
    }

    public record MiniGameLogEntry(
            MiniGameId id,
            String displayName,
            String description,
            boolean unlocked,
            boolean implemented,
            int completedStages,
            int totalStages) {
        public int remainingStages() {
            return Math.max(0, totalStages - completedStages);
        }

        public int rewardPts() {
            return Math.max(1, completedStages);
        }

        public boolean locked() {
            return !unlocked;
        }
    }

}
