package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.ShopController;
import io.github.finalwave.model.shop.ShopOffer;
import io.github.finalwave.model.shop.ShopTab;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PriceButton;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.ShopItemCard;
import io.github.finalwave.view.gui.widget.StoreChrome;

import java.util.List;


public final class ShopScreen extends MenuScreen {
    private static final float CLOSE_HEIGHT = 56f;
    private static final float TAB_WIDTH = 124f;
    private static final float TAB_IDLE_HEIGHT = 100f;
    private static final float TAB_CHEVRON_HEIGHT = 24f;
    private static final float TAB_ACTIVE_HEIGHT = TAB_IDLE_HEIGHT + TAB_CHEVRON_HEIGHT;
    private static final float TAB_OVERLAP = TAB_CHEVRON_HEIGHT;
    private static final float TAB_ICON_ACTIVE = 62f;
    private static final float TAB_ICON_IDLE = 52f;
    private static final int TAB_BODY_SRC = 35;
    private static final int TAB_CHEVRON_INSET = 26;
    private static final int TAB_CHEVRON_SRC = 18;
    private static final float CARD_PAD = 14f;
    private static final float PICKER_CARD_WIDTH = 176f;
    private static final float PICKER_CARD_HEIGHT = 200f;
    private static final Color TITLE_GOLD_DARK = Color.valueOf("C89628");
    private static final Color TITLE_GOLD_RICH = Color.valueOf("FFC53A");
    private static final Color PICKER_NAME_HOVER = Color.valueOf("FFD24A");
    private static final Color BODY_WHITE = Color.valueOf("FFF8E7");
    private static final Color IDLE_TAB_ICON = new Color(1f, 1f, 1f, 0.78f);

    private ShopController controller;
    private PlantAnimationCatalog catalog;
    private Table idleTabHost;
    private Table activeTabHost;
    private Table cardGrid;
    private ShopTab activeTab = ShopTab.SEEDS;

    public ShopScreen(PvzGame game) {
        super(game);
    }

    public void bind(ShopController controller) {
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
        contentLayer.setTouchable(Touchable.childrenOnly);
        contentLayer.pad(100f, 36f, 24f, 36f);
        if (catalog == null) {
            catalog = new PlantAnimationCatalog(assets.root());
        }
        if (controller != null) {
            bindCurrency(controller.getUser());
        }

        hudLayer.clearChildren();
        hudLayer.top().left();
        hudLayer.padTop(10f).padLeft(16f).padRight(24f).padBottom(0f);
        hudLayer.add().expandX();
        hudLayer.add(currencyBar).right().padTop(14f);

        Stack shopRoot = new Stack();
        shopRoot.setTouchable(Touchable.childrenOnly);
        Table idleLayer = new Table();
        idleLayer.setTouchable(Touchable.childrenOnly);
        idleLayer.top().left().padLeft(18f);
        idleTabHost = new Table();
        idleLayer.add(idleTabHost).left().top();

        Table panelLayer = new Table();
        panelLayer.setTouchable(Touchable.childrenOnly);
        panelLayer.top().padTop(TAB_IDLE_HEIGHT - TAB_OVERLAP);
        panelLayer.add(storeWindow()).grow();

        Table frontLayer = new Table();
        frontLayer.setTouchable(Touchable.childrenOnly);
        frontLayer.top().padLeft(18f);
        activeTabHost = new Table();
        frontLayer.add(activeTabHost).left().top();
        frontLayer.add().expandX();

        shopRoot.add(idleLayer);
        shopRoot.add(panelLayer);
        shopRoot.add(frontLayer);
        shopRoot.add(closeOverlay());
        contentLayer.add(shopRoot).grow();
        rebuildTabs();
        refreshOffers();
    }

    private Table storeWindow() {
        Table frame = new Table();
        frame.setBackground(StoreChrome.panel());
        frame.setTouchable(Touchable.childrenOnly);
        frame.pad(
                StoreChrome.PANEL_PAD_TOP,
                StoreChrome.PANEL_PAD_LEFT,
                StoreChrome.PANEL_PAD_BOTTOM,
                StoreChrome.PANEL_PAD_RIGHT);

        cardGrid = new Table();
        cardGrid.top().left();
        cardGrid.setTouchable(Touchable.childrenOnly);
        ScrollPane scroll = new ScrollPane(cardGrid, assets.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        scroll.setFlickScroll(false);
        scroll.setCancelTouchFocus(false);
        frame.add(scroll).grow();

        Table wrap = new Table();
        wrap.setTouchable(Touchable.childrenOnly);
        wrap.add(frame).grow();
        return wrap;
    }

    private Table closeOverlay() {
        TextureRegion region = assets.region(MenuAssetIds.STORE_CLOSE);
        float closeWidth = CLOSE_HEIGHT * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
        Actor close = PvzButtons.iconButton(region, closeWidth, CLOSE_HEIGHT, this::goBack);
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

    public void refreshOffers() {
        if (cardGrid == null || controller == null) {
            return;
        }
        modalLayer.clearChildren();
        cardGrid.clearChildren();
        List<ShopOffer> offers = controller.offers(activeTab);
        int columns = columnsFor(activeTab);
        cardGrid.top().left();
        int column = 0;
        for (ShopOffer offer : offers) {
            ShopItemCard card = new ShopItemCard(assets, catalog, assets.skin());
            card.bind(offer, () -> onBuyClicked(offer));
            cardGrid.add(card).size(ShopItemCard.CARD_WIDTH, ShopItemCard.CARD_HEIGHT).pad(CARD_PAD);
            column++;
            if (column % columns == 0) {
                cardGrid.row();
            }
        }
        bindCurrency(controller.getUser());
    }

    private void rebuildTabs() {
        if (idleTabHost == null || activeTabHost == null) {
            return;
        }
        idleTabHost.clearChildren();
        activeTabHost.clearChildren();
        idleTabHost.defaults().size(TAB_WIDTH, TAB_IDLE_HEIGHT).padRight(8f).top();
        activeTabHost.defaults().size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).padRight(8f).top();
        addTab(ShopTab.SEEDS, MenuAssetIds.STORE_TAB_SEEDS_ACTIVE, MenuAssetIds.STORE_TAB_SEEDS_IDLE,
                MenuAssetIds.STORE_TAB_ICON_SEEDS);
        addTab(ShopTab.COINS, MenuAssetIds.STORE_TAB_COINS_ACTIVE, MenuAssetIds.STORE_TAB_COINS_IDLE,
                MenuAssetIds.STORE_TAB_ICON_COINS);
        addTab(ShopTab.GARDEN, MenuAssetIds.STORE_TAB_GARDEN_ACTIVE, MenuAssetIds.STORE_TAB_GARDEN_IDLE,
                MenuAssetIds.STORE_TAB_ICON_GARDEN);
    }

    private void addTab(ShopTab tab, String activeId, String idleId, String iconId) {
        boolean active = activeTab == tab;
        if (active) {
            idleTabHost.add().size(TAB_WIDTH, TAB_IDLE_HEIGHT).padRight(8f);
            activeTabHost.add(tabButton(tab, activeId, iconId, true));
        } else {
            idleTabHost.add(tabButton(tab, idleId, iconId, false));
            activeTabHost.add().size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).padRight(8f);
        }
    }

    private Actor tabButton(ShopTab tab, String plateId, String iconId, boolean active) {
        float height = active ? TAB_ACTIVE_HEIGHT : TAB_IDLE_HEIGHT;
        Actor plate = tabPlate(assets.region(plateId), active);

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
                .padBottom(active ? TAB_CHEVRON_HEIGHT : 14f);

        Stack stack = new Stack();
        stack.setSize(TAB_WIDTH, height);
        stack.setTouchable(Touchable.enabled);
        stack.add(plate);
        stack.add(iconHost);
        PvzButtons.animate(stack, active ? 1.02f : 1.08f, 0.94f, () -> selectTab(tab));
        return stack;
    }

    private static Actor tabPlate(TextureRegion region, boolean active) {
        int srcW = region.getRegionWidth();
        int srcH = region.getRegionHeight();
        int bodySrcH = Math.min(srcH, TAB_BODY_SRC);
        TextureRegion bodyRegion = new TextureRegion(region, 0, 0, srcW, bodySrcH);
        Image body = new Image(StoreChrome.tabBody(bodyRegion));
        body.setTouchable(Touchable.disabled);

        if (!active || srcW <= TAB_CHEVRON_INSET * 2 || srcH <= bodySrcH) {
            Table plate = new Table();
            plate.setFillParent(true);
            plate.setTouchable(Touchable.disabled);
            plate.add(body).grow();
            return plate;
        }
        int chevronH = Math.min(TAB_CHEVRON_SRC, srcH - bodySrcH);
        int chevronW = srcW - TAB_CHEVRON_INSET * 2;
        Image chevron = new Image(new TextureRegionDrawable(
                new TextureRegion(region, TAB_CHEVRON_INSET, bodySrcH, chevronW, chevronH)));
        chevron.setScaling(Scaling.stretch);
        chevron.setTouchable(Touchable.disabled);
        Table plate = new Table();
        plate.setFillParent(true);
        plate.setTouchable(Touchable.disabled);
        plate.add(body).grow();
        plate.row();
        plate.add(chevron).growX().height(TAB_CHEVRON_HEIGHT).padTop(-3f);
        return plate;
    }

    private void selectTab(ShopTab tab) {
        if (activeTab == tab) {
            return;
        }
        activeTab = tab;
        rebuildTabs();
        refreshOffers();
    }

    private static int columnsFor(ShopTab tab) {
        return switch (tab) {
            case COINS -> 4;
            case SEEDS, GARDEN -> 3;
        };
    }

    private void onBuyClicked(ShopOffer offer) {
        if (offer.requiresPlantType()) {
            showPlantPicker(offer);
            return;
        }
        showConfirm(offer, null);
    }

    private void showPlantPicker(ShopOffer offer) {
        modalLayer.clearChildren();
        Skin skin = assets.skin();
        ModalPanel panel = new ModalPanel(skin, null);
        panel.content().add(goldTitle(skin, "Choose a plant", TITLE_GOLD_RICH)).padBottom(14f).row();

        Table grid = new Table();
        grid.defaults().pad(8f);
        List<String> plants = controller.unlockedPlantNames();
        int column = 0;
        for (String plant : plants) {
            Actor plantCard = plantPickerCard(skin, plant, () -> {
                panel.dismiss();
                showConfirm(offer, plant);
            });
            grid.add(plantCard).size(PICKER_CARD_WIDTH, PICKER_CARD_HEIGHT);
            column++;
            if (column % 4 == 0) {
                grid.row();
            }
        }
        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        panel.content().add(scroll).width(820f).height(430f).padBottom(8f).row();
        panel.show(modalLayer, viewport);
        panel.addCloseButton(skin);
    }

    private Actor plantPickerCard(Skin skin, String plant, Runnable onPick) {
        Table card = new Table();
        Image glow = new Image(StoreChrome.previewGlow(assets.region(MenuAssetIds.STORE_PREVIEW_BOX)));
        glow.setScaling(Scaling.fit);
        Stack stack = new Stack();
        stack.add(glow);
        Table inner = new Table();
        Image portrait = new Image(new TextureRegionDrawable(
                assets.region(ShopItemCard.packetImageId(assets, plant))));
        portrait.setScaling(Scaling.fit);
        Label name = new Label(plant, skin, outlineStyle(skin));
        name.setAlignment(Align.center);
        name.setColor(BODY_WHITE);
        name.setWrap(true);
        name.setFontScale(0.58f);
        inner.top().padTop(18f);
        inner.add(portrait).size(112f).row();
        inner.add(name).width(PICKER_CARD_WIDTH - 20f).padTop(4f);
        stack.add(inner);
        card.add(stack).grow();
        PvzButtons.animate(card, 1.07f, 0.93f, onPick);
        card.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    name.setColor(PICKER_NAME_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    name.setColor(BODY_WHITE);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                name.setColor(PICKER_NAME_HOVER);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        return card;
    }

    private void showConfirm(ShopOffer offer, String plantType) {
        modalLayer.clearChildren();
        Skin skin = assets.skin();
        ModalPanel panel = new ModalPanel(skin, null);
        panel.content().add(goldTitle(skin, "Purchase Confirmation", TITLE_GOLD_DARK)).padBottom(18f).row();
        String question = plantType == null || plantType.isBlank()
                ? "Would you like to purchase " + offer.name() + "?"
                : "Would you like to purchase " + offer.name() + " for " + plantType + "?";
        Label body = new Label(question, skin, outlineStyle(skin));
        body.setWrap(true);
        body.setAlignment(Align.center);
        body.setColor(BODY_WHITE);
        panel.content().add(body).width(560f).padBottom(22f).row();
        Actor confirm = PriceButton.of(assets, skin, offer, 200f, 58f, () -> {
            panel.dismiss();
            if (controller != null) {
                controller.buy(offer.id(), offer.purchaseCount(), plantType);
            }
        });
        panel.content().add(confirm).size(200f, 58f);
        panel.show(modalLayer, viewport);
        panel.addCloseButton(skin);
    }

    private Label goldTitle(Skin skin, String text, Color color) {
        Label title = new Label(text, skin, "big");
        title.setColor(color);
        title.setAlignment(Align.center);
        return title;
    }

    private static String outlineStyle(Skin skin) {
        return skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
    }

    private void goBack() {
        if (controller != null) {
            controller.back();
        }
    }
}
