package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.collection.CollectionPlantQuery;
import io.github.finalwave.model.collection.CollectionZombieEntry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.hud.SeedLoadoutBar;
import io.github.finalwave.view.gui.render.ChapterBackground;
import io.github.finalwave.view.gui.widget.CollectionZombieCard;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PlantCardActor;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;
import pvz.skin.BorderedTable;

import java.util.List;


public final class PlantSelectionScreen extends MenuScreen {
    private static final float PANEL_WIDTH = 980f;
    private static final float PANEL_HEIGHT = 860f;
    private static final float CARD_WIDTH = 140f;
    private static final float CARD_HEIGHT = 105f;
    private static final int GRID_COLUMNS = 5;
    private static final Color TITLE_WHITE = Color.valueOf("FFFFFF");
    private static final Color BODY_BROWN = Color.valueOf("4A3018");
    private static final String BUNDLE_BG = "image_ui_if_bundle_reward1_bg_10";

    private PlantSelectionController controller;
    private ChapterBackground chapterBackground;
    private PlantAnimationCatalog plantCatalog;
    private SeedLoadoutBar loadoutBar;
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

        Actor back = PvzButtons.iconButton(assets.region(MenuAssetIds.HUD_BACK), 88f, 88f, () -> controller.back());
        Table chooser = chooserPanel(skin);
        Table zombies = zombieColumn(skin);

        Table screen = new Table();
        screen.add(back).size(88f).padLeft(18f).padTop(18f).top().left();
        screen.add(chooser).size(PANEL_WIDTH, PANEL_HEIGHT).expand().center().pad(12f);
        screen.add(zombies).width(140f).top().padTop(40f).padRight(16f);
        contentLayer.add(screen).grow();
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

    private Table chooserPanel(Skin skin) {
        Table chooser = new Table();
        chooser.top().pad(18f, 22f, 16f, 22f);
        if (skin.has(BUNDLE_BG, Drawable.class)) {
            chooser.setBackground(skin.getDrawable(BUNDLE_BG));
        } else {
            chooser.setBackground(StoreChrome.panel());
        }

        loadoutBar = new SeedLoadoutBar(assets);
        loadoutBar.refresh(controller);

        TextButton start = PvzButtons.textButton("LET'S ROCK!", skin, greenStyle(skin), () -> controller.startGame());
        chooser.add(detailPanel(skin)).growX().height(250f).padBottom(8f).row();
        chooser.add(plantGrid(skin)).grow().padBottom(8f).row();
        chooser.add(loadoutBar).padBottom(10f).row();
        chooser.add(start).size(250f, 60f).padBottom(8f);
        return chooser;
    }

    private Table detailPanel(Skin skin) {
        BorderedTable panel = new BorderedTable();
        panel.pad(12f, 18f, 16f, 18f);
        CollectionPlantDetail detail = focusedDetail();
        if (detail == null) {
            Label hint = PanelLabels.body(skin, "Choose plants for this level.");
            hint.setAlignment(Align.center);
            panel.add(hint).grow();
            return panel;
        }
        Label name = PanelLabels.title(skin, detail.name());
        name.setAlignment(Align.center);
        panel.add(name).growX().colspan(2).padTop(8f).padBottom(8f).row();
        panel.add(preview(detail)).size(180f, 180f).left().padRight(16f).padLeft(8f).padBottom(12f);
        panel.add(detailActions(skin, detail)).grow().top();
        return panel;
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
        card.add(pam).size(100f, 100f).expand().center().padBottom(36f);
        if (!controller.isBoosted(detail.name())) {
            return card;
        }
        Image sprout = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.SPROUT_ICON)));
        sprout.setScaling(Scaling.fit);
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.bottom().left().pad(8f);
        overlay.add(sprout).size(32f, 32f);
        Stack stack = new Stack();
        stack.add(card);
        stack.add(overlay);
        Table wrap = new Table();
        wrap.add(stack).size(180f, 180f);
        return wrap;
    }

    private Table detailActions(Skin skin, CollectionPlantDetail detail) {
        Table column = new Table();
        PlantDefinition definition = controller.plantRegistry().getDefinition(detail.name());
        int cost = definition == null ? detail.cost() : definition.getCost();
        double recharge = definition == null ? detail.recharge() : definition.getRecharge();
        Label stats = new Label("Cost: " + cost + " | Recharge: " + formatRecharge(recharge), skin, "medium");
        stats.setColor(BODY_BROWN);
        stats.setWrap(true);
        stats.setAlignment(Align.topLeft);
        column.add(stats).growX().height(56f).top().row();
        if (detail.owned()) {
            column.add(actionButtons(skin, detail)).right().expandX().bottom();
        }
        return column;
    }

    private Table actionButtons(Skin skin, CollectionPlantDetail detail) {
        Table buttons = new Table();
        String green = greenStyle(skin);
        if (detail.canUpgrade()) {
            TextButton upgrade = PvzButtons.textButton("UPGRADE", skin, "purple", () -> controller.upgradePlant(detail.name()));
            buttons.add(upgrade).size(130f, 50f).padRight(8f);
        }
        boolean matchBoosted = controller.boostedPlants().contains(detail.name());
        if (matchBoosted) {
            TextButton boosted = PvzButtons.textButton("BOOSTED", skin, green, null);
            boosted.setDisabled(true);
            boosted.setTouchable(Touchable.disabled);
            buttons.add(boosted).size(110f, 50f).padRight(8f);
        } else {
            TextButton boost = PvzButtons.textButton("x2 BOOST", skin, green, () -> controller.boostPlant(detail.name()));
            Image gem = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.STORE_PRICE_GEM)));
            gem.setScaling(Scaling.fit);
            boost.add(gem).size(22f, 22f).padLeft(4f);
            buttons.add(boost).size(130f, 50f).padRight(8f);
        }
        buttons.add(selectButton(skin, detail, green)).size(120f, 50f);
        return buttons;
    }

    private TextButton selectButton(Skin skin, CollectionPlantDetail detail, String green) {
        boolean restricted = controller.isRestricted(detail.name());
        boolean inLoadout = controller.selectedPlants().contains(detail.name());
        if (restricted) {
            TextButton banned = PvzButtons.textButton("BANNED", skin, green, null);
            banned.setDisabled(true);
            banned.setTouchable(Touchable.disabled);
            return banned;
        }
        if (!detail.owned()) {
            TextButton locked = PvzButtons.textButton("LOCKED", skin, green, null);
            locked.setDisabled(true);
            locked.setTouchable(Touchable.disabled);
            return locked;
        }
        if (inLoadout) {
            return PvzButtons.textButton("DESELECT", skin, green, () -> controller.removePlant(detail.name()));
        }
        return PvzButtons.textButton("SELECT", skin, green, () -> controller.addPlant(detail.name()));
    }

    private Actor plantGrid(Skin skin) {
        Table grid = new Table();
        grid.top();
        List<CollectionPlantEntry> plants = controller.plants(CollectionPlantQuery.all());
        int column = 0;
        for (CollectionPlantEntry entry : plants) {
            PlantCardActor card = new PlantCardActor(assets, skin, entry.name());
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.bind(entry);
            PlantDefinition definition = controller.plantRegistry().getDefinition(entry.name());
            card.setCost(definition == null ? 0 : definition.getCost());
            boolean restricted = controller.isRestricted(entry.name());
            card.setLocked(!entry.owned() || restricted);
            card.setBoosted(controller.isBoosted(entry.name()));
            card.setSelected(controller.selectedPlants().contains(entry.name()));
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

    private Table zombieColumn(Skin skin) {
        Table column = new Table();
        column.top();
        List<String> aliases = controller.getLevel().getAllowedZombieAliases();
        if (aliases.isEmpty()) {
            return column;
        }
        Label title = new Label("Zombies", skin, "medium");
        title.setColor(TITLE_WHITE);
        column.add(title).padBottom(8f).row();
        int shown = 0;
        for (String alias : aliases) {
            if (shown >= 4) {
                break;
            }
            CollectionZombieCard card = new CollectionZombieCard(assets, skin);
            card.bind(new CollectionZombieEntry(alias, true, 0, 0d, "", ""));
            column.add(card).size(120f, 150f).padBottom(8f).row();
            shown++;
        }
        return column;
    }

    private void onPlantClicked(String name) {
        focusedPlant = name;
        if (controller.selectedPlants().contains(name)) {
            controller.removePlant(name);
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
