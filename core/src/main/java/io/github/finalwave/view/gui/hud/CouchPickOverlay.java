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
import io.github.finalwave.controller.CouchIZombieController;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PlantCardActor;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;

import java.util.ArrayList;
import java.util.List;

public final class CouchPickOverlay extends Table {

    private static final float CARD_WIDTH = 140f;
    private static final float CARD_HEIGHT = 105f;
    private static final float LOADOUT_WIDTH = 116f;
    private static final float LOADOUT_HEIGHT = 84f;
    private static final int GRID_COLUMNS = 4;
    private static final float HEADER_HEIGHT = 52f;
    private static final float FOOTER_HEIGHT = 64f;
    private static final float READY_WIDTH = 200f;
    private static final float READY_HEIGHT = 56f;
    private static final String DARK_PANEL = "image_ui_if_bundle_reward1_bg_10";
    private static final String GREEN_PLATE = "image_ui_generic_greenbutton_10";
    private static final Color HEADER_CREAM = Color.valueOf("FFFBE6");
    private static final Color FOCUS_CREAM = Color.valueOf("7FD4FF");

    private final GameAssets assets;
    private final CouchIZombieController controller;
    private final Texture dimTexture;
    private final SidePanel plantPanel;
    private final SidePanel zombiePanel;

    public CouchPickOverlay(GameAssets assets, CouchIZombieController controller) {
        this.assets = assets;
        this.controller = controller;
        setFillParent(true);
        setTouchable(Touchable.enabled);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();

        Image dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTexture)));
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.55f);
        dimmer.setTouchable(Touchable.enabled);

        Skin skin = assets.skin();
        Label heading = PanelLabels.title(skin, "I, Zombie - Couch Play");
        heading.setAlignment(Align.center);
        heading.setColor(HEADER_CREAM);
        Label hint = PanelLabels.body(skin,
                "Player 1 picks plants with the mouse  |  Player 2 picks zombies with the mouse or keyboard (TAB/ARROWS move, SPACE toggles, ENTER starts)");
        hint.setAlignment(Align.center);

        plantPanel = new SidePanel(controller.plantPicks(), "Player 1 - Plants", "Mouse only");
        zombiePanel = new SidePanel(controller.zombiePicks(), "Player 2 - Zombies", "Mouse or keyboard");
        Table columns = new Table();
        columns.add(plantPanel.loadoutColumn())
                .width(LOADOUT_WIDTH + 10f)
                .top()
                .left()
                .padLeft(18f)
                .padTop(10f);
        columns.add(plantPanel.root()).grow().fill().pad(10f);
        columns.add(zombiePanel.root()).grow().fill().pad(10f);
        columns.add(zombiePanel.loadoutColumn())
                .width(LOADOUT_WIDTH + 10f)
                .top()
                .right()
                .padRight(18f)
                .padTop(10f);
        columns.left();

        Stack layers = new Stack();
        layers.setFillParent(true);
        layers.add(dimmer);
        Table content = new Table();
        content.setFillParent(true);
        content.top();
        content.add(heading).growX().padTop(10f).row();
        content.add(hint).growX().padTop(2f).padBottom(4f).row();
        content.add(columns).grow().fill().row();
        layers.add(content);
        addActor(layers);

        plantPanel.rebuild();
        zombiePanel.rebuild();
        plantPanel.updateChrome();
        zombiePanel.updateChrome();
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
        plantPanel.updateChrome();
        zombiePanel.updateChrome();
    }

    public void moveZombieFocus(int delta) {
        zombiePanel.moveFocus(delta);
    }

    public void toggleZombieFocused() {
        zombiePanel.toggleFocused();
    }

    public void submitZombie() {
        controller.zombiePicks().submitPicks();
    }

    private final class SidePanel {
        private final CouchIZombieController.CouchPickSide side;
        private final String title;
        private final String controlHint;
        private final Table root;
        private final Label headerLabel;
        private final Label timerLabel;
        private final Label statusLabel;
        private final Table grid;
        private final Table loadoutColumn;
        private final TextButton readyButton;
        private final List<PlantCardActor> poolCards = new ArrayList<>();
        private final List<PlantCardActor> loadoutCards = new ArrayList<>();
        private List<String> lastPicks = List.of();
        private int focusIndex;

        private SidePanel(CouchIZombieController.CouchPickSide side, String title, String controlHint) {
            this.side = side;
            this.title = title;
            this.controlHint = controlHint;
            Skin skin = assets.skin();

            root = new Table(skin);
            root.pad(12f, 14f, 12f, 14f);
            root.setBackground(StoreChrome.panel());

            headerLabel = new Label(title, skin, titleStyle(skin));
            headerLabel.setAlignment(Align.center);
            headerLabel.setColor(side.zombieSide() ? FOCUS_CREAM : HEADER_CREAM);
            Table header = new Table();
            if (skin.has(GREEN_PLATE, Drawable.class)) {
                header.setBackground(skin.getDrawable(GREEN_PLATE));
            }
            header.add(headerLabel).grow().pad(2f, 14f, 4f, 14f);
            root.add(header).growX().height(HEADER_HEIGHT).padBottom(8f).row();

            loadoutColumn = new Table(skin);
            loadoutColumn.top();
            grid = new Table(skin);
            ScrollPane scroll = new ScrollPane(grid, skin);
            scroll.setFadeScrollBars(false);
            scroll.setScrollingDisabled(true, false);
            Table gridPanel = new Table(skin);
            gridPanel.setBackground(gridBackground(skin));
            gridPanel.pad(10f, 8f, 10f, 8f);
            gridPanel.add(scroll).grow();
            root.add(gridPanel).grow().padBottom(8f).row();

            timerLabel = new Label("", skin, "secondary");
            statusLabel = PanelLabels.body(skin, "");
            readyButton = PvzButtons.textButton("LET'S ROCK!", skin, readyStyle(skin), side::submitPicks);
            Table footer = new Table(skin);
            Table status = new Table(skin);
            status.add(timerLabel).left().row();
            status.add(statusLabel).left();
            footer.add(status).left().expandX();
            footer.add(readyButton).size(READY_WIDTH, READY_HEIGHT).right();
            root.add(footer).growX().height(FOOTER_HEIGHT);
        }

        private Table root() {
            return root;
        }

        private Table loadoutColumn() {
            return loadoutColumn;
        }

        private void rebuild() {
            Skin skin = assets.skin();
            grid.clearChildren();
            if (side.zombieSide()) {
                grid.top().right();
            } else {
                grid.top().left();
            }
            poolCards.clear();
            List<String> pool = side.pickPool();
            for (int i = 0; i < pool.size(); i++) {
                String name = pool.get(i);
                PlantCardActor card = new PlantCardActor(assets, skin, name);
                card.setSize(CARD_WIDTH, CARD_HEIGHT);
                if (side.zombieSide()) {
                    card.setZombie(name);
                    card.setCost(IZombieDuelCatalog.costOf(name));
                } else {
                    bindPlantCard(card, name);
                }
                card.setOnClick(() -> onCardClicked(name));
                poolCards.add(card);
                grid.add(card).size(CARD_WIDTH, CARD_HEIGHT).pad(8f);
                if ((i + 1) % GRID_COLUMNS == 0) {
                    grid.row();
                }
            }
            rebuildLoadout();
            syncSelection();
            refreshFocus();
        }

        private void bindPlantCard(PlantCardActor card, String name) {
            CollectionPlantEntry entry = controller.plantEntry(name);
            if (entry != null) {
                card.bind(entry);
            }
            card.setCost(controller.plantCost(name));
            boolean selectable = controller.plantSelectable(name);
            card.setLocked(!selectable);
            if (selectable && entry != null && !entry.owned()) {
                card.setLevel(1);
            }
            card.setBoosted(controller.plantBoosted(name));
        }

        private void rebuildLoadout() {
            Skin skin = assets.skin();
            loadoutColumn.clearChildren();
            loadoutCards.clear();
            List<String> picks = side.localPicks();
            for (int i = 0; i < side.pickSlots(); i++) {
                PlantCardActor card = new PlantCardActor(assets, skin, null);
                card.setSize(LOADOUT_WIDTH, LOADOUT_HEIGHT);
                if (i < picks.size()) {
                    String name = picks.get(i);
                    if (side.zombieSide()) {
                        card.setZombie(name);
                        card.setCost(IZombieDuelCatalog.costOf(name));
                    } else {
                        card.setPlant(name);
                        card.setLevel(0);
                        card.setFamily(null);
                        card.setBoosted(controller.plantBoosted(name));
                        card.setCost(controller.plantCost(name));
                        card.setOnClick(() -> onCardClicked(name));
                    }
                } else {
                    card.setEmpty();
                    card.setTouchable(Touchable.disabled);
                }
                loadoutCards.add(card);
                loadoutColumn.add(card).size(LOADOUT_WIDTH, LOADOUT_HEIGHT).pad(3f).row();
            }
        }

        private void onCardClicked(String name) {
            if (side.zombieSide()) {
                List<String> pool = side.pickPool();
                int index = pool.indexOf(name);
                if (index >= 0) {
                    focusIndex = index;
                }
            }
            side.togglePick(name);
            lastPicks = List.copyOf(side.localPicks());
            syncSelection();
            rebuildLoadout();
            refreshFocus();
            updateChrome();
        }

        private void toggleFocused() {
            List<String> pool = side.pickPool();
            if (pool.isEmpty()) {
                return;
            }
            onCardClicked(pool.get(Math.min(focusIndex, pool.size() - 1)));
        }

        private void moveFocus(int delta) {
            List<String> pool = side.pickPool();
            if (pool.isEmpty()) {
                return;
            }
            focusIndex = Math.floorMod(focusIndex + delta, pool.size());
            refreshFocus();
        }

        private void syncSelection() {
            List<String> picks = side.localPicks();
            for (PlantCardActor card : poolCards) {
                if (side.zombieSide()) {
                    card.setSelected(picks.contains(card.plantName()));
                } else {
                    card.setDisabled(picks.contains(card.plantName()));
                }
            }
        }

        private void refreshFocus() {
            List<String> pool = side.pickPool();
            if (focusIndex >= 0 && focusIndex < pool.size()) {
                headerLabel.setText(title + "  -  " + pool.get(focusIndex));
                Color base = side.zombieSide() ? FOCUS_CREAM : HEADER_CREAM;
                headerLabel.setColor(base);
            } else {
                headerLabel.setText(title + "  -  " + controlHint);
            }
        }

        private void updateChrome() {
            timerLabel.setText("Pick time left: " + side.pickSecondsLeft() + "s");
            statusLabel.setText("Selected " + side.localPicks().size() + " / " + side.pickSlots());
            List<String> picks = side.localPicks();
            if (!picks.equals(lastPicks)) {
                lastPicks = List.copyOf(picks);
                syncSelection();
                rebuildLoadout();
            }
            if (side.zombieSide()) {
                refreshFocus();
            }
            if (side.ready()) {
                timerLabel.setText("Locked in!");
                readyButton.setDisabled(true);
                readyButton.setColor(0.6f, 0.6f, 0.6f, 1f);
            }
        }
    }

    private static Drawable gridBackground(Skin skin) {
        if (skin.has(DARK_PANEL, Drawable.class)) {
            return skin.getDrawable(DARK_PANEL);
        }
        return StoreChrome.panel();
    }

    private static String titleStyle(Skin skin) {
        if (skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        return "big";
    }

    private static String readyStyle(Skin skin) {
        if (skin.has("green", TextButton.TextButtonStyle.class)) {
            return "green";
        }
        if (skin.has("green_small", TextButton.TextButtonStyle.class)) {
            return "green_small";
        }
        return "purple";
    }
}
