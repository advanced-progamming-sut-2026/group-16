package controller;

import model.command.TravelLogMenuCommands;
import model.quest.Quest;
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
        for (Quest quest : quests) {
            lines.add(formatQuestLine(quest));
        }
        getTravelLogView().showTravelLogPage(title, lines);
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
