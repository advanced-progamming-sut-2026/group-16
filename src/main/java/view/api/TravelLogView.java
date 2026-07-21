package view.api;

import java.util.List;

public interface TravelLogView extends View {
    void showCurrentMenu();

    void showTravelLogPage(String pageName, List<String> questLines);

    void errorInvalidCommand();

    void errorPageNameRequired();

    void errorUnknownPage(String pageName);
}
