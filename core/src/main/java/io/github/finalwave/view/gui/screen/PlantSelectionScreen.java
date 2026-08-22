package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.collection.CollectionPlantQuery;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.assets.ZombieAnimationCatalog;
import io.github.finalwave.view.gui.hud.SeedLoadoutColumn;
import io.github.finalwave.view.gui.render.ChapterBackground;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PlantCardActor;
import io.github.finalwave.view.gui.widget.PriceButton;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;
import io.github.finalwave.view.gui.widget.UpgradeBadges;
import io.github.finalwave.view.gui.widget.UpgradeSeedBar;

import java.util.List;


public final class PlantSelectionScreen extends MenuScreen {
    private static final float PANEL_WIDTH = 980f;
    private static final float PANEL_HEIGHT = 860f;
    private static final float CARD_WIDTH = 140f;
    private static final float CARD_HEIGHT = 105f;
    private static final int GRID_COLUMNS = 5;
    private static final float BACK_SIZE = 88f;
    private static final float LOADOUT_WIDTH = 128f;
    private static final float ZOMBIE_LANE_WIDTH = 260f;
    private static final float ZOMBIE_SLOT_WIDTH = 170f;
    private static final float ZOMBIE_SLOT_HEIGHT = 190f;
    private static final float ZOMBIE_SCALE = 0.62f;
    private static final float ZOMBIE_STAGGER = 56f;
    private static final float ZOMBIE_FLOOR_PAD = 110f;
    private static final int ZOMBIE_PREVIEW_LIMIT = 4;
    private static final float FRAME_PAD_SIDE = 24f;
    private static final float FRAME_PAD_TOP = 22f;
    private static final float FRAME_PAD_BOTTOM = 20f;
    private static final float DETAIL_HEIGHT = 296f;
    private static final float HEADER_HEIGHT = 58f;
    private static final float PREVIEW_SIZE = 190f;
    private static final float PREVIEW_PAM_SIZE = 110f;
    private static final float SEED_BAR_HEIGHT = 24f;
    private static final float BADGE_SIZE = 40f;
    private static final float UPGRADE_BUTTON_WIDTH = 240f;
    private static final float BOOST_BUTTON_WIDTH = 200f;
    private static final float ACTION_HEIGHT = 60f;
    private static final float START_WIDTH = 280f;
    private static final float START_HEIGHT = 76f;
    private static final float START_PAD = 32f;
    private static final int BOOST_COST_DIAMONDS = 2;
    private static final float DESCRIPTION_FONT_SCALE = 1.12f;
    private static final Color BODY_BROWN = Color.valueOf("4A3018");
    private static final Color DESCRIPTION_OLIVE = Color.valueOf("4C4520");
    private static final Color HEADER_CREAM = Color.valueOf("FFFBE6");
    private static final String DARK_PANEL = "image_ui_if_bundle_reward1_bg_10";
    private static final String GREEN_PLATE = "image_ui_generic_greenbutton_10";
    private static final String GREEN_PLATE_DOWN = "image_ui_generic_greenbutton_down_10";
    private static final String ROUNDED_CREAM_PANEL = "image_ui_dialog_asset_inner_bkgd_10";

    private PlantSelectionController controller;
    private ChapterBackground chapterBackground;
    private PlantAnimationCatalog plantCatalog;
    private ZombieAnimationCatalog zombieCatalog;
    private SeedLoadoutColumn loadoutColumn;
    private String focusedPlant;

    public PlantSelectionScreen(PvzGame game) {
        super(game, new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
    }

    public void bind(PlantSelectionController controller) {
        this.controller = controller;
        this.focusedPlant = null;
        ChapterId chapterId = controller == null || controller.getChapter() == null
                ? ChapterId.ANCIENT_EGYPT
                : controller.getChapter().getId();
        this.chapterBackground = new ChapterBackground(assets, chapterId);
        this.chapterBackground.layoutFor(WORLD_WIDTH, WORLD_HEIGHT, 9);
        if (plantCatalog == null) {
            plantCatalog = new PlantAnimationCatalog(assets.root());
        }
        if (zombieCatalog == null) {
            zombieCatalog = new ZombieAnimationCatalog(assets.root());
        }
        if (controller != null) {
            bindCurrency(controller.getUser());
            List<String> selected = controller.selectedPlants();
            if (!selected.isEmpty()) {
                focusedPlant = selected.get(0);
            } else {
                List<CollectionPlantEntry> plants = controller.plants(CollectionPlantQuery.all());
                if (!plants.isEmpty()) {
                    focusedPlant = plants.get(0).name();
                }
            }
        }
    }

    @Override
    protected void buildUi() {
        setBackground(null);
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        contentLayer.setTouchable(Touchable.childrenOnly);
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
        refresh();
    }

    public void refresh() {
        if (controller == null) {
            return;
        }
        contentLayer.clearChildren();
        Skin skin = assets.skin();

        Table screen = new Table();
        screen.add(leftColumn()).width(LOADOUT_WIDTH).top().left().padLeft(18f).padTop(18f);
        screen.add(chooserPanel(skin)).size(PANEL_WIDTH, PANEL_HEIGHT).expand().center().pad(12f);
        screen.add(zombieLineup()).width(ZOMBIE_LANE_WIDTH).bottom().padBottom(ZOMBIE_FLOOR_PAD).padRight(24f);

        Stack layers = new Stack();
        layers.add(screen);
        layers.add(startButtonCorner(skin));
        contentLayer.add(layers).grow();
        bindCurrency(controller.getUser());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (chapterBackground != null) {
            chapterBackground.draw(batch, viewport);
        }
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    private Table leftColumn() {
        Actor back = PvzButtons.iconButton(assets.region(MenuAssetIds.HUD_BACK), BACK_SIZE, BACK_SIZE, () -> controller.back());
        loadoutColumn = new SeedLoadoutColumn(assets);
        loadoutColumn.refresh(controller);
        Table column = new Table();
        column.top();
        column.add(back).size(BACK_SIZE).padBottom(14f).row();
        column.add(loadoutColumn).top();
        return column;
    }

    private Table chooserPanel(Skin skin) {
        Table chooser = new Table();
        chooser.top().pad(FRAME_PAD_TOP, FRAME_PAD_SIDE, FRAME_PAD_BOTTOM, FRAME_PAD_SIDE);
        chooser.setBackground(StoreChrome.panel());
        chooser.add(detailPanel(skin)).growX().height(DETAIL_HEIGHT).padBottom(12f).row();
        chooser.add(gridPanel(skin)).grow();
        return chooser;
    }

    private Table startButtonCorner(Skin skin) {
        TextButton start = PvzButtons.textButton("LET'S ROCK!", skin, greenStyle(skin), () -> controller.startGame());
        Table corner = new Table();
        corner.setTouchable(Touchable.childrenOnly);
        corner.bottom().right();
        corner.add(start).size(START_WIDTH, START_HEIGHT).padRight(START_PAD).padBottom(START_PAD);
        return corner;
    }

    private Table gridPanel(Skin skin) {
        Table panel = new Table();
        panel.setBackground(gridBackground(skin));
        panel.pad(14f, 10f, 14f, 10f);
        panel.add(plantGrid(skin)).grow();
        return panel;
    }

    private Drawable gridBackground(Skin skin) {
        if (skin.has(DARK_PANEL, Drawable.class)) {
            return skin.getDrawable(DARK_PANEL);
        }
        return StoreChrome.panel();
    }

    private Drawable detailBackground(Skin skin) {
        if (skin.has(ROUNDED_CREAM_PANEL, Drawable.class)) {
            return skin.getDrawable(ROUNDED_CREAM_PANEL);
        }
        return StoreChrome.panel();
    }

    private Table detailPanel(Skin skin) {
        Table panel = new Table();
        panel.setBackground(detailBackground(skin));
        panel.pad(14f, 18f, 16f, 18f);
        CollectionPlantDetail detail = focusedDetail();
        panel.add(nameHeader(skin, detail == null ? "Choose Your Plants" : detail.name()))
                .growX()
                .colspan(2)
                .height(HEADER_HEIGHT)
                .padBottom(12f)
                .row();
        if (detail == null) {
            Label hint = PanelLabels.body(skin, "Tap a seed packet to add it to your loadout.");
            hint.setAlignment(Align.center);
            panel.add(hint).grow().colspan(2);
            return panel;
        }
        panel.add(preview(detail)).size(PREVIEW_SIZE, PREVIEW_SIZE).left().padRight(18f).padLeft(4f);
        panel.add(detailActions(skin, detail)).grow().top();
        return panel;
    }

    private Table nameHeader(Skin skin, String name) {
        Table header = new Table();
        if (skin.has(GREEN_PLATE, Drawable.class)) {
            header.setBackground(skin.getDrawable(GREEN_PLATE));
        }
        Label label = new Label(name, skin, titleStyle(skin));
        label.setAlignment(Align.center);
        label.setColor(HEADER_CREAM);
        header.add(label).grow().pad(2f, 18f, 6f, 18f);
        return header;
    }

    private Table preview(CollectionPlantDetail detail) {
        Table card = new Table();
        String previewId = assets.hasImage(MenuAssetIds.PLANT_PREVIEW_CARD)
                ? MenuAssetIds.PLANT_PREVIEW_CARD
                : MenuAssetIds.ALMANAC_PLANT_CARD;
        card.setBackground(new TextureRegionDrawable(assets.region(previewId)));
        PamActor pam = new PamActor(assets.pamPlayer());
        pam.setClip(plantCatalog.idleFor(detail.name()), 1.15f);
        pam.setTouchable(Touchable.disabled);
        card.add(pam).size(PREVIEW_PAM_SIZE, PREVIEW_PAM_SIZE).expand().center().padBottom(26f);

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.bottom().pad(6f, 8f, 8f, 8f);
        overlay.add(UpgradeBadges.forPlant(assets, detail)).size(BADGE_SIZE).padRight(4f);
        overlay.add(previewSeedBar(detail)).growX().height(SEED_BAR_HEIGHT);

        Stack stack = new Stack();
        stack.add(card);
        stack.add(overlay);
        Table wrap = new Table();
        wrap.add(stack).grow();
        return wrap;
    }

    private Actor previewSeedBar(CollectionPlantDetail detail) {
        int needed = Math.max(1, detail.seedPacketsNeeded());
        float value = detail.maxLevel() ? 1f : Math.min(1f, detail.seedPackets() / (float) needed);
        String text = detail.maxLevel() ? "MAX" : detail.seedPackets() + " / " + needed;
        UpgradeSeedBar bar = new UpgradeSeedBar(assets.skin());
        bar.bind(selectable(detail) ? value : 0f, selectable(detail) ? text : "LOCKED");
        return bar;
    }

    private Table detailActions(Skin skin, CollectionPlantDetail detail) {
        Table column = new Table();
        column.top();
        Label description = new Label(
                CollectionCardLooks.plantAbilityLine(detail.name(), detail.category(), detail.abilityType()),
                skin,
                "medium");
        description.setColor(DESCRIPTION_OLIVE);
        description.setWrap(true);
        description.setFontScale(DESCRIPTION_FONT_SCALE);
        description.setAlignment(Align.topLeft);
        column.add(description).growX().top().padBottom(10f).row();

        PlantDefinition definition = controller.plantRegistry().getDefinition(detail.name());
        int cost = definition == null ? detail.cost() : definition.getCost();
        double recharge = definition == null ? detail.recharge() : definition.getRecharge();
        Label stats = new Label("Cost: " + cost + " | Recharge: " + formatRecharge(recharge), skin, "medium");
        stats.setColor(BODY_BROWN);
        stats.setFontScale(0.9f);
        stats.setAlignment(Align.topLeft);
        column.add(stats).growX().top().row();
        if (selectable(detail)) {
            column.add(actionButtons(skin, detail)).expand().bottom().right();
        }
        return column;
    }

    private Table actionButtons(Skin skin, CollectionPlantDetail detail) {
        Table buttons = new Table();
        if (detail.owned() && !detail.maxLevel()) {
            buttons.add(upgradeButton(skin, detail)).size(UPGRADE_BUTTON_WIDTH, ACTION_HEIGHT).padRight(12f);
        }
        buttons.add(boostButton(skin, detail)).size(BOOST_BUTTON_WIDTH, ACTION_HEIGHT);
        return buttons;
    }

    private Actor upgradeButton(Skin skin, CollectionPlantDetail detail) {
        return PriceButton.coinsLabeled(
                assets,
                skin,
                "UPGRADE",
                Math.max(detail.upgradeCoins(), 0),
                UPGRADE_BUTTON_WIDTH,
                ACTION_HEIGHT,
                StoreChrome.purpleButton(),
                StoreChrome.purpleButtonDown(),
                () -> controller.upgradePlant(detail.name()));
    }

    private Actor boostButton(Skin skin, CollectionPlantDetail detail) {
        if (controller.boostedPlants().contains(detail.name())) {
            return PriceButton.labeled(
                    skin,
                    "BOOSTED",
                    BOOST_BUTTON_WIDTH,
                    ACTION_HEIGHT,
                    StoreChrome.disabledButton(assets.region(MenuAssetIds.STORE_BUY_DISABLED)),
                    null,
                    null);
        }
        if (controller.getUser().hasStoredBoost(detail.name())) {
            return PriceButton.labeled(
                    skin,
                    "BOOST",
                    BOOST_BUTTON_WIDTH,
                    ACTION_HEIGHT,
                    greenPlate(skin, GREEN_PLATE),
                    greenPlate(skin, GREEN_PLATE_DOWN),
                    () -> controller.boostPlant(detail.name()));
        }
        return PriceButton.gemsLabeled(
                assets,
                skin,
                "BOOST",
                BOOST_COST_DIAMONDS,
                BOOST_BUTTON_WIDTH,
                ACTION_HEIGHT,
                greenPlate(skin, GREEN_PLATE),
                greenPlate(skin, GREEN_PLATE_DOWN),
                () -> controller.boostPlant(detail.name()));
    }

    private Drawable greenPlate(Skin skin, String id) {
        if (skin.has(id, Drawable.class)) {
            return skin.getDrawable(id);
        }
        return StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE));
    }

    private Actor plantGrid(Skin skin) {
        Table grid = new Table();
        grid.top();
        List<CollectionPlantEntry> plants = controller.plants(CollectionPlantQuery.all());
        int column = 0;
        for (CollectionPlantEntry entry : plants) {
            if (controller.isRestricted(entry.name())) {
                continue;
            }
            PlantCardActor card = new PlantCardActor(assets, skin, entry.name());
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.bind(entry);
            PlantDefinition definition = controller.plantRegistry().getDefinition(entry.name());
            card.setCost(definition == null ? 0 : definition.getCost());
            boolean selectable = controller.isOwned(entry.name());
            card.setLocked(!selectable);
            if (selectable && !entry.owned()) {
                card.setLevel(1);
            }
            card.setBoosted(controller.isBoosted(entry.name()));
            card.setDisabled(controller.selectedPlants().contains(entry.name()));
            card.setOnClick(() -> onPlantClicked(entry.name()));
            grid.add(card).size(CARD_WIDTH, CARD_HEIGHT).pad(8f);
            column++;
            if (column % GRID_COLUMNS == 0) {
                grid.row();
            }
        }
        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        return scroll;
    }

    private Table zombieLineup() {
        Table lane = new Table();
        lane.bottom().right();
        List<String> aliases = controller.getLevel().getAllowedZombieAliases();
        int shown = 0;
        for (String alias : aliases) {
            if (shown >= ZOMBIE_PREVIEW_LIMIT) {
                break;
            }
            PamActor zombie = new PamActor(assets.pamPlayer());
            zombie.setClip(zombieCatalog.plantClip(alias), ZOMBIE_SCALE);
            zombie.setTouchable(Touchable.disabled);
            float stagger = shown % 2 == 0 ? 0f : ZOMBIE_STAGGER;
            lane.add(zombie).size(ZOMBIE_SLOT_WIDTH, ZOMBIE_SLOT_HEIGHT).right().padRight(stagger).row();
            shown++;
        }
        return lane;
    }

    private void onPlantClicked(String name) {
        focusedPlant = name;
        if (controller.selectedPlants().contains(name)) {
            refresh();
            return;
        }
        controller.addPlant(name);
        if (!controller.selectedPlants().contains(name)) {
            refresh();
        }
    }

    private CollectionPlantDetail focusedDetail() {
        if (focusedPlant == null) {
            List<String> selected = controller.selectedPlants();
            if (!selected.isEmpty()) {
                focusedPlant = selected.get(0);
            }
        }
        if (focusedPlant == null) {
            return null;
        }
        return controller.plantDetail(focusedPlant);
    }

    private boolean selectable(CollectionPlantDetail detail) {
        return detail != null && controller != null && controller.isOwned(detail.name());
    }

    private static String titleStyle(Skin skin) {
        if (skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        return "big";
    }

    private static String greenStyle(Skin skin) {
        if (skin.has("green", TextButton.TextButtonStyle.class)) {
            return "green";
        }
        if (skin.has("green_small", TextButton.TextButtonStyle.class)) {
            return "green_small";
        }
        return "purple";
    }

    private static String formatRecharge(double recharge) {
        if (recharge == (long) recharge) {
            return String.valueOf((long) recharge);
        }
        return String.format("%.1f", recharge);
    }
}
