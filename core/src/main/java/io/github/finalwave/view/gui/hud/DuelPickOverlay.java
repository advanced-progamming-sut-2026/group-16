package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.controller.NetworkedIZombieController;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.network.match.MatchRole;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PlantCardActor;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;

import java.util.ArrayList;
import java.util.List;

public final class DuelPickOverlay extends Table {

    private static final float PANEL_WIDTH = 980f;
    private static final float PANEL_HEIGHT = 780f;
    private static final float CARD_WIDTH = PlantCardActor.WIDTH;
    private static final float CARD_HEIGHT = PlantCardActor.HEIGHT;
    private static final float LOADOUT_WIDTH = 116f;
    private static final float LOADOUT_HEIGHT = 84f;
    private static final float DETAIL_HEIGHT = 120f;
    private static final float START_WIDTH = 280f;
    private static final float START_HEIGHT = 76f;
    private static final int GRID_COLUMNS = 5;
    private static final String DARK_PANEL = "image_ui_if_bundle_reward1_bg_10";
    private static final String GREEN_PLATE = "image_ui_generic_greenbutton_10";
    private static final Color HEADER_CREAM = Color.valueOf("FFFBE6");

    private final GameAssets assets;
    private final NetworkedIZombieController controller;
    private final boolean zombieRole;
    private final List<PlantCardActor> poolCards = new ArrayList<>();
    private final Texture dimTexture;
    private Label timer;
    private Label selectedLabel;
    private Label focusLabel;
    private Table grid;
    private Table loadoutColumn;
    private List<String> lastPicks = List.of();
    private String focusedName;

    public DuelPickOverlay(GameAssets assets, NetworkedIZombieController controller) {
        this.assets = assets;
        this.controller = controller;
        this.zombieRole = controller.role() == MatchRole.ZOMBIE;
        setFillParent(true);
        setTouchable(Touchable.enabled);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();

        Skin skin = assets.skin();
        Image dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTexture)));
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.55f);
        dimmer.setTouchable(Touchable.enabled);

        Table screen = new Table(skin);
        loadoutColumn = new Table(skin);
        loadoutColumn.top();
        screen.add(loadoutColumn).width(LOADOUT_WIDTH + 12f).top().left().padLeft(24f).padTop(28f);
        screen.add(chooserPanel(skin)).size(PANEL_WIDTH, PANEL_HEIGHT).expand().center().pad(12f);

        TextButton ready = PvzButtons.textButton("LET'S ROCK!", skin, "green_small", controller::submitPicks);
        Table corner = new Table();
        corner.setTouchable(Touchable.childrenOnly);
        corner.bottom().right();
        corner.add(ready).size(START_WIDTH, START_HEIGHT).padRight(32f).padBottom(32f);

        Stack layers = new Stack();
        layers.setFillParent(true);
        layers.add(dimmer);
        layers.add(screen);
        layers.add(corner);
        addActor(layers);

        List<String> pool = controller.pickPool();
        if (!pool.isEmpty()) {
            focusedName = pool.getFirst();
        }
        rebuildGrid();
        rebuildLoadout();
        refreshFocus();
        updateChrome();
    }

    @Override
    public boolean remove() {
        boolean removed = super.remove();
        if (dimTexture != null) {
            dimTexture.dispose();
        }
        return removed;
    }

    public void updateChrome() {
        if (controller == null || timer == null) {
            return;
        }
        timer.setText("Pick time left: " + controller.pickSecondsLeft() + "s");
        List<String> picks = controller.localPicks();
        selectedLabel.setText("Selected " + picks.size() + " / " + controller.pickSlots());
        if (!picks.equals(lastPicks)) {
            lastPicks = List.copyOf(picks);
            syncPoolSelection();
            rebuildLoadout();
        }
    }

    private Table chooserPanel(Skin skin) {
        Table chooser = new Table();
        chooser.top().pad(22f, 24f, 20f, 24f);
        chooser.setBackground(StoreChrome.panel());
        chooser.add(detailPanel(skin)).growX().height(DETAIL_HEIGHT).padBottom(12f).row();
        chooser.add(gridPanel(skin)).grow();
        return chooser;
    }

    private Table detailPanel(Skin skin) {
        Table panel = new Table();
        panel.setBackground(detailBackground(skin));
        panel.pad(12f, 18f, 12f, 18f);
        String role = zombieRole ? "Zombies" : "Plants";
        Table header = new Table();
        if (skin.has(GREEN_PLATE, Drawable.class)) {
            header.setBackground(skin.getDrawable(GREEN_PLATE));
        }
        focusLabel = new Label("Choose Your " + role, skin, "big");
        focusLabel.setAlignment(Align.center);
        focusLabel.setColor(HEADER_CREAM);
        header.add(focusLabel).grow().pad(2f, 18f, 6f, 18f);
        panel.add(header).growX().height(52f).padBottom(8f).row();
        timer = new Label("", skin, "medium");
        selectedLabel = PanelLabels.body(skin, "");
        panel.add(timer).left().padBottom(4f).row();
        panel.add(selectedLabel).left();
        return panel;
    }

    private Table gridPanel(Skin skin) {
        Table panel = new Table();
        panel.setBackground(gridBackground(skin));
        panel.pad(14f, 10f, 14f, 10f);
        grid = new Table(skin);
        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(false, false);
        panel.add(scroll).grow();
        return panel;
    }

    private Drawable gridBackground(Skin skin) {
        if (skin.has(DARK_PANEL, Drawable.class)) {
            return skin.getDrawable(DARK_PANEL);
        }
        return StoreChrome.panel();
    }

    private Drawable detailBackground(Skin skin) {
        if (skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
            return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        }
        return StoreChrome.panel();
    }

    private void refreshFocus() {
        if (focusLabel == null) {
            return;
        }
        if (focusedName == null || focusedName.isBlank()) {
            focusLabel.setText(zombieRole ? "Choose Your Zombies" : "Choose Your Plants");
        } else {
            focusLabel.setText(focusedName);
        }
    }

    private void rebuildGrid() {
        Skin skin = assets.skin();
        grid.clearChildren();
        poolCards.clear();
        List<String> pool = controller.pickPool();
        List<String> picks = controller.localPicks();
        int col = 0;
        for (String name : pool) {
            PlantCardActor card = new PlantCardActor(assets, skin, name);
            if (zombieRole) {
                card.setZombie(name);
                card.setCost(IZombieDuelCatalog.costOf(name));
            }
            card.setSelected(picks.contains(name));
            card.setOnClick(() -> onPoolClicked(name));
            poolCards.add(card);
            grid.add(card).size(CARD_WIDTH, CARD_HEIGHT).pad(4f);
            col++;
            if (col % GRID_COLUMNS == 0) {
                grid.row();
            }
        }
    }

    private void onPoolClicked(String name) {
        focusedName = name;
        refreshFocus();
        controller.togglePick(name);
        lastPicks = List.copyOf(controller.localPicks());
        syncPoolSelection();
        rebuildLoadout();
        updateChrome();
    }

    private void syncPoolSelection() {
        List<String> picks = controller.localPicks();
        for (PlantCardActor card : poolCards) {
            card.setSelected(picks.contains(card.plantName()));
        }
    }

    private void rebuildLoadout() {
        Skin skin = assets.skin();
        loadoutColumn.clearChildren();
        List<String> picks = controller.localPicks();
        int slots = controller.pickSlots();
        for (int i = 0; i < slots; i++) {
            PlantCardActor card = new PlantCardActor(assets, skin, null);
            card.setSize(LOADOUT_WIDTH, LOADOUT_HEIGHT);
            if (i < picks.size()) {
                String name = picks.get(i);
                if (zombieRole) {
                    card.setZombie(name);
                    card.setCost(IZombieDuelCatalog.costOf(name));
                } else {
                    card.setPlant(name);
                    card.setCost(0);
                }
                card.setOnClick(() -> onPoolClicked(name));
            } else {
                card.setEmpty();
            }
            loadoutColumn.add(card).size(LOADOUT_WIDTH, LOADOUT_HEIGHT).pad(3f).row();
        }
    }
}
