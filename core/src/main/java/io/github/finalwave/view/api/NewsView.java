package io.github.finalwave.view.api;

import java.util.List;

public interface NewsView extends View {
    void showCurrentMenu();

    void showUnreadNews(List<String> news);

    void showAllNews(List<String> news);

    void showNoUnreadNews();

    void showNoNews();
}
