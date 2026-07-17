package view.cli;

import view.api.NewsView;

import java.util.List;

public class NewsViewCli extends CliView implements NewsView {
    @Override
    public void showUnreadNews(List<String> news) {
        for (String n : news) {
            displayMessage(n);
        }
    }

    @Override
    public void showAllNews(List<String> news) {
        for (String n : news) {
            displayMessage(n);
        }
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid news command.");
    }
}
