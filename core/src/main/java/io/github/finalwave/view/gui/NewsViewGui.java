package io.github.finalwave.view.gui;

import io.github.finalwave.controller.NewsController;
import io.github.finalwave.view.api.NewsView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class NewsViewGui extends GuiViewBase implements NewsView {
    public NewsViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(NewsController controller) {

    }

    @Override
    public void showCurrentMenu() {
        router.showNewsPlaceholder("Choose Unread or All news.");
    }

    @Override
    public void showUnreadNews(List<String> news) {
        router.showNewsLines(news);
    }

    @Override
    public void showAllNews(List<String> news) {
        router.showNewsLines(news);
    }

    @Override
    public void showNoUnreadNews() {
        router.showNewsPlaceholder("No unread news.");
    }

    @Override
    public void showNoNews() {
        router.showNewsPlaceholder("No news.");
    }
}
