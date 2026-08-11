package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.NewsController;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;

import java.util.List;


public final class NewsScreen extends MenuScreen {
    private NewsController controller;
    private Table newsList;

    public NewsScreen(PvzGame game) {
        super(game);
    }

    public void bind(NewsController controller) {
        this.controller = controller;
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();

        Skin skin = assets.skin();
        if (controller != null) {
            bindCurrency(controller.getUser());
        }

        BorderedTable panel = new BorderedTable();
        panel.pad(40);
        panel.add(PanelLabels.title(skin, "News")).padBottom(20).row();

        Table actions = new Table();
        TextButton unreadBtn = PvzButtons.textButton("Unread", skin, "purple", () -> {
            if (controller != null) {
                controller.showUnread();
            }
        });
        TextButton allBtn = PvzButtons.textButton("All news", skin, "brown", () -> {
            if (controller != null) {
                controller.showAll();
            }
        });
        TextButton backBtn = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        });
        actions.add(unreadBtn).width(200).height(56).padRight(12);
        actions.add(allBtn).width(200).height(56).padRight(12);
        actions.add(backBtn).width(160).height(50);
        panel.add(actions).padBottom(16).row();

        newsList = new Table();
        newsList.top().left();
        ScrollPane scroll = new ScrollPane(newsList, skin);
        scroll.setFadeScrollBars(false);
        panel.add(scroll).width(900).height(520).grow();

        contentLayer.add(panel);
        showPlaceholder("Choose Unread or All news.");
    }

    public void showNewsLines(List<String> lines) {
        if (newsList == null) {
            return;
        }
        newsList.clearChildren();
        Skin skin = assets.skin();
        if (lines == null || lines.isEmpty()) {
            showPlaceholder("No news.");
            return;
        }
        for (String line : lines) {
            Label label = PanelLabels.body(skin, line);
            label.setAlignment(Align.left);
            newsList.add(label).width(860).left().padBottom(12).row();
        }
    }

    public void showPlaceholder(String message) {
        if (newsList == null) {
            return;
        }
        newsList.clearChildren();
        Label label = PanelLabels.body(assets.skin(), message);
        label.setAlignment(Align.center);
        newsList.add(label).width(860).padTop(40);
    }
}
