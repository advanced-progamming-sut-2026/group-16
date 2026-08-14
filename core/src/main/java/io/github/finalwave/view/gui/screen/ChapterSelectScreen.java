package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.GameController;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.ChapterCarousel;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.List;

public final class ChapterSelectScreen extends MenuScreen {
    private static final float BACK_SIZE = 96f;

    private GameController controller;
    private int focusIndex;

    public ChapterSelectScreen(PvzGame game) {
        super(game);
    }

    public void bind(GameController controller) {
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
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
        buildHud();
        refreshCarousel();
    }

    public void refreshCarousel() {
        contentLayer.clearChildren();
        if (controller == null) {
            return;
        }
        List<ChapterConfig> chapters = controller.chapters();
        if (chapters.isEmpty()) {
            return;
        }
        focusIndex = Math.max(0, Math.min(focusIndex, chapters.size() - 1));
        ChapterCarousel carousel = new ChapterCarousel(
                assets,
                assets.skin(),
                chapters,
                focusIndex,
                controller.getUser(),
                index -> {
                    focusIndex = index;
                    refreshCarousel();
                },
                this::playFocused,
                this::playFocused);
        contentLayer.add(carousel).expand().center();
    }

    private void buildHud() {
        hudLayer.clearChildren();
        hudLayer.top().left();
        hudLayer.padTop(0f).padRight(0f).padLeft(0f);
        Table topBar = new Table();
        Actor back = PvzButtons.iconButton(
                assets.region(MenuAssetIds.HUD_BACK),
                BACK_SIZE,
                BACK_SIZE,
                () -> {
                    if (controller != null) {
                        controller.back();
                    }
                });
        topBar.add(back).size(BACK_SIZE).padLeft(16f).padTop(12f);
        topBar.add().expandX();
        topBar.add(currencyBar).padTop(14f).padRight(24f);
        hudLayer.add(topBar).growX();
    }

    private void playFocused() {
        if (controller == null) {
            return;
        }
        List<ChapterConfig> chapters = controller.chapters();
        if (focusIndex < 0 || focusIndex >= chapters.size()) {
            return;
        }
        controller.enterChapter(chapters.get(focusIndex).getId());
    }
}
