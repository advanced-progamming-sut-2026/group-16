package io.github.finalwave.controller;

import io.github.finalwave.model.command.NewsMenuCommands;
import io.github.finalwave.model.user.NewsItem;
import io.github.finalwave.model.user.NewsManager;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.NewsView;

import java.util.List;
import java.util.regex.Matcher;

public class NewsController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final NewsManager newsManager = new NewsManager();

    public NewsController(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;
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
        navigator.pop();
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
