package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.view.api.minigame.DuelPickController;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.assets.ZombieAnimationCatalog;
import io.github.finalwave.view.gui.assets.ZombieCardLooks;
import io.github.finalwave.view.gui.widget.PamActor;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PlantCardActor;
import io.github.finalwave.view.gui.widget.StoreChrome;
import io.github.finalwave.view.gui.widget.UpgradeBadges;
import io.github.finalwave.view.gui.widget.UpgradeSeedBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AdventurePickPanel extends Table {

    private static final float PANEL_WIDTH = 980f;
    private static final float PANEL_HEIGHT = 860f;
    private static final float CARD_WIDTH = 140f;
    private static final float CARD_HEIGHT = 105f;
    private static final float LOADOUT_WIDTH = 116f;
    private static final float LOADOUT_HEIGHT = 84f;
    private static final float PREVIEW_LANE_WIDTH = 260f;
    private static final float PREVIEW_SLOT_WIDTH = 170f;
    private static final float PREVIEW_SLOT_HEIGHT = 190f;
    private static final float PREVIEW_SCALE = 0.62f;
    private static final float PREVIEW_STAGGER = 56f;
    private static final float PREVIEW_FLOOR_PAD = 110f;
    private static final int PREVIEW_LIMIT = 4;
    private static final int GRID_COLUMNS = 5;
    private static final float FRAME_PAD_SIDE = 24f;
    private static final float FRAME_PAD_TOP = 22f;
    private static final float FRAME_PAD_BOTTOM = 20f;
    private static final float DETAIL_HEIGHT = 296f;
    private static final float FOOTER_HEIGHT = 52f;
    private static final float HEADER_HEIGHT = 58f;
    private static final float PREVIEW_SIZE = 190f;
    private static final float PREVIEW_PAM_SIZE = 110f;
    private static final float SEED_BAR_HEIGHT = 24f;
    private static final float BADGE_SIZE = 40f;
    private static final float DESCRIPTION_FONT_SCALE = 1.12f;
    private static final Color BODY_BROWN = Color.valueOf("4A3018");
    private static final Color DESCRIPTION_OLIVE = Color.valueOf("4C4520");
    private static final Color HEADER_CREAM = Color.valueOf("FFFBE6");
    private static final String DARK_PANEL = "image_ui_if_bundle_reward1_bg_10";
    private static final String GREEN_PLATE = "image_ui_generic_greenbutton_10";
    private static final String ROUNDED_CREAM_PANEL = "image_ui_dialog_asset_inner_bkgd_10";

    private final GameAssets assets;
    private final DuelPickController controller;
    private final AdventurePickBindings bindings;
    private final boolean zombieRole;
    private final PlantAnimationCatalog plantCatalog;
    private final ZombieAnimationCatalog zombieCatalog;
    private final List<PlantCardActor> poolCards = new ArrayList<>();
    private final Table loadoutColumn;
    private final Table detailHost;
    private final Table grid;
    private final Table previewLane;
    private Label timerLabel;
    private Label selectedLabel;
    private String focusedName;
    private List<String> lastPicks = List.of();

    public AdventurePickPanel(GameAssets assets, DuelPickController controller, AdventurePickBindings bindings) {
        this.assets = assets;
        this.controller = controller;
        this.bindings = bindings == null ? new AdventurePickBindings() {
        } : bindings;
        this.zombieRole = controller.zombieSide();
        this.plantCatalog = new PlantAnimationCatalog(assets.root());
        this.zombieCatalog = new ZombieAnimationCatalog(assets.root());

        Skin skin = assets.skin();
        loadoutColumn = new Table(skin);
        loadoutColumn.top();
        detailHost = new Table(skin);
        grid = new Table(skin);
        previewLane = new Table(skin);
        previewLane.bottom().right();

        add(loadoutColumn).width(LOADOUT_WIDTH + 12f).top().left().padLeft(18f).padTop(18f);
        add(buildChooser(skin)).size(PANEL_WIDTH, PANEL_HEIGHT).expand().center().pad(12f);
        add(previewLane).width(PREVIEW_LANE_WIDTH).bottom().padBottom(PREVIEW_FLOOR_PAD).padRight(24f);

        List<String> pool = controller.pickPool();
        if (!pool.isEmpty()) {
            focusedName = pool.getFirst();
        }
        rebuildPreviewLane();
        rebuildGrid();
        rebuildLoadout();
        rebuildDetail();
        updateChrome();
    }

    public void updateChrome() {
        if (timerLabel != null) {
            timerLabel.setText("Pick time left: " + controller.pickSecondsLeft() + "s");
        }
        if (selectedLabel != null) {
            selectedLabel.setText("Selected " + controller.localPicks().size() + " / " + controller.pickSlots());
        }
        List<String> picks = controller.localPicks();
        if (!picks.equals(lastPicks)) {
            lastPicks = List.copyOf(picks);
            syncPoolSelection();
            rebuildLoadout();
        }
    }

    private Table buildChooser(Skin skin) {
        Table chooser = new Table();
        chooser.top().pad(FRAME_PAD_TOP, FRAME_PAD_SIDE, FRAME_PAD_BOTTOM, FRAME_PAD_SIDE);
        chooser.setBackground(StoreChrome.panel());
        chooser.add(detailHost).growX().height(DETAIL_HEIGHT).padBottom(12f).row();
        Table gridPanel = new Table();
        gridPanel.setBackground(gridBackground(skin));
        gridPanel.pad(14f, 10f, 14f, 10f);
        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        gridPanel.add(scroll).grow();
        chooser.add(gridPanel).grow().padBottom(8f).row();

        timerLabel = new Label("", skin, "secondary");
        timerLabel.setWrap(false);
        selectedLabel = new Label("", skin, "medium");
        selectedLabel.setWrap(false);
        selectedLabel.setColor(PanelLabels.panelText(skin));
        Table status = new Table(skin);
        status.add(timerLabel).left().growX().row();
        status.add(selectedLabel).left().growX();
        chooser.add(status).growX().left().height(FOOTER_HEIGHT);

        return chooser;
    }

    private void rebuildDetail() {
        Skin skin = assets.skin();
        detailHost.clearChildren();
        Table panel = new Table();
        panel.setBackground(detailBackground(skin));
        panel.pad(14f, 18f, 16f, 18f);
        String header = focusedName == null || focusedName.isBlank()
                ? (zombieRole ? "Choose Your Zombies" : "Choose Your Plants")
                : focusedName;
        panel.add(nameHeader(skin, header)).growX().colspan(2).height(HEADER_HEIGHT).padBottom(12f).row();
        if (focusedName != null && !focusedName.isBlank()) {
            if (zombieRole) {
                panel.add(zombiePreview(focusedName)).size(PREVIEW_SIZE, PREVIEW_SIZE).left().padRight(18f);
                panel.add(zombieDetailActions(skin, focusedName)).grow().top();
            } else {
                CollectionPlantDetail detail = bindings.plantDetail(focusedName);
                if (detail != null) {
                    panel.add(plantPreview(detail)).size(PREVIEW_SIZE, PREVIEW_SIZE).left().padRight(18f);
                    panel.add(plantDetailActions(skin, detail)).grow().top();
                } else {
                    Label hint = PanelLabels.body(skin, "Tap a seed packet to add it to your loadout.");
                    hint.setAlignment(Align.center);
                    panel.add(hint).grow().colspan(2);
                }
            }
        } else {
            Label hint = PanelLabels.body(skin,
                    zombieRole ? "Tap a zombie to add it to your loadout." : "Tap a seed packet to add it to your loadout.");
            hint.setAlignment(Align.center);
            panel.add(hint).grow().colspan(2);
        }
        detailHost.add(panel).grow();
    }

    private Table plantPreview(CollectionPlantDetail detail) {
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
        overlay.add(plantPreviewSeedBar(detail)).growX().height(SEED_BAR_HEIGHT);

        Stack stack = new Stack();
        stack.add(card);
        stack.add(overlay);
        Table wrap = new Table();
        wrap.add(stack).grow();
        return wrap;
    }

    private Actor plantPreviewSeedBar(CollectionPlantDetail detail) {
        int needed = Math.max(1, detail.seedPacketsNeeded());
        float value = detail.maxLevel() ? 1f : Math.min(1f, detail.seedPackets() / (float) needed);
        String text = detail.maxLevel() ? "MAX" : detail.seedPackets() + " / " + needed;
        boolean selectable = bindings.plantSelectable(detail.name());
        UpgradeSeedBar bar = new UpgradeSeedBar(assets.skin());
        bar.bind(selectable ? value : 0f, selectable ? text : "LOCKED");
        return bar;
    }

    private Table plantDetailActions(Skin skin, CollectionPlantDetail detail) {
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
        int cost = bindings.plantCost(detail.name());
        if (cost <= 0) {
            cost = detail.cost();
        }
        Label stats = new Label(
                "Cost: " + cost + " | Recharge: " + formatRecharge(detail.recharge()),
                skin,
                "medium");
        stats.setColor(BODY_BROWN);
        stats.setFontScale(0.9f);
        stats.setAlignment(Align.topLeft);
        column.add(stats).growX().top();
        return column;
    }

    private Table zombiePreview(String alias) {
        PamActor pam = new PamActor(assets.pamPlayer());
        EntityAnimationCatalog entityCatalog = assets.entityAnims();
        EntityAnimationCatalog.ClipSpec clip = entityCatalog.zombieClip(alias, "idle", "walk");
        pam.setClip(new PlantAnimationCatalog.ClipSpec(clip.path(), clip.clip()), PREVIEW_SCALE);
        Map<String, Boolean> armorLeaves = ZombieCardLooks.intactArmorLeaves(
                assets.pamPlayer(),
                entityCatalog,
                alias,
                ZombieCardLooks.armorAliasesFor(alias));
        if (armorLeaves != null && !armorLeaves.isEmpty()) {
            pam.setVisibility(armorLeaves);
        }
        pam.setTouchable(Touchable.disabled);
        Table wrap = new Table();
        wrap.add(pam).size(PREVIEW_PAM_SIZE, PREVIEW_PAM_SIZE).center();
        return wrap;
    }

    private Table zombieDetailActions(Skin skin, String alias) {
        Table column = new Table();
        column.top();
        Label description = PanelLabels.body(skin, "Deploy this zombie past the red line.");
        description.setColor(DESCRIPTION_OLIVE);
        description.setWrap(true);
        description.setFontScale(DESCRIPTION_FONT_SCALE);
        description.setAlignment(Align.topLeft);
        column.add(description).growX().top().padBottom(10f).row();
        int cost = IZombieDuelCatalog.costOf(alias);
        int recharge = IZombieDuelCatalog.rechargeSeconds(alias);
        Label stats = new Label("Cost: " + cost + " | Recharge: " + recharge + "s", skin, "medium");
        stats.setColor(BODY_BROWN);
        stats.setFontScale(0.9f);
        stats.setAlignment(Align.topLeft);
        column.add(stats).growX().top();
        return column;
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
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            if (zombieRole) {
                card.setZombie(name);
                card.setCost(IZombieDuelCatalog.costOf(name));
            } else {
                CollectionPlantEntry entry = bindings.plantEntry(name);
                if (entry != null) {
                    card.bind(entry);
                }
                int cost = bindings.plantCost(name);
                card.setCost(cost);
                boolean selectable = bindings.plantSelectable(name);
                card.setLocked(!selectable);
                if (selectable && entry != null && !entry.owned()) {
                    card.setLevel(1);
                }
                card.setBoosted(bindings.plantBoosted(name));
            }
            card.setDisabled(picks.contains(name));
            card.setSelected(false);
            card.setOnClick(() -> onCardClicked(name));
            poolCards.add(card);
            grid.add(card).size(CARD_WIDTH, CARD_HEIGHT).pad(8f);
            col++;
            if (col % GRID_COLUMNS == 0) {
                grid.row();
            }
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
                    card.setLevel(0);
                    card.setFamily(null);
                    card.setBoosted(bindings.plantBoosted(name));
                    card.setCost(bindings.plantCost(name));
                }
                card.setOnClick(() -> onCardClicked(name));
            } else {
                card.setEmpty();
            }
            loadoutColumn.add(card).size(LOADOUT_WIDTH, LOADOUT_HEIGHT).pad(3f).row();
        }
    }

    private void rebuildPreviewLane() {
        previewLane.clearChildren();
        List<String> names = bindings.previewLaneNames();
        if (names.isEmpty()) {
            names = zombieRole ? IZombieDuelCatalog.DEFAULT_PLANTS : IZombieDuelCatalog.DEFAULT_ZOMBIES;
        }
        int shown = 0;
        for (String name : names) {
            if (shown >= PREVIEW_LIMIT) {
                break;
            }
            PamActor actor = new PamActor(assets.pamPlayer());
            if (zombieRole) {
                actor.setClip(plantCatalog.idleFor(name), PREVIEW_SCALE);
            } else {
                actor.setClip(zombieCatalog.plantClip(name), PREVIEW_SCALE);
            }
            actor.setTouchable(Touchable.disabled);
            float stagger = shown % 2 == 0 ? 0f : PREVIEW_STAGGER;
            previewLane.add(actor).size(PREVIEW_SLOT_WIDTH, PREVIEW_SLOT_HEIGHT).right().padRight(stagger).row();
            shown++;
        }
    }

    private void onCardClicked(String name) {
        focusedName = name;
        controller.togglePick(name);
        lastPicks = List.copyOf(controller.localPicks());
        syncPoolSelection();
        rebuildLoadout();
        rebuildDetail();
        updateChrome();
    }

    private void syncPoolSelection() {
        List<String> picks = controller.localPicks();
        for (PlantCardActor card : poolCards) {
            String cardName = card.plantName();
            card.setSelected(false);
            card.setDisabled(picks.contains(cardName));
        }
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

    private static String titleStyle(Skin skin) {
        if (skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        return "big";
    }

    private static String formatRecharge(double recharge) {
        if (recharge == (long) recharge) {
            return String.valueOf((long) recharge);
        }
        return String.format("%.1f", recharge);
    }
}
