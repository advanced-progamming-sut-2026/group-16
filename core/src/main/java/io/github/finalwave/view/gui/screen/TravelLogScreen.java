package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.TravelLogController;
import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.MinigameLogCard;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.QuestRow;
import io.github.finalwave.view.gui.widget.StoreChrome;
import io.github.finalwave.view.gui.widget.StoreTopTabs;
import io.github.finalwave.view.gui.widget.TravelLogChrome;
import io.github.finalwave.view.gui.widget.TravelLogTab;

import java.util.List;

public final class TravelLogScreen extends MenuScreen {
    private static final float MODAL_WIDTH = 1580f;
    private static final float MODAL_HEIGHT = 880f;
    private static final float CLOSE_HEIGHT = 56f;
    private static final float LIST_PAD = 7f;
    private static final float MASCOT_SIZE = 128f;

    private TravelLogController controller;
    private TravelLogTab activeTab = TravelLogTab.DAILY;
    private Table headerHost;
    private Table listHost;
    private Table scrollFrame;
    private Table mascotLayer;

    public TravelLogScreen(PvzGame game) {
        super(game);
    }

    public void bind(TravelLogController controller) {
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
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
        buildHud();

        Stack root = new Stack();
        root.setFillParent(true);
        Table dim = new Table();
        dim.setBackground(TravelLogChrome.dimBackdrop());
        root.add(dim);

        Table center = new Table();
        center.add(windowStack()).size(MODAL_WIDTH, MODAL_HEIGHT);
        root.add(center);
        contentLayer.add(root).grow();
        refresh();
    }

    public void refresh() {
        if (headerHost == null || listHost == null || mascotLayer == null || controller == null) {
            return;
        }
        bindCurrency(controller.getUser());
        rebuildHeader();
        rebuildList();
        rebuildMascot();
    }

    private void buildHud() {
        hudLayer.clearChildren();
        hudLayer.top().right();
        hudLayer.padTop(18f).padRight(24f);
        hudLayer.add(currencyBar);
    }

    private Stack windowStack() {
        Stack stack = new Stack();
        stack.setTouchable(Touchable.childrenOnly);

        Table panelLayer = new Table();
        panelLayer.setTouchable(Touchable.childrenOnly);
        panelLayer.top().padTop(StoreTopTabs.TAB_IDLE_HEIGHT - StoreTopTabs.TAB_OVERLAP);
        panelLayer.add(logWindow()).grow();
        stack.add(panelLayer);

        Table tabsLayer = new Table();
        tabsLayer.setTouchable(Touchable.childrenOnly);
        tabsLayer.top().left().padLeft(28f);
        tabsLayer.add(travelTabs()).left().top();
        stack.add(tabsLayer);

        mascotLayer = new Table();
        mascotLayer.setTouchable(Touchable.disabled);
        mascotLayer.top().left();
        stack.add(mascotLayer);
        stack.add(closeOverlay());
        return stack;
    }

    private StoreTopTabs<TravelLogTab> travelTabs() {
        return new StoreTopTabs<>(
                assets,
                assets.skin(),
                List.of(
                        questTab(TravelLogTab.DAILY, "Daily"),
                        questTab(TravelLogTab.MAIN, "Main"),
                        questTab(TravelLogTab.EPIC, "Epic"),
                        new StoreTopTabs.Tab<>(
                                TravelLogTab.MINIGAME,
                                "Minigames",
                                MenuAssetIds.STORE_TAB_GARDEN_ACTIVE,
                                MenuAssetIds.STORE_TAB_GARDEN_IDLE,
                                null)),
                activeTab,
                this::selectTab,
                0.94f);
    }

    private static StoreTopTabs.Tab<TravelLogTab> questTab(TravelLogTab tab, String label) {
        return new StoreTopTabs.Tab<>(
                tab,
                label,
                MenuAssetIds.STORE_TAB_SEEDS_ACTIVE,
                MenuAssetIds.STORE_TAB_SEEDS_IDLE,
                null);
    }

    private Table logWindow() {
        Table frame = new Table();
        frame.setBackground(StoreChrome.panel());
        frame.setTouchable(Touchable.childrenOnly);
        frame.pad(
                StoreChrome.PANEL_PAD_TOP + 8f,
                StoreChrome.PANEL_PAD_LEFT,
                StoreChrome.PANEL_PAD_BOTTOM,
                StoreChrome.PANEL_PAD_RIGHT);

        headerHost = new Table();
        headerHost.setTouchable(Touchable.childrenOnly);
        frame.add(headerHost).growX().padBottom(8f).row();

        listHost = new Table();
        listHost.top().left();
        listHost.setTouchable(Touchable.childrenOnly);
        ScrollPane scroll = new ScrollPane(listHost, assets.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        scroll.setFlickScroll(true);
        scroll.setCancelTouchFocus(false);

        scrollFrame = new Table();
        scrollFrame.setTouchable(Touchable.childrenOnly);
        scrollFrame.pad(8f);
        scrollFrame.add(scroll).grow();
        frame.add(scrollFrame).grow();
        return frame;
    }

    private Table closeOverlay() {
        TextureRegion region = assets.region(MenuAssetIds.STORE_CLOSE);
        float closeWidth = CLOSE_HEIGHT * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
        Actor close = PvzButtons.iconButton(region, closeWidth, CLOSE_HEIGHT, this::goBack);
        Table overlay = new Table();
        overlay.setTouchable(Touchable.childrenOnly);
        overlay.top().right();
        overlay.padTop(StoreTopTabs.TAB_IDLE_HEIGHT - StoreTopTabs.TAB_OVERLAP);
        overlay.add(close)
                .size(closeWidth, CLOSE_HEIGHT)
                .padTop(-CLOSE_HEIGHT + 12f)
                .padRight(22f);
        return overlay;
    }

    private void selectTab(TravelLogTab tab) {
        if (tab == null) {
            return;
        }
        activeTab = tab;
        refresh();
    }

    private void rebuildHeader() {
        headerHost.clearChildren();
        if (!activeTab.questTab()) {
            headerHost.add().height(8f).growX();
            return;
        }
        Skin skin = assets.skin();
        Label timer = new Label(controller.dailyRefreshLabel(), skin, "medium");
        timer.setColor(TravelLogChrome.TIMER_YELLOW);
        timer.setAlignment(Align.center);
        timer.setFontScale(0.82f);
        headerHost.add(timer).growX().padTop(3f).padBottom(7f).row();
        headerHost.add(claimBanner()).growX().height(68f);
    }

    private Table claimBanner() {
        int pending = controller.pendingClaimCount(activeTab.questCategory());
        Table banner = new Table();
        banner.setBackground(TravelLogChrome.claimBanner());
        banner.pad(8f, 16f, 8f, 12f);
        Label text = new Label(claimBannerText(pending), assets.skin(), "medium");
        text.setColor(TravelLogChrome.TITLE_BROWN);
        text.setWrap(true);
        text.setFontScale(0.78f);
        banner.add(text).growX().left();

        String style = assets.skin().has("green_small", TextButton.TextButtonStyle.class)
                ? "green_small"
                : "brown";
        TextButton claimAll = PvzButtons.textButton(
                "Claim All",
                assets.skin(),
                style,
                pending > 0 ? () -> controller.claimAll(activeTab.questCategory()) : null);
        claimAll.setDisabled(pending == 0);
        claimAll.setTouchable(pending > 0 ? Touchable.enabled : Touchable.disabled);
        banner.add(claimAll).size(170f, 50f).right().padLeft(16f);
        return banner;
    }

    private static String claimBannerText(int pending) {
        if (pending <= 0) {
            return "All completed quest rewards have been claimed.";
        }
        return "You have " + pending + " completed quest"
                + (pending == 1 ? "" : "s")
                + ". Claim all your rewards now!";
    }

    private void rebuildList() {
        listHost.clearChildren();
        listHost.top().left();
        if (activeTab.questTab()) {
            scrollFrame.setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
            for (Quest quest : controller.questsFor(activeTab.questCategory())) {
                QuestRow row = new QuestRow(
                        assets,
                        assets.skin(),
                        quest,
                        () -> controller.claimQuest(quest));
                listHost.add(row).growX().height(QuestRow.ROW_HEIGHT).pad(LIST_PAD).row();
            }
            return;
        }
        scrollFrame.setBackground(TravelLogChrome.minigamePanel());
        for (TravelLogController.MiniGameLogEntry entry : controller.minigameEntries()) {
            MinigameLogCard card = new MinigameLogCard(
                    assets,
                    assets.skin(),
                    entry,
                    () -> controller.playMinigame(entry.id()));
            listHost.add(card).growX().height(MinigameLogCard.CARD_HEIGHT).pad(LIST_PAD).row();
        }
    }

    private void rebuildMascot() {
        mascotLayer.clearChildren();
        if (activeTab != TravelLogTab.MINIGAME) {
            return;
        }
        Image mascot = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.QUESTS_ICON)));
        mascot.setScaling(Scaling.fit);
        mascotLayer.add(mascot)
                .size(MASCOT_SIZE)
                .padTop(64f)
                .padLeft(-26f);
    }

    private void goBack() {
        if (controller != null) {
            controller.back();
        }
    }
}
