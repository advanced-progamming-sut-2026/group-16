package controller;

import model.command.NewsMenuCommands;
import model.user.NewsItem;
import model.user.NewsManager;
import model.user.User;
import model.user.UserDatabase;
import view.api.NewsView;

import java.util.List;
import java.util.regex.Matcher;

public class NewsController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final MainMenuController mainMenuController;
    private final NewsManager newsManager = new NewsManager();

    public NewsController(User user, UserDatabase userDatabase, MainMenuController mainMenuController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.mainMenuController = mainMenuController;
    }

    @Override
    public void displayMenu() {
        getNewsView().showCurrentMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (NewsMenuCommands cmd : NewsMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case SHOW_UNREAD -> handleShowUnread();
                case SHOW_ALL -> handleShowAll();
            }
            return;
        }
        view.displayError("Invalid news command.");
    }

    private void handleShowCurrent() {
        getNewsView().showCurrentMenu();
    }

    private void handleMenuExit() {
        parser.switchController(mainMenuController);
    }

    private void handleShowUnread() {
        List<NewsItem> unread = newsManager.getUnread(user);
        if (unread.isEmpty()) {
            getNewsView().showNoUnreadNews();
            return;
        }
        getNewsView().showUnreadNews(newsManager.formatAll(unread));
        newsManager.markAsRead(unread);
        userDatabase.saveUserNews(user);
    }

    private void handleShowAll() {
        List<NewsItem> all = newsManager.getAll(user);
        if (all.isEmpty()) {
            getNewsView().showNoNews();
            return;
        }
        getNewsView().showAllNews(newsManager.formatAll(all));
    }

    private NewsView getNewsView() {
        return (NewsView) view;
    }
}
