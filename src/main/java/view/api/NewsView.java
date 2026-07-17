package view.api;

import java.util.List;

public interface NewsView extends View {
    void showUnreadNews(List<String> news);

    void showAllNews(List<String> news);

    void errorInvalidCommand();
}
