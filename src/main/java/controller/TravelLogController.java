package controller;

import model.command.TravelLogMenuCommands;
import model.minigame.MiniGameId;
import model.minigame.MiniGameRegistry;
import model.quest.Quest;
import model.quest.QuestService;
import model.quest.QuestTracker;
import model.user.User;
import model.user.UserDatabase;
import view.api.TravelLogView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class TravelLogController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final GameController gameController;

    public TravelLogController(User user, UserDatabase userDatabase, GameController gameController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.gameController = gameController;
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
        parser.switchController(gameController);
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
                parser.switchController(new MiniGameHubController(user, userDatabase, this));
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

    private TravelLogView getTravelLogView() {
        return (TravelLogView) view;
    }
}
