package view.api;

import java.util.List;

public interface TravelLogView extends View {
    void showCurrentMenu();

    void showTravelLogPage(String pageName, List<String> questLines);

    void showProgressSummary(List<String> lines);

    void errorInvalidCommand();

    void errorPageNameRequired();

    void errorUnknownPage(String pageName);
}
