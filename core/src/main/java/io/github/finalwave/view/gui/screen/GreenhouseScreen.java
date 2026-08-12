package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.GreenhouseController;
import io.github.finalwave.model.greenhouse.GreenhouseSlotState;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.widget.GreenhouseGrid;
import io.github.finalwave.view.gui.widget.GreenhousePotSlot;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.List;


public final class GreenhouseScreen extends MenuScreen {
    private static final float BACK_SIZE = 96f;
    private static final float STORE_SIZE = 92f;

    private GreenhouseController controller;
    private PlantAnimationCatalog catalog;
    private Group potHost;

    public GreenhouseScreen(PvzGame game) {
        super(game);
    }

    public void bind(GreenhouseController controller) {
        this.controller = controller;
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
    }

    @Override
    protected void buildUi() {
        setBackground(assets.region(MenuAssetIds.ZEN_GARDEN_BACKGROUND));
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        if (catalog == null) {
            catalog = new PlantAnimationCatalog(assets.root());
        }
        if (controller != null) {
            bindCurrency(controller.getUser());
            currencyBar.enableSprouts(controller::plantableCount, this::onSproutPlus);
        }

        Actor back = PvzButtons.iconButton(
                assets.region(MenuAssetIds.HUD_BACK),
                BACK_SIZE,
                BACK_SIZE,
                () -> {
                    if (controller != null) {
                        controller.back();
                    }
                });

        potHost = new Group();
        potHost.setSize(WORLD_WIDTH, WORLD_HEIGHT);
        contentLayer.addActor(potHost);

        hudLayer.clearChildren();
        hudLayer.top().left();
        hudLayer.padTop(0f).padRight(0f).padLeft(0f);
        Table topBar = new Table();
        topBar.add(back).size(BACK_SIZE).padLeft(16f).padTop(12f);
        topBar.add().expandX();
        topBar.add(currencyBar).padTop(14f);
        Actor store = PvzButtons.iconButton(
                assets.region(MenuAssetIds.STORE_ICON),
                STORE_SIZE,
                STORE_SIZE,
                this::openShop);
        topBar.add(store).size(STORE_SIZE).padLeft(8f).padRight(16f).padTop(10f);
        hudLayer.add(topBar).growX();

        refreshPots();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        refreshPots();
    }

    public void refreshPots() {
        if (potHost == null || controller == null) {
            return;
        }
        potHost.clearChildren();
        List<GreenhouseSlotState> slots = controller.slotStates();
        for (GreenhouseSlotState slot : slots) {
            GreenhousePotSlot actor = new GreenhousePotSlot(assets, catalog, assets.skin());
            actor.bind(slot, actionsFor(slot.x(), slot.y()));
            actor.setPosition(
                    GreenhouseGrid.slotX(slot.x(), viewport.getWorldWidth(), viewport.getWorldHeight()),
                    GreenhouseGrid.slotY(slot.y(), viewport.getWorldWidth(), viewport.getWorldHeight()));
            potHost.addActor(actor);
        }
        if (showsCurrencyBar()) {
            bindCurrency(controller.getUser());
        }
    }

    public void showCollectReward(String reward) {
        modalLayer.clearChildren();
        Skin skin = assets.skin();
        ModalPanel panel = new ModalPanel(skin, "Harvest");
        panel.content().add(PanelLabels.body(skin, "You received: " + reward)).width(520f).padBottom(16f).row();
        TextButton ok = PvzButtons.textButton("OK", skin, "green_small", panel::dismiss);
        panel.content().add(ok).width(160f).height(48f);
        panel.show(modalLayer, viewport);
    }

    private GreenhousePotSlot.Actions actionsFor(int x, int y) {
        return new GreenhousePotSlot.Actions() {
            @Override
            public void plant() {
                if (controller != null) {
                    controller.plantPot(x, y);
                }
            }

            @Override
            public void collect() {
                if (controller != null) {
                    controller.collectPot(x, y);
                }
            }

            @Override
            public void grow() {
                if (controller != null) {
                    controller.growPot(x, y);
                }
            }

            @Override
            public void unlock() {
                if (controller != null) {
                    controller.unlockPot(x, y);
                }
            }
        };
    }

    private void onSproutPlus() {
        if (controller == null) {
            return;
        }
        if (!controller.cheatUnlockNextPot()) {
            toastMessage("All pots are already unlocked");
        }
    }

    private void openShop() {
        toastMessage("Coming soon");
    }
}
