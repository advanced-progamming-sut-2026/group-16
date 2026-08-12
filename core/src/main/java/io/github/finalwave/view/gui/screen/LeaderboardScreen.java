package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.LeaderboardController;
import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.model.leaderboard.LeaderboardSortColumn;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;

import java.util.List;

public final class LeaderboardScreen extends MenuScreen {
    private static final float SEP_WIDTH = 3f;
    private static final float COL_RANK = 52f;
    private static final float COL_USER = 170f;
    private static final float COL_PROGRESS = 190f;
    private static final float COL_MINI = 150f;
    private static final float COL_DAILY = 100f;
    private static final float COL_NONDAILY = 118f;
    private static final float COL_SCORE = 140f;
    private static final int SEP_COUNT = 6;
    private static final float TABLE_WIDTH =
            COL_RANK + COL_USER + COL_PROGRESS + COL_MINI + COL_DAILY + COL_NONDAILY + COL_SCORE
                    + SEP_COUNT * SEP_WIDTH;
    private static final float ROW_HEIGHT = 56f;
    private static final float HEADER_HEIGHT = 56f;
    private static final float SHEET_HEIGHT = 520f;
    private static final float SCROLL_GUTTER = 36f;
    private static final float MARKER_SIZE = 16f;

    private static final Color HEADER_TEXT = Color.valueOf("FFF8E7");
    private static final Color CELL_TEXT = Color.valueOf("FFF8E7");
    private static final Color STATUS_TEXT = Color.valueOf("4A3018");
    private static final Color ROW_EVEN = new Color(0.42f, 0.28f, 0.16f, 0.95f);
    private static final Color ROW_ODD = new Color(0.32f, 0.20f, 0.10f, 0.95f);
    private static final Color ROW_ACTIVE = new Color(0.55f, 0.38f, 0.72f, 0.75f);

    private LeaderboardController controller;
    private Label sortStatus;
    private Table sheetBody;
    private Table headerRow;
    private LeaderboardSortColumn activeColumn = LeaderboardSortColumn.USERNAME;
    private boolean ascending = true;
    private List<LeaderboardEntry> pendingEntries;
    private Drawable sheetBg;
    private Drawable headerBg;
    private Drawable headerActiveBg;
    private Drawable hLine;
    private Drawable vLine;
    private Drawable rowEvenBg;
    private Drawable rowOddBg;
    private Drawable arrowUpDrawable;
    private Drawable arrowDownDrawable;

    public LeaderboardScreen(PvzGame game) {
        super(game);
    }

    public void bind(LeaderboardController controller) {
        this.controller = controller;
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();

        Skin skin = assets.skin();
        resolveTheme(skin);

        BorderedTable panel = new BorderedTable();
        panel.pad(40, 48, 40, 48);

        Label title = PanelLabels.title(skin, "Leaderboard");
        title.setFontScale(1.45f);
        panel.add(title).left().padBottom(6).row();

        sortStatus = new Label("Sorted by username (asc)", skin, "medium");
        sortStatus.setColor(STATUS_TEXT);
        sortStatus.setAlignment(Align.left);
        sortStatus.setWrap(false);
        panel.add(sortStatus).left().padBottom(14).row();

        Table actions = new Table();
        actions.add(sortOrderButton(skin, "Asc", "image_ui_generic_arrow_up_green", () -> {
            if (controller != null && activeColumn != null) {
                controller.sort(activeColumn.getKey(), "asc");
            }
        })).width(150).height(52).padRight(10);
        actions.add(sortOrderButton(skin, "Desc", "image_ui_generic_arrow_down_orange", () -> {
            if (controller != null && activeColumn != null) {
                controller.sort(activeColumn.getKey(), "desc");
            }
        })).width(160).height(52).padRight(10);
        actions.add(PvzButtons.textButton("Refresh", skin, "purple", () -> {
            if (controller != null) {
                controller.refresh();
            }
        })).width(160).height(50).padRight(10);
        actions.add(PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        })).width(140).height(50);
        panel.add(actions).left().padBottom(16).row();

        float sheetOuterWidth = TABLE_WIDTH + SCROLL_GUTTER;
        Table sheet = new Table();
        if (sheetBg != null) {
            sheet.setBackground(sheetBg);
        }
        sheet.pad(12);

        headerRow = new Table();
        headerRow.defaults().pad(0);
        rebuildHeader(skin);
        Table headerLine = new Table();
        headerLine.add(headerRow).width(TABLE_WIDTH).left();
        headerLine.add().width(SCROLL_GUTTER);
        sheet.add(headerLine).width(sheetOuterWidth).height(HEADER_HEIGHT).row();
        Actor hRule = lineHorizontal();
        if (hRule != null) {
            Table ruleLine = new Table();
            ruleLine.add(hRule).width(TABLE_WIDTH).height(3).left();
            ruleLine.add().width(SCROLL_GUTTER);
            sheet.add(ruleLine).width(sheetOuterWidth).row();
        }

        sheetBody = new Table();
        sheetBody.top().left();
        sheetBody.defaults().pad(0);
        Table scrollContent = new Table();
        scrollContent.add(sheetBody).width(TABLE_WIDTH).left().top();
        scrollContent.add().width(SCROLL_GUTTER).growY();
        ScrollPane scroll = new ScrollPane(scrollContent, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setScrollBarPositions(false, true);
        sheet.add(scroll).width(sheetOuterWidth).height(SHEET_HEIGHT);

        panel.add(sheet).width(sheetOuterWidth + 24).row();
        contentLayer.add(panel).center();

        if (pendingEntries != null) {
            showLeaderboard(pendingEntries, activeColumn, ascending);
        } else if (controller != null) {
            controller.refresh();
        } else {
            showPlaceholder("(no players)");
        }
    }

    public void showLeaderboard(
            List<LeaderboardEntry> entries,
            LeaderboardSortColumn column,
            boolean ascending) {
        this.activeColumn = column == null ? LeaderboardSortColumn.USERNAME : column;
        this.ascending = ascending;
        this.pendingEntries = entries;
        if (sortStatus != null) {
            sortStatus.setText("Sorted by " + this.activeColumn.getKey() + " (" + (ascending ? "asc" : "desc") + ")");
        }
        updateHeaderAppearance();
        if (sheetBody == null) {
            return;
        }
        sheetBody.clearChildren();
        Skin skin = assets.skin();
        if (entries == null || entries.isEmpty()) {
            showPlaceholder("(no players)");
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            Table row = buildDataRow(skin, i + 1, entry, i % 2 == 0);
            sheetBody.add(row).width(TABLE_WIDTH).height(ROW_HEIGHT).left().row();
            Actor rule = lineHorizontal();
            if (rule != null) {
                sheetBody.add(rule).width(TABLE_WIDTH).height(2).row();
            }
        }
    }

    private void rebuildHeader(Skin skin) {
        headerRow.clearChildren();
        addColumn(headerRow, headerCell(skin, "#", null, COL_RANK, false), COL_RANK, HEADER_HEIGHT, false);
        addColumn(headerRow, headerCell(skin, "Username", LeaderboardSortColumn.USERNAME, COL_USER, true), COL_USER, HEADER_HEIGHT, true);
        addColumn(headerRow, headerCell(skin, "Progress", LeaderboardSortColumn.PROGRESS, COL_PROGRESS, true), COL_PROGRESS, HEADER_HEIGHT, true);
        addColumn(headerRow, headerCell(skin, "Minigames", LeaderboardSortColumn.MINIGAMES, COL_MINI, true), COL_MINI, HEADER_HEIGHT, true);
        addColumn(headerRow, headerCell(skin, "Daily", LeaderboardSortColumn.DAILY_QUESTS, COL_DAILY, true), COL_DAILY, HEADER_HEIGHT, true);
        addColumn(headerRow, headerCell(skin, "NonDaily", LeaderboardSortColumn.NON_DAILY_QUESTS, COL_NONDAILY, true), COL_NONDAILY, HEADER_HEIGHT, true);
        addColumn(headerRow, headerCell(skin, "BestScore", LeaderboardSortColumn.BEST_SCORE, COL_SCORE, true), COL_SCORE, HEADER_HEIGHT, false);
    }

    private void addColumn(Table row, Actor cell, float width, float height, boolean withSepAfter) {
        row.add(cell).width(width).height(height).growY();
        if (withSepAfter) {
            row.add(vSep(height)).width(SEP_WIDTH).height(height);
        }
    }

    private void updateHeaderAppearance() {
        if (headerRow == null) {
            return;
        }
        for (Actor child : headerRow.getChildren()) {
            if (!(child instanceof Table cell)) {
                continue;
            }
            Object raw = cell.getUserObject();
            if (!(raw instanceof LeaderboardSortColumn column)) {
                continue;
            }
            boolean active = column == activeColumn;
            cell.setBackground(active ? headerActiveBg : headerBg);
            Label label = cell.findActor("headerLabel");
            if (label != null) {
                label.setText(columnLabel(column));
                fitHeaderLabel(label, columnWidth(column), active);
            }
            Actor marker = cell.findActor("sortMarker");
            if (marker != null) {
                marker.setVisible(active);
                if (marker instanceof Image image && active) {
                    Drawable arrow = ascending ? arrowUpDrawable : arrowDownDrawable;
                    if (arrow != null) {
                        image.setDrawable(arrow);
                    }
                }
            }
        }
    }

    private static float columnWidth(LeaderboardSortColumn column) {
        return switch (column) {
            case USERNAME -> COL_USER;
            case PROGRESS -> COL_PROGRESS;
            case MINIGAMES -> COL_MINI;
            case DAILY_QUESTS -> COL_DAILY;
            case NON_DAILY_QUESTS -> COL_NONDAILY;
            case BEST_SCORE -> COL_SCORE;
        };
    }

    private static String columnLabel(LeaderboardSortColumn column) {
        return switch (column) {
            case USERNAME -> "Username";
            case PROGRESS -> "Progress";
            case MINIGAMES -> "Minigames";
            case DAILY_QUESTS -> "Daily";
            case NON_DAILY_QUESTS -> "NonDaily";
            case BEST_SCORE -> "BestScore";
        };
    }

    private Table headerCell(
            Skin skin,
            String title,
            LeaderboardSortColumn column,
            float width,
            boolean sortable) {
        Table cell = new Table();
        cell.setTouchable(Touchable.enabled);
        cell.setClip(true);
        cell.setUserObject(column);
        boolean active = sortable && column == activeColumn;
        cell.setBackground(active ? headerActiveBg : headerBg);

        Label label = new Label(title, skin, "medium");
        label.setName("headerLabel");
        label.setColor(HEADER_TEXT);
        label.setAlignment(Align.center);
        label.setEllipsis(false);
        fitHeaderLabel(label, width, active);
        cell.add(label).expandX().center().padLeft(4).padRight(2);

        if (sortable) {
            Image marker = new Image(ascending ? arrowUpDrawable : arrowDownDrawable);
            marker.setName("sortMarker");
            marker.setScaling(Scaling.fit);
            marker.setVisible(active);
            cell.add(marker).size(MARKER_SIZE, MARKER_SIZE).padRight(4);
            PvzButtons.animate(cell, 1.02f, 0.97f, () -> {
                if (controller != null) {
                    controller.sort(column.getKey());
                }
            });
        }
        return cell;
    }

    private static void fitHeaderLabel(Label label, float columnWidth, boolean showMarker) {
        float available = columnWidth - 16f - (showMarker ? MARKER_SIZE + 10f : 0f);
        if (available < 8f) {
            available = 8f;
        }
        label.setFontScale(1f);
        float pref = label.getPrefWidth();
        if (pref <= 0f) {
            label.setFontScale(0.9f);
            return;
        }
        float scale = Math.min(0.92f, available / pref);
        label.setFontScale(Math.max(0.58f, scale));
    }

    private Actor sortOrderButton(Skin skin, String labelText, String arrowRegion, Runnable onClick) {
        Table button = new Table();
        if (headerBg != null) {
            button.setBackground(headerBg);
        }
        Drawable arrow = firstDrawable(skin, arrowRegion);
        if (arrow != null) {
            Image image = new Image(arrow);
            image.setScaling(Scaling.fit);
            button.add(image).size(24, 24).padRight(10).padLeft(6);
        }
        Label label = new Label(labelText, skin, "medium");
        label.setColor(HEADER_TEXT);
        button.add(label).padRight(12);
        button.padTop(6).padBottom(6);
        PvzButtons.animate(button, 1.06f, 0.94f, onClick);
        return button;
    }

    private Table buildDataRow(Skin skin, int rank, LeaderboardEntry entry, boolean even) {
        Table row = new Table();
        row.defaults().pad(0);
        Drawable bg = even ? rowEvenBg : rowOddBg;
        if (bg != null) {
            row.setBackground(bg);
        }

        addColumn(row, dataCell(skin, String.valueOf(rank), COL_RANK, Align.center, false), COL_RANK, ROW_HEIGHT, true);
        addColumn(row, dataCell(skin, entry.username(), COL_USER, Align.left, activeColumn == LeaderboardSortColumn.USERNAME), COL_USER, ROW_HEIGHT, true);
        addColumn(row, dataCell(skin, entry.progressLabel(), COL_PROGRESS, Align.left, activeColumn == LeaderboardSortColumn.PROGRESS), COL_PROGRESS, ROW_HEIGHT, true);
        addColumn(row, dataCell(skin, String.valueOf(entry.minigameCount()), COL_MINI, Align.center, activeColumn == LeaderboardSortColumn.MINIGAMES), COL_MINI, ROW_HEIGHT, true);
        addColumn(row, dataCell(skin, String.valueOf(entry.dailyQuestCount()), COL_DAILY, Align.center, activeColumn == LeaderboardSortColumn.DAILY_QUESTS), COL_DAILY, ROW_HEIGHT, true);
        addColumn(row, dataCell(skin, String.valueOf(entry.nonDailyQuestCount()), COL_NONDAILY, Align.center, activeColumn == LeaderboardSortColumn.NON_DAILY_QUESTS), COL_NONDAILY, ROW_HEIGHT, true);
        addColumn(row, dataCell(skin, String.valueOf(entry.bestScore()), COL_SCORE, Align.center, activeColumn == LeaderboardSortColumn.BEST_SCORE), COL_SCORE, ROW_HEIGHT, false);
        return row;
    }

    private Table dataCell(Skin skin, String text, float width, int align, boolean highlight) {
        Table cell = new Table();
        cell.setClip(true);
        if (highlight) {
            Drawable tint = tinted(sheetRegion("image_ui_quests_quest_list_bg"), ROW_ACTIVE);
            if (tint != null) {
                cell.setBackground(zeroMinSize(tint));
            }
        }
        Label label = new Label(text == null ? "" : text, skin, "medium");
        label.setColor(CELL_TEXT);
        label.setAlignment(align);
        label.setWrap(false);
        label.setFontScale(1.05f);
        cell.add(label).grow().padLeft(8).padRight(8);
        return cell;
    }

    private void showPlaceholder(String message) {
        if (sheetBody == null) {
            return;
        }
        sheetBody.clearChildren();
        Label label = new Label(message, assets.skin(), "medium");
        label.setColor(CELL_TEXT);
        label.setAlignment(Align.center);
        label.setFontScale(1.2f);
        sheetBody.add(label).width(TABLE_WIDTH).padTop(48);
    }

    private Actor lineHorizontal() {
        if (hLine == null) {
            return null;
        }
        Image image = new Image(hLine);
        image.setScaling(Scaling.stretchX);
        return image;
    }

    private Actor vSep(float height) {
        if (vLine == null) {
            Table spacer = new Table();
            spacer.setSize(SEP_WIDTH, height);
            return spacer;
        }
        Image image = new Image(vLine);
        image.setScaling(Scaling.stretch);
        image.setSize(SEP_WIDTH, height);
        return image;
    }

    private void resolveTheme(Skin skin) {
        sheetBg = firstDrawable(skin,
                "image_ui_quests_panel_edge_to_edge_ten",
                "image_ui_quests_panel_edge_to_edge",
                "image_ui_quests_quest_list_bg");
        headerBg = zeroMinSize(firstDrawable(skin,
                "image_ui_generic_brownbutton_10",
                "image_ui_generic_brownbutton"));
        headerActiveBg = null;
        if (skin.has("purple", TextButton.TextButtonStyle.class)) {
            headerActiveBg = zeroMinSize(skin.get("purple", TextButton.TextButtonStyle.class).up);
        }
        if (headerActiveBg == null) {
            headerActiveBg = headerBg;
        }
        hLine = firstDrawable(skin, "image_ui_cards_card_table_line_horizontal");
        vLine = firstDrawable(skin, "image_ui_cards_card_table_line_vertical");
        arrowUpDrawable = firstDrawable(skin, "image_ui_generic_arrow_up_green");
        arrowDownDrawable = firstDrawable(skin, "image_ui_generic_arrow_down_orange");
        TextureRegion listBg = sheetRegion("image_ui_quests_quest_list_bg");
        rowEvenBg = zeroMinSize(tinted(listBg, ROW_EVEN));
        rowOddBg = zeroMinSize(tinted(listBg, ROW_ODD));
        if (rowEvenBg == null && sheetBg != null) {
            rowEvenBg = sheetBg;
            rowOddBg = sheetBg;
        }
    }

    private TextureRegion sheetRegion(String name) {
        Skin skin = assets.skin();
        if (skin.has(name, TextureRegion.class)) {
            return skin.getRegion(name);
        }
        TextureAtlas atlas = skin.getAtlas();
        if (atlas != null) {
            return atlas.findRegion(name);
        }
        return null;
    }

    private static Drawable tinted(TextureRegion region, Color color) {
        if (region == null) {
            return null;
        }
        return new TextureRegionDrawable(region).tint(color);
    }

    private static Drawable zeroMinSize(Drawable source) {
        if (source == null) {
            return null;
        }
        if (source instanceof NinePatchDrawable nine) {
            NinePatch patch = nine.getPatch();
            NinePatchDrawable copy = patch != null
                    ? new NinePatchDrawable(new NinePatch(patch))
                    : new NinePatchDrawable(nine);
            copy.setMinWidth(0f);
            copy.setMinHeight(0f);
            return copy;
        }
        if (source instanceof TextureRegionDrawable region) {
            TextureRegionDrawable copy = new TextureRegionDrawable(region);
            copy.setMinWidth(0f);
            copy.setMinHeight(0f);
            return copy;
        }
        return source;
    }

    private static Drawable firstDrawable(Skin skin, String... names) {
        for (String name : names) {
            if (skin.has(name, Drawable.class)) {
                return skin.getDrawable(name);
            }
            if (skin.has(name, TextureRegion.class)) {
                return new TextureRegionDrawable(skin.getRegion(name));
            }
            TextureAtlas atlas = skin.getAtlas();
            if (atlas != null) {
                TextureRegion region = atlas.findRegion(name);
                if (region != null) {
                    return new TextureRegionDrawable(region);
                }
            }
        }
        return null;
    }
}
