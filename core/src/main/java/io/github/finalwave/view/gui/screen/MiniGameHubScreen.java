package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.MiniGameHubController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.view.gui.assets.AdventureAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.VaseBreakerStagePath;
import pvz.skin.BorderedTable;

import java.util.HashSet;
import java.util.Set;


public final class MiniGameHubScreen extends MenuScreen {
    private static final float BACK_SIZE = 96f;

    private MiniGameHubController controller;
    private Label titleLabel;
    private Table stageList;
    private MiniGameId trackedGame;
    private final Set<Integer> seenCompleted = new HashSet<>();
    private final Set<Integer> seenUnlocked = new HashSet<>();

    public MiniGameHubScreen(PvzGame game) {
        super(game);
    }

    public void bind(MiniGameHubController controller) {
        this.controller = controller;
    }

    @Override
    protected void buildUi() {
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        if (usesStagePath()) {
            setBackground(assets.region(AdventureAssetIds.MAP_BACKGROUND));
            buildPathHud();
            refreshPath();
            return;
        }
        useDefaultBackground();
        buildPanel();
        refresh();
    }

    public void refresh() {
        if (usesStagePath()) {
            refreshPath();
            return;
        }
        if (titleLabel != null) {
            titleLabel.setText(heading());
        }
        rebuildStages();
    }

    private void refreshPath() {
        retainSeen();
        contentLayer.clearChildren();
        if (controller == null) {
            return;
        }
        VaseBreakerStagePath path = new VaseBreakerStagePath(
                assets,
                assets.skin(),
                pathChapter(),
                controller.selectedStages(),
                seenCompleted,
                seenUnlocked,
                index -> {
                    if (controller != null) {
                        controller.startStage(index);
                    }
                });
        contentLayer.addActor(path);
    }

    private void retainSeen() {
        MiniGameId id = controller == null ? null : controller.selectedGame();
        if (id == trackedGame) {
            return;
        }
        trackedGame = id;
        seenCompleted.clear();
        seenUnlocked.clear();
        if (controller == null) {
            return;
        }
        for (MiniGameHubController.StageInfo stage : controller.selectedStages()) {
            if (stage.completed()) {
                seenCompleted.add(stage.index());
            }
            if (stage.playable()) {
                seenUnlocked.add(stage.index());
            }
        }
    }

    private void buildPathHud() {
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

    private void buildPanel() {
        hudLayer.clearChildren();
        Skin skin = assets.skin();
        BorderedTable panel = new BorderedTable();
        panel.pad(40, 48, 40, 48);

        titleLabel = PanelLabels.title(skin, heading());
        titleLabel.setFontScale(1.35f);
        panel.add(titleLabel).left().padBottom(18).row();

        stageList = new Table();
        panel.add(stageList).growX().padBottom(24).row();

        TextButton back = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        });
        panel.add(back).width(180).height(56).left();
        contentLayer.add(panel);
    }

    private void rebuildStages() {
        if (stageList == null || controller == null) {
            return;
        }
        stageList.clearChildren();
        Skin skin = assets.skin();
        for (MiniGameHubController.StageInfo stage : controller.selectedStages()) {
            String label = "Stage " + stage.index() + "  " + stage.detail() + "  " + status(stage);
            TextButton button = PvzButtons.textButton(label, skin, styleFor(stage), () -> {
                if (controller != null && stage.playable() && stage.implemented()) {
                    controller.startStage(stage.index());
                }
            });
            button.setDisabled(stage.locked() || !stage.implemented());
            stageList.add(button).growX().height(64f).padBottom(10f).row();
        }
    }

    private boolean usesStagePath() {
        if (controller == null) {
            return false;
        }
        MiniGameId id = controller.selectedGame();
        return id == MiniGameId.VASE_BREAKER
                || id == MiniGameId.WALNUT_BOWLING
                || id == MiniGameId.I_ZOMBIE
                || id == MiniGameId.BEGHOULED;
    }

    private ChapterId pathChapter() {
        if (controller != null && controller.selectedGame() == MiniGameId.WALNUT_BOWLING) {
            return ChapterId.BIG_WAVE_BEACH;
        }
        return ChapterId.ANCIENT_EGYPT;
    }

    private String heading() {
        if (controller == null || controller.selectedGame() == null) {
            return "Mini-Games";
        }
        return controller.selectedGame().getDisplayName();
    }

    private static String status(MiniGameHubController.StageInfo stage) {
        if (!stage.implemented()) {
            return "Coming soon";
        }
        if (stage.completed()) {
            return "Done";
        }
        if (stage.playable()) {
            return "Open";
        }
        return "Locked";
    }

    private static String styleFor(MiniGameHubController.StageInfo stage) {
        if (stage.completed()) {
            return "green_small";
        }
        if (stage.playable() && stage.implemented()) {
            return "purple";
        }
        return "brown";
    }
}
