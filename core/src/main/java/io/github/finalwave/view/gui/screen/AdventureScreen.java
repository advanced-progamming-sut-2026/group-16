package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.AdventureController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.view.gui.assets.AdventureAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.LevelPathMap;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.HashSet;
import java.util.Set;

public final class AdventureScreen extends MenuScreen {
    private static final float BACK_SIZE = 96f;
    private static final float HUD_ICON = 88f;

    private AdventureController controller;
    private ChapterId seenChapter;
    private final Set<Integer> seenCompleted = new HashSet<>();
    private final Set<Integer> seenUnlocked = new HashSet<>();

    public AdventureScreen(PvzGame game) {
        super(game);
    }

    public void bind(AdventureController controller) {
        this.controller = controller;
        retainSeenClips();
        if (controller != null && controller.getUser() != null) {
            bindCurrency(controller.getUser());
        }
    }

    @Override
    protected void buildUi() {
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        if (controller != null && controller.getChapter() != null) {
            setBackground(assets.region(AdventureAssetIds.chapterBackground(controller.getChapter().getId())));
            bindCurrency(controller.getUser());
        } else {
            useDefaultBackground();
        }
        buildHud();
        refreshPath();
    }

    public void refreshPath() {
        contentLayer.clearChildren();
        if (controller == null) {
            return;
        }
        retainSeenClips();
        LevelPathMap path = new LevelPathMap(assets, assets.skin(), controller, seenCompleted, seenUnlocked, level -> {
            if (controller != null) {
                controller.startLevel(level);
            }
        });
        contentLayer.addActor(path);
    }

    private void retainSeenClips() {
        if (controller == null || controller.getChapter() == null) {
            return;
        }
        ChapterId chapterId = controller.getChapter().getId();
        if (chapterId == seenChapter) {
            return;
        }
        seenChapter = chapterId;
        seenCompleted.clear();
        seenUnlocked.clear();
        ChapterProgress progress = controller.getUser() == null ? null : controller.getUser().getChapterProgress();
        if (progress != null) {
            seenCompleted.addAll(progress.getCompletedLevels(chapterId));
        }
        for (LevelConfig level : controller.getChapter().getLevels()) {
            if (controller.isLevelUnlocked(level.getIndex())) {
                seenUnlocked.add(level.getIndex());
            }
        }
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
        topBar.add(hudShortcut(MenuAssetIds.GREENHOUSE_ICON, () -> {
            if (controller != null) {
                controller.openGreenhouse();
            }
        })).size(HUD_ICON).padLeft(8f).padTop(14f);
        topBar.add(hudShortcut(MenuAssetIds.HUD_SETTINGS_ICON, () -> {
            if (controller != null) {
                controller.openSettings();
            }
        })).size(HUD_ICON).padLeft(8f).padTop(14f);
        topBar.add(hudShortcut(MenuAssetIds.ALMANAC_ICON, () -> {
            if (controller != null) {
                controller.openCollection();
            }
        })).size(HUD_ICON).padLeft(8f).padTop(14f);
        topBar.add().expandX();
        topBar.add(currencyBar).padTop(14f).padRight(24f);
        hudLayer.add(topBar).growX();
    }

    private Actor hudShortcut(String iconId, Runnable onClick) {
        return PvzButtons.iconButton(assets.region(iconId), HUD_ICON, HUD_ICON, onClick);
    }
}
