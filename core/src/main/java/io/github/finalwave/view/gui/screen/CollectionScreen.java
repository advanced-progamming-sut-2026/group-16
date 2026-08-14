package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.CollectionController;
import io.github.finalwave.model.collection.CollectionCounts;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.collection.CollectionPlantFilter;
import io.github.finalwave.model.collection.CollectionPlantQuery;
import io.github.finalwave.model.collection.CollectionZombieDetail;
import io.github.finalwave.model.collection.CollectionZombieEntry;
import io.github.finalwave.model.collection.PlantCollection;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.assets.ZombieAnimationCatalog;
import io.github.finalwave.view.gui.widget.CollectionPlantCard;
import io.github.finalwave.view.gui.widget.CollectionZombieCard;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PriceButton;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;
import io.github.finalwave.view.gui.widget.ThemedSelectBox;
import io.github.finalwave.view.gui.widget.UpgradeSeedBar;

import java.util.List;
import java.util.Locale;


public final class CollectionScreen extends MenuScreen {
    private static final float BACK_SIZE = 96f;
    private static final float CLOSE_HEIGHT = 56f;
    private static final float TAB_WIDTH = 124f;
    private static final float TAB_IDLE_HEIGHT = 100f;
    private static final float TAB_ACTIVE_HEIGHT = 124f;
    private static final float TAB_OVERLAP = 24f;
    private static final float CARD_PAD = 8f;
    private static final float GRID_PAD_TOP = 48f;
    private static final float CONTENT_PAD_X = 36f;
    private static final int PLANT_COLUMNS = 8;
    private static final float PREVIEW_WIDTH = 450f;
    private static final float PREVIEW_HEIGHT = 550f;
    private static final float INFO_WIDTH = 850f;
    private static final float SEED_BAR_WIDTH = 450f;
    private static final float SEED_BAR_HEIGHT = 26f;
    private static final float UPGRADE_BUTTON_WIDTH = 300f;
    private static final float BADGE_SIZE = 56f;
    private static final float TAB_ICON_ACTIVE = 62f;
    private static final float TAB_ICON_IDLE = 52f;
    private static final Color TITLE_WHITE = Color.valueOf("FFFFFF");
    private static final Color LORE_YELLOW = Color.valueOf("FFE566");
    private static final Color BODY_WHITE = Color.valueOf("FFF8E7");
    private static final Color FOOTER_CREAM = Color.valueOf("FFF3C4");
    private static final Color UPGRADE_GREEN = Color.valueOf("7CFF4A");
    private static final Color IDLE_TAB_ICON = new Color(1f, 1f, 1f, 0.78f);
    private static final String ALL_FAMILIES = "All families";

    private enum Tab {
        PLANTS,
        ZOMBIES
    }

    private enum Mode {
        GRID,
        PLANT,
        ZOMBIE
    }

    private CollectionController controller;
    private PlantAnimationCatalog plantCatalog;
    private ZombieAnimationCatalog zombieCatalog;
    private Table idleTabHost;
    private Table activeTabHost;
    private Table cardGrid;
    private Label footerStatus;
    private Actor filterButton;
    private Tab activeTab = Tab.PLANTS;
    private Mode mode = Mode.GRID;
    private CollectionPlantQuery query = CollectionPlantQuery.all();
    private String selectedPlant;
    private String selectedZombie;

    public CollectionScreen(PvzGame game) {
        super(game);
    }

    public void bind(CollectionController controller) {
        this.controller = controller;
        this.mode = Mode.GRID;
        this.activeTab = Tab.PLANTS;
        this.query = CollectionPlantQuery.all();
        this.selectedPlant = null;
        this.selectedZombie = null;
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        contentLayer.setTouchable(Touchable.childrenOnly);
        if (plantCatalog == null) {
            plantCatalog = new PlantAnimationCatalog(assets.root());
        }
        if (zombieCatalog == null) {
            zombieCatalog = new ZombieAnimationCatalog(assets.root());
        }
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
        refresh();
    }

    public void refresh() {
        if (controller == null) {
            return;
        }
        if (mode == Mode.PLANT && selectedPlant != null) {
            CollectionPlantDetail detail = controller.plantDetail(selectedPlant);
            if (detail != null) {
                showPlantDetail(detail);
                return;
            }
            mode = Mode.GRID;
        }
        if (mode == Mode.ZOMBIE && selectedZombie != null) {
            CollectionZombieDetail detail = controller.zombieDetail(selectedZombie);
            if (detail != null) {
                showZombieDetail(detail);
                return;
            }
            mode = Mode.GRID;
        }
        showGrid();
    }

    private void showGrid() {
        mode = Mode.GRID;
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        contentLayer.pad(100f, CONTENT_PAD_X, 24f, CONTENT_PAD_X);
        buildHud(true);
        Stack root = new Stack();
        root.setTouchable(Touchable.childrenOnly);
        Table idleLayer = new Table();
        idleLayer.setTouchable(Touchable.childrenOnly);
        idleLayer.top().left().padLeft(18f);
        idleTabHost = new Table();
        idleLayer.add(idleTabHost).left().top();

        Table panelLayer = new Table();
        panelLayer.setTouchable(Touchable.childrenOnly);
        panelLayer.top().padTop(TAB_IDLE_HEIGHT - TAB_OVERLAP);
        panelLayer.add(collectionWindow()).grow();

        Table frontLayer = new Table();
        frontLayer.setTouchable(Touchable.childrenOnly);
        frontLayer.top().padLeft(18f);
        activeTabHost = new Table();
        frontLayer.add(activeTabHost).left().top();
        frontLayer.add().expandX();

        root.add(idleLayer);
        root.add(panelLayer);
        root.add(frontLayer);
        root.add(closeOverlay());
        contentLayer.add(root).grow();
        rebuildTabs();
        refreshGrid();
    }

    private void buildHud(boolean grid) {
        hudLayer.clearChildren();
        hudLayer.top().left();
        hudLayer.padTop(10f).padLeft(16f).padRight(24f).padBottom(0f);
        if (!grid) {
            Actor back = PvzButtons.iconButton(
                    assets.region(MenuAssetIds.ALMANAC_DETAIL_BACK),
                    BACK_SIZE,
                    BACK_SIZE,
                    this::onBack);
            hudLayer.add(back).size(BACK_SIZE).left();
        }
        hudLayer.add().expandX();
        hudLayer.add(currencyBar).right().padTop(14f);
    }

    private Table collectionWindow() {
        Table frame = new Table();
        frame.setBackground(StoreChrome.panel());
        frame.setTouchable(Touchable.childrenOnly);
        frame.pad(
                StoreChrome.PANEL_PAD_TOP,
                StoreChrome.PANEL_PAD_LEFT,
                16f,
                StoreChrome.PANEL_PAD_RIGHT);

        cardGrid = new Table();
        cardGrid.top().left();
        cardGrid.setTouchable(Touchable.childrenOnly);
        ScrollPane scroll = new ScrollPane(cardGrid, assets.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        scroll.setFlickScroll(false);
        frame.add(scroll).grow().padTop(GRID_PAD_TOP).row();
        frame.add(footerBar()).growX().height(68f).padTop(8f);
        return frame;
    }

    private Table footerBar() {
        Table footer = new Table();
        filterButton = PvzButtons.iconButton(
                assets.region(MenuAssetIds.ALMANAC_FILTER_UP),
                64f,
                64f,
                this::showFilterModal);
        footerStatus = new Label("", assets.skin(), outlineStyle(assets.skin()));
        footerStatus.setColor(FOOTER_CREAM);
        footerStatus.setFontScale(0.78f);
        footer.add(filterButton).size(64f).padRight(12f);
        footer.add(footerStatus).left().expandX();
        return footer;
    }

    private Table closeOverlay() {
        TextureRegion region = assets.region(MenuAssetIds.STORE_CLOSE);
        float closeWidth = CLOSE_HEIGHT * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
        Actor close = PvzButtons.iconButton(region, closeWidth, CLOSE_HEIGHT, this::leaveCollection);
        Table overlay = new Table();
        overlay.setTouchable(Touchable.childrenOnly);
        overlay.top().right();
        overlay.padTop(TAB_IDLE_HEIGHT - TAB_OVERLAP);
        overlay.add(close)
                .size(closeWidth, CLOSE_HEIGHT)
                .padTop(-CLOSE_HEIGHT + 12f)
                .padRight(22f);
        return overlay;
    }

    private void refreshGrid() {
        if (cardGrid == null || controller == null) {
            return;
        }
        if (filterButton != null) {
            boolean plants = activeTab == Tab.PLANTS;
            filterButton.setVisible(plants);
            filterButton.setTouchable(plants ? Touchable.enabled : Touchable.disabled);
        }
        cardGrid.clearChildren();
        cardGrid.top().left();
        int column = 0;
        if (activeTab == Tab.PLANTS) {
            List<CollectionPlantEntry> plants = controller.plants(query);
            for (CollectionPlantEntry entry : plants) {
                CollectionPlantCard card = new CollectionPlantCard(assets, assets.skin());
                card.bind(entry);
                PvzButtons.animate(card, 1.06f, 0.94f, () -> openPlant(entry.name()));
                cardGrid.add(card)
                        .size(CollectionPlantCard.CARD_WIDTH, CollectionPlantCard.CARD_HEIGHT)
                        .pad(CARD_PAD)
                        .padBottom(18f);
                column++;
                if (column % PLANT_COLUMNS == 0) {
                    cardGrid.row();
                }
            }
            CollectionCounts counts = controller.plantCounts();
            footerStatus.setText(filterCaption() + "    Plants collected: "
                    + counts.owned() + " of " + counts.total());
        } else {
            List<CollectionZombieEntry> zombies = controller.zombies();
            int seen = 0;
            int columns = zombieColumns();
            for (CollectionZombieEntry entry : zombies) {
                if (entry.seen()) {
                    seen++;
                }
                CollectionZombieCard card = new CollectionZombieCard(assets, assets.skin());
                card.bind(entry);
                PvzButtons.animate(card, 1.06f, 0.94f, () -> openZombie(entry));
                cardGrid.add(card)
                        .size(CollectionZombieCard.CARD_WIDTH, CollectionZombieCard.CARD_HEIGHT)
                        .pad(CARD_PAD);
                column++;
                if (column % columns == 0) {
                    cardGrid.row();
                }
            }
            footerStatus.setText("Zombies discovered: " + seen + " of " + zombies.size());
        }
        bindCurrency(controller.getUser());
    }

    private int zombieColumns() {
        float inner = viewport.getWorldWidth()
                - CONTENT_PAD_X * 2f
                - StoreChrome.PANEL_PAD_LEFT
                - StoreChrome.PANEL_PAD_RIGHT
                - 16f;
        float cell = CollectionZombieCard.CARD_WIDTH + CARD_PAD * 2f;
        return Math.max(1, (int) (inner / cell));
    }

    private void rebuildTabs() {
        if (idleTabHost == null || activeTabHost == null) {
            return;
        }
        idleTabHost.clearChildren();
        activeTabHost.clearChildren();
        addTab(Tab.PLANTS, MenuAssetIds.ALMANAC_TAB_PLANTS_ACTIVE, MenuAssetIds.ALMANAC_TAB_PLANTS_IDLE,
                MenuAssetIds.STORE_TAB_ICON_SEEDS);
        addTab(Tab.ZOMBIES, MenuAssetIds.ALMANAC_TAB_ZOMBIES_ACTIVE, MenuAssetIds.ALMANAC_TAB_ZOMBIES_IDLE,
                MenuAssetIds.STORE_TAB_ICON_ZOMBIES);
    }

    private void addTab(Tab tab, String activeId, String idleId, String iconId) {
        boolean active = activeTab == tab;
        if (active) {
            idleTabHost.add().size(TAB_WIDTH, TAB_IDLE_HEIGHT).padRight(8f);
            activeTabHost.add(tabButton(tab, activeId, iconId, true)).size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).padRight(8f).top();
        } else {
            idleTabHost.add(tabButton(tab, idleId, iconId, false)).size(TAB_WIDTH, TAB_IDLE_HEIGHT).padRight(8f).top();
            activeTabHost.add().size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).padRight(8f);
        }
    }

    private Actor tabButton(Tab tab, String plateId, String iconId, boolean active) {
        Image plate = new Image(new TextureRegionDrawable(assets.region(plateId)));
        plate.setScaling(Scaling.stretch);
        Image icon = new Image(new TextureRegionDrawable(assets.region(iconId)));
        icon.setScaling(Scaling.fit);
        if (!active) {
            icon.setColor(IDLE_TAB_ICON);
        }
        Table iconHost = new Table();
        iconHost.setTouchable(Touchable.disabled);
        iconHost.top();
        iconHost.add(icon)
                .size(active ? TAB_ICON_ACTIVE : TAB_ICON_IDLE)
                .padTop(8f)
                .padBottom(active ? 24f : 14f);
        Stack stack = new Stack();
        stack.add(plate);
        stack.add(iconHost);
        PvzButtons.animate(stack, active ? 1.02f : 1.08f, 0.94f, () -> selectTab(tab));
        return stack;
    }

    private void selectTab(Tab tab) {
        if (activeTab == tab && mode == Mode.GRID) {
            return;
        }
        activeTab = tab;
        mode = Mode.GRID;
        selectedPlant = null;
        selectedZombie = null;
        showGrid();
    }

    private void openPlant(String name) {
        CollectionPlantDetail detail = controller.plantDetail(name);
        if (detail == null) {
            return;
        }
        selectedPlant = name;
        showPlantDetail(detail);
    }

    private void openZombie(CollectionZombieEntry entry) {
        CollectionZombieDetail detail = controller.zombieDetail(entry.alias());
        if (detail == null) {
            return;
        }
        selectedZombie = entry.alias();
        showZombieDetail(detail);
    }

    private void showPlantDetail(CollectionPlantDetail detail) {
        mode = Mode.PLANT;
        selectedPlant = detail.name();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        contentLayer.pad(16f, 40f, 16f, 40f);
        buildHud(false);
        Skin skin = assets.skin();
        Label title = new Label(detail.name(), skin, titleStyle(skin));
        title.setAlignment(Align.center);
        title.setColor(TITLE_WHITE);
        contentLayer.add(title).growX().padBottom(12f).row();
        Table body = new Table();
        body.add(plantPreviewColumn(detail)).center().padRight(56f);
        body.add(plantInfo(skin, detail)).width(INFO_WIDTH).center().left();
        contentLayer.add(body).expand().center();
        contentLayer.row();
        contentLayer.add(detailNav(true)).growX().padTop(8f);
        bindCurrency(controller.getUser());
    }

    private void showZombieDetail(CollectionZombieDetail detail) {
        mode = Mode.ZOMBIE;
        selectedZombie = detail.alias();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        contentLayer.pad(16f, 40f, 16f, 40f);
        buildHud(false);
        Table body = new Table();
        body.add(lawnPreview(zombieCatalog.plantClip(detail.alias()), 0.72f, 220f, null))
                .size(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                .center()
                .padRight(56f);
        body.add(zombieInfo(assets.skin(), detail)).width(INFO_WIDTH).center().left();
        contentLayer.add(body).expand().center();
        contentLayer.row();
        contentLayer.add(detailNav(false)).growX().padTop(8f);
        bindCurrency(controller.getUser());
    }

    private Table plantPreviewColumn(CollectionPlantDetail detail) {
        Table column = new Table();
        column.add(lawnPreview(plantCatalog.idleFor(detail.name()), 0.84f, 320f, detail))
                .size(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                .row();
        column.add(detailSeedBar(detail)).width(SEED_BAR_WIDTH + BADGE_SIZE).height(BADGE_SIZE).padTop(10f).row();
        Actor action = plantAction(detail);
        if (action != null) {
            column.add(action).width(UPGRADE_BUTTON_WIDTH).height(58f).padTop(14f);
        }
        return column;
    }

    private Table lawnPreview(PlantAnimationCatalog.ClipSpec clip, float scale, float pamSize, CollectionPlantDetail plant) {
        Table preview = new Table();
        preview.setBackground(new TextureRegionDrawable(assets.region(MenuAssetIds.ALMANAC_LAWN)));
        PamActor pam = new PamActor(assets.pamPlayer());
        pam.setClip(clip, scale);
        pam.setTouchable(Touchable.disabled);
        preview.add(pam).size(pamSize, pamSize).expand().center();
        if (plant == null || !plant.owned()) {
            return preview;
        }
        Label level = new Label("Level " + plant.level(), assets.skin(), outlineStyle(assets.skin()));
        level.setAlignment(Align.center);
        level.setColor(TITLE_WHITE);
        level.setFontScale(0.72f);
        Table overlay = new Table();
        overlay.bottom();
        overlay.add(level).padBottom(16f);
        Stack stack = new Stack();
        stack.add(preview);
        stack.add(overlay);
        Table wrap = new Table();
        wrap.add(stack).grow();
        return wrap;
    }

    private Table detailSeedBar(CollectionPlantDetail plant) {
        Skin skin = assets.skin();
        float value = plant.maxLevel() ? 1f : Math.min(1f, plant.seedPackets() / (float) Math.max(1, plant.seedPacketsNeeded()));
        String text = plant.maxLevel() ? "MAX" : plant.seedPackets() + "/" + Math.max(1, plant.seedPacketsNeeded());
        UpgradeSeedBar bar = new UpgradeSeedBar(skin);
        bar.bind(value, text);
        PamActor badge = new PamActor(assets.pamPlayer());
        badge.setAnchor(0.5f, 0.5f);
        badge.setTouchable(Touchable.disabled);
        badge.setClip(PlantAnimationCatalog.UPGRADE_BADGE_PAM, upgradeBadgeClip(plant), 0.42f, true);
        Table row = new Table();
        row.add(badge).size(BADGE_SIZE).padRight(6f);
        row.add(bar).width(SEED_BAR_WIDTH - 8f).height(SEED_BAR_HEIGHT);
        return row;
    }

    private static String upgradeBadgeClip(CollectionPlantDetail plant) {
        if (!plant.owned()) {
            return "locked";
        }
        if (plant.maxLevel() || plant.canUpgrade()) {
            return "idle";
        }
        return "no_charge";
    }

    private Actor plantAction(CollectionPlantDetail plant) {
        if (!plant.owned()) {
            return PriceButton.coinsLabeled(
                    assets,
                    assets.skin(),
                    "BUY",
                    PlantCollection.PURCHASE_COST_COINS,
                    UPGRADE_BUTTON_WIDTH,
                    58f,
                    () -> controller.purchasePlant(plant.name()));
        }
        if (plant.maxLevel()) {
            return null;
        }
        return PriceButton.coinsLabeled(
                assets,
                assets.skin(),
                "UPGRADE",
                Math.max(plant.upgradeCoins(), 0),
                UPGRADE_BUTTON_WIDTH,
                58f,
                StoreChrome.purpleButton(),
                StoreChrome.purpleButtonDown(),
                () -> controller.upgradePlant(plant.name()));
    }

    private Table plantInfo(Skin skin, CollectionPlantDetail detail) {
        Table stats = new Table();
        stats.left().top();
        float blockWidth = 360f;
        float padY = 10f;
        stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_SUN, "SUN COST",
                        deltaValue(skin, String.valueOf(detail.cost()),
                                detail.nextCost() == null ? null : String.valueOf(detail.nextCost()))))
                .width(blockWidth).padBottom(padY).left();
        stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_RECHARGE, "RECHARGE",
                        deltaValue(skin, formatNumber(detail.recharge()),
                                detail.nextRecharge() == null ? null : formatNumber(detail.nextRecharge()))))
                .width(blockWidth).padBottom(padY).left().row();
        stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_TOUGHNESS, "TOUGHNESS",
                        deltaValue(skin, String.valueOf(detail.maxHealth()),
                                detail.nextMaxHealth() == null ? null : String.valueOf(detail.nextMaxHealth()))))
                .width(blockWidth).padBottom(padY).left();
        if ("SUN_PRODUCER".equalsIgnoreCase(detail.category())) {
            stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_SUN_PRODUCTION, "SUN PRODUCTION",
                            deltaValue(skin, formatNumber(detail.abilityValue()), null)))
                    .width(blockWidth).padBottom(padY).left().row();
        } else {
            stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_DAMAGE, "DAMAGE",
                            deltaValue(skin, String.valueOf(detail.damage()),
                                    detail.nextDamage() == null ? null : String.valueOf(detail.nextDamage()))))
                    .width(blockWidth).padBottom(padY).left().row();
        }
        String plantFood = CollectionCardLooks.plantFoodLine(detail.plantFoodType());
        if (plantFood != null) {
            stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_PLANTFOOD, "PLANT FOOD POWER",
                            deltaValue(skin, formatNumber(detail.plantFoodValue()), null)))
                    .width(blockWidth).padBottom(padY).left();
        } else {
            stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_CATEGORY, "CATEGORY",
                            deltaValue(skin, CollectionCardLooks.words(detail.category()), null)))
                    .width(blockWidth).padBottom(padY).left();
        }
        stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_RANGE, "RANGE",
                        deltaValue(skin, CollectionCardLooks.rangeLabel(detail.category()), null)))
                .width(blockWidth).padBottom(padY).left().row();

        if (plantFood != null) {
            Table food = new Table();
            Image icon = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.ALMANAC_STAT_PLANTFOOD)));
            icon.setScaling(Scaling.fit);
            Label foodText = wrapped(skin, plantFood, BODY_WHITE, 0.62f);
            food.add(icon).size(42f, 42f).padRight(10f).top();
            food.add(foodText).growX().left();
            stats.add(food).colspan(2).growX().padTop(18f).row();
        }
        Label ability = wrapped(skin,
                CollectionCardLooks.plantAbilityLine(detail.name(), detail.category(), detail.abilityType()),
                BODY_WHITE, 0.68f);
        stats.add(ability).colspan(2).growX().padTop(16f).row();
        if (detail.hasNextLevel() && hasText(detail.nextUpgradeSummary())
                && !"Max level reached".equals(detail.nextUpgradeSummary())) {
            Label lore = wrapped(skin, detail.nextUpgradeSummary(), LORE_YELLOW, 0.62f);
            stats.add(lore).colspan(2).growX().padTop(14f).row();
        }
        return stats;
    }

    private Table zombieInfo(Skin skin, CollectionZombieDetail detail) {
        Table stats = new Table();
        stats.left().top();
        Label title = new Label(detail.displayName(), skin, titleStyle(skin));
        title.setColor(TITLE_WHITE);
        title.setAlignment(Align.left);
        stats.add(title).colspan(2).left().padBottom(22f).row();
        float blockWidth = 360f;
        stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_ZOMBIE_TOUGHNESS, "TOUGHNESS",
                        deltaValue(skin, detail.toughnessDisplay(), null)))
                .width(blockWidth).left();
        stats.add(statBlock(skin, MenuAssetIds.ALMANAC_STAT_ZOMBIE_SPEED, "SPEED",
                        deltaValue(skin, detail.speedDisplay(), null)))
                .width(blockWidth).left().row();
        Label summary = wrapped(skin, detail.displayName() + ".", BODY_WHITE, 0.68f);
        stats.add(summary).colspan(2).growX().padTop(28f).row();
        StringBuilder lore = new StringBuilder("Hitpoints " + detail.hitpoints() + ".");
        if (detail.hasArmor() && detail.armorAliases() != null && !detail.armorAliases().isEmpty()) {
            lore.append(" Armor: ").append(String.join(", ", detail.armorAliases())).append('.');
        }
        Label flavor = wrapped(skin, lore.toString(), LORE_YELLOW, 0.62f);
        stats.add(flavor).colspan(2).growX().padTop(18f).row();
        return stats;
    }

    private Table statBlock(Skin skin, String iconId, String caption, Actor value) {
        Table block = new Table();
        block.left();
        Stack iconStack = new Stack();
        Image frame = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.ALMANAC_STAT_FRAME)));
        frame.setScaling(Scaling.stretch);
        frame.setTouchable(Touchable.disabled);
        Image icon = new Image(new TextureRegionDrawable(assets.region(iconId)));
        icon.setScaling(Scaling.fit);
        icon.setTouchable(Touchable.disabled);
        Table iconPad = new Table();
        iconPad.add(icon).grow().pad(8f);
        iconStack.add(frame);
        iconStack.add(iconPad);
        Table text = new Table();
        text.left();
        Label cap = new Label(caption, skin, outlineStyle(skin));
        cap.setFontScale(0.48f);
        cap.setColor(TITLE_WHITE);
        text.add(cap).left().row();
        text.add(value).left().padTop(2f);
        block.add(iconStack).size(64f, 64f).padRight(10f).left();
        block.add(text).expandX().left();
        return block;
    }

    private Actor deltaValue(Skin skin, String current, String next) {
        if (next == null || next.equals(current)) {
            Label value = new Label(current == null ? "" : current, skin, outlineStyle(skin));
            value.setFontScale(0.82f);
            value.setColor(TITLE_WHITE);
            return value;
        }
        Table row = new Table();
        Label now = new Label(current, skin, outlineStyle(skin));
        now.setFontScale(0.82f);
        now.setColor(TITLE_WHITE);
        Label arrow = new Label(" > ", skin, outlineStyle(skin));
        arrow.setFontScale(0.82f);
        arrow.setColor(UPGRADE_GREEN);
        Label later = new Label(next, skin, outlineStyle(skin));
        later.setFontScale(0.82f);
        later.setColor(UPGRADE_GREEN);
        row.add(now);
        row.add(arrow);
        row.add(later);
        return row;
    }

    private Label wrapped(Skin skin, String text, Color color, float scale) {
        Label label = new Label(text == null ? "" : text, skin, outlineStyle(skin));
        label.setWrap(true);
        label.setAlignment(Align.left);
        label.setFontScale(scale);
        label.setColor(color);
        return label;
    }

    private Table detailNav(boolean plants) {
        Table nav = new Table();
        Actor prev = PvzButtons.iconButton(assets.region(MenuAssetIds.ALMANAC_NAV_PREV), 72f, 72f, () -> cycle(-1, plants));
        Actor next = PvzButtons.iconButton(assets.region(MenuAssetIds.ALMANAC_NAV_NEXT), 72f, 72f, () -> cycle(1, plants));
        nav.add(prev).size(72f).left();
        nav.add().expandX();
        nav.add(next).size(72f).right();
        return nav;
    }

    private void cycle(int delta, boolean plants) {
        if (controller == null) {
            return;
        }
        if (plants) {
            List<CollectionPlantEntry> entries = controller.plants(query);
            if (entries.isEmpty()) {
                return;
            }
            int index = indexOfPlant(entries, selectedPlant);
            CollectionPlantEntry next = entries.get(Math.floorMod(index + delta, entries.size()));
            openPlant(next.name());
            return;
        }
        List<CollectionZombieEntry> seen = controller.zombies().stream().filter(CollectionZombieEntry::seen).toList();
        if (seen.isEmpty()) {
            return;
        }
        int index = indexOfZombie(seen, selectedZombie);
        openZombie(seen.get(Math.floorMod(index + delta, seen.size())));
    }

    private static int indexOfPlant(List<CollectionPlantEntry> entries, String name) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).name().equals(name)) {
                return i;
            }
        }
        return 0;
    }

    private static int indexOfZombie(List<CollectionZombieEntry> entries, String alias) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).alias().equals(alias)) {
                return i;
            }
        }
        return 0;
    }

    private void showFilterModal() {
        if (activeTab != Tab.PLANTS) {
            return;
        }
        modalLayer.clearChildren();
        Skin skin = assets.skin();
        ModalPanel panel = new ModalPanel(skin, "Filter plants");
        SelectBox<String> families = ThemedSelectBox.create(skin);
        Array<String> items = new Array<>();
        items.add(ALL_FAMILIES);
        for (String family : controller.plantFamilies()) {
            items.add(family);
        }
        families.setItems(items);
        if (query.family() != null && !query.family().isBlank()) {
            families.setSelected(query.family());
        } else {
            families.setSelected(ALL_FAMILIES);
        }
        families.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = families.getSelected();
                query = query.withFamily(ALL_FAMILIES.equals(selected) ? null : selected);
                refreshGrid();
            }
        });
        panel.content().add(families).width(360f).height(48f).padBottom(16f).row();
        Table buttons = new Table();
        buttons.defaults().pad(6f).width(160f).height(44f);
        buttons.add(filterButton(skin, "All", CollectionPlantFilter.ALL, panel));
        buttons.add(filterButton(skin, "Owned", CollectionPlantFilter.OWNED, panel));
        buttons.row();
        buttons.add(filterButton(skin, "Locked", CollectionPlantFilter.LOCKED, panel));
        buttons.add(filterButton(skin, "Upgradeable", CollectionPlantFilter.UPGRADEABLE, panel));
        panel.content().add(buttons);
        panel.show(modalLayer, viewport);
        panel.addCloseButton(skin);
    }

    private TextButton filterButton(Skin skin, String label, CollectionPlantFilter filter, ModalPanel panel) {
        TextButton button = PvzButtons.textButton(label, skin, "green_small", () -> {
            query = query.withFilter(filter);
            panel.dismiss();
            refreshGrid();
        });
        return button;
    }

    private String filterCaption() {
        CollectionPlantFilter filter = query.filter() == null ? CollectionPlantFilter.ALL : query.filter();
        String family = query.family() == null || query.family().isBlank() ? null : query.family();
        String status = switch (filter) {
            case ALL -> family == null ? "Show All Plants" : family;
            case OWNED -> "Owned plants";
            case LOCKED -> "Locked plants";
            case UPGRADEABLE -> "Upgradeable plants";
        };
        if (family != null && filter != CollectionPlantFilter.ALL) {
            return status + " / " + family;
        }
        return status;
    }

    private void onBack() {
        if (mode != Mode.GRID) {
            selectedPlant = null;
            selectedZombie = null;
            showGrid();
            return;
        }
        leaveCollection();
    }

    private void leaveCollection() {
        if (controller != null) {
            controller.back();
        }
    }

    private static String titleStyle(Skin skin) {
        return skin.has("big_outline", Label.LabelStyle.class) ? "big_outline" : "big";
    }

    private static String outlineStyle(Skin skin) {
        return skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
    }

    private static String formatNumber(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
