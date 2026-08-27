package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class ZombieSandboxPanel extends Table {
    public static final List<String> ALIASES = List.of(
            "ZombieDefault",
            "ZombieArmor1",
            "ZombieArmor2",
            "ZombieArmor4",
            "ZombieDarkArmor3",
            "ZombieGargantuar",
            "ZombieImp",
            "ZombieRa",
            "ZombieExplorer",
            "ZombieTombRaiser",
            "ZombieIceAgeDodo",
            "ZombieIceAgeHunter",
            "ZombieIceAgeTroglobite",
            "ZombieBeachFisherman",
            "ZombieBeachOctopus",
            "ZombieBeachSnorkel",
            "ZombieDarkJuggler",
            "ZombieWizard",
            "ZombieDarkKing",
            "ZombieDarkImpDragon",
            "ZombieModernAllStar",
            "ZombieLostCityJane",
            "ZombieCrystalSkull",
            "ZombieProspector",
            "ZombiePiano",
            "ZombieNewspaper",
            "ZombieArcade"
    );

    public interface Host {
        void spawnSolo(String alias, int row);

        void spawnPack(String alias);

        void spawnWave();

        void clearZombies();

        void dropSun();
    }

    private static final float PANEL_WIDTH = 348f;
    private static final float LIST_HEIGHT = 520f;
    private static final Color SELECTED_TINT = new Color(1f, 0.92f, 0.45f, 1f);

    private final Host host;
    private String selected = ALIASES.getFirst();
    private int lane = 2;
    private final Label selectedLabel;
    private final Label laneLabel;
    private final Map<String, TextButton> aliasButtons = new LinkedHashMap<>();
    private final List<TextButton> laneButtons = new java.util.ArrayList<>();

    public ZombieSandboxPanel(GameAssets assets, Host host) {
        this.host = host;
        Skin skin = assets.skin();
        setBackground(StoreChrome.panel());
        pad(12f);
        defaults().growX();

        Label title = new Label("Zombie sandbox", skin, "medium");
        title.setAlignment(Align.center);
        title.setFontScale(0.92f);
        add(title).padBottom(8f).row();

        selectedLabel = new Label(shortName(selected), skin, "medium");
        selectedLabel.setAlignment(Align.center);
        selectedLabel.setFontScale(0.8f);
        selectedLabel.setColor(Color.GOLD);
        add(selectedLabel).padBottom(8f).row();

        Table grid = new Table();
        grid.defaults().pad(3f);
        int column = 0;
        for (String alias : ALIASES) {
            TextButton button = PvzButtons.textButton(shortName(alias), skin, "brown", () -> select(alias));
            button.getLabel().setFontScale(0.68f);
            aliasButtons.put(alias, button);
            grid.add(button).width(152f).height(44f);
            column++;
            if (column == 2) {
                grid.row();
                column = 0;
            }
        }
        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        add(scroll).height(LIST_HEIGHT).padBottom(10f).row();

        Table lanes = new Table();
        lanes.defaults().pad(3f);
        laneLabel = new Label("Lane " + (lane + 1), skin, "medium");
        laneLabel.setFontScale(0.72f);
        lanes.add(laneLabel).colspan(5).padBottom(6f).row();
        for (int row = 0; row < 5; row++) {
            int index = row;
            TextButton laneButton = PvzButtons.textButton(String.valueOf(row + 1), skin, "brown",
                    () -> setLane(index));
            laneButton.getLabel().setFontScale(0.78f);
            laneButtons.add(laneButton);
            lanes.add(laneButton).width(58f).height(44f);
        }
        add(lanes).padBottom(10f).row();

        Table actions = new Table();
        actions.defaults().pad(4f);
        actions.add(action(skin, "Spawn", () -> host.spawnSolo(selected, lane))).width(156f).height(56f);
        actions.add(action(skin, "Solo", () -> {
            host.clearZombies();
            host.spawnSolo(selected, lane);
        })).width(156f).height(56f).row();
        actions.add(action(skin, "Line", () -> {
            for (int row = 0; row < 5; row++) {
                host.spawnSolo(selected, row);
            }
        })).width(156f).height(56f);
        actions.add(action(skin, "Pack", () -> host.spawnPack(selected))).width(156f).height(56f).row();
        actions.add(action(skin, "Wave 27", host::spawnWave)).width(156f).height(56f);
        actions.add(action(skin, "Clear", host::clearZombies)).width(156f).height(56f).row();
        actions.add(action(skin, "Drop sun", host::dropSun)).width(320f).height(56f).colspan(2);
        add(actions);
        pack();
        setWidth(PANEL_WIDTH);
        refreshSelection();
        refreshLanes();
    }

    private void select(String alias) {
        selected = alias;
        selectedLabel.setText(shortName(alias));
        refreshSelection();
    }

    private void setLane(int row) {
        lane = row;
        laneLabel.setText("Lane " + (row + 1));
        refreshLanes();
    }

    private void refreshSelection() {
        for (Map.Entry<String, TextButton> entry : aliasButtons.entrySet()) {
            boolean on = selected.equals(entry.getKey());
            entry.getValue().getLabel().setColor(on ? SELECTED_TINT : Color.WHITE);
            entry.getValue().getLabel().setFontScale(on ? 0.76f : 0.68f);
        }
    }

    private void refreshLanes() {
        for (int i = 0; i < laneButtons.size(); i++) {
            boolean on = i == lane;
            laneButtons.get(i).getLabel().setColor(on ? SELECTED_TINT : Color.WHITE);
        }
    }

    private static TextButton action(Skin skin, String text, Runnable onClick) {
        TextButton button = PvzButtons.textButton(text, skin, "brown", onClick);
        button.getLabel().setFontScale(0.72f);
        return button;
    }

    public static String shortName(String alias) {
        if (alias == null) {
            return "";
        }
        return switch (alias) {
            case "ZombieDefault" -> "Basic";
            case "ZombieArmor1" -> "Cone";
            case "ZombieArmor2" -> "Bucket";
            case "ZombieArmor4" -> "Brick";
            case "ZombieDarkArmor3" -> "Dark 3";
            case "ZombieGargantuar" -> "Garg";
            case "ZombieImp" -> "Imp";
            case "ZombieRa" -> "Ra";
            case "ZombieExplorer" -> "Explorer";
            case "ZombieTombRaiser" -> "Tomb";
            case "ZombieIceAgeDodo" -> "Dodo";
            case "ZombieIceAgeHunter" -> "Hunter";
            case "ZombieIceAgeTroglobite" -> "Troglo";
            case "ZombieBeachFisherman" -> "Fisher";
            case "ZombieBeachOctopus" -> "Octopus";
            case "ZombieBeachSnorkel" -> "Snorkel";
            case "ZombieDarkJuggler" -> "Jester";
            case "ZombieWizard" -> "Wizard";
            case "ZombieDarkKing" -> "King";
            case "ZombieDarkImpDragon" -> "Dragon";
            case "ZombieModernAllStar" -> "All-Star";
            case "ZombieLostCityJane" -> "Jane";
            case "ZombieCrystalSkull" -> "Crystal";
            case "ZombieProspector" -> "Prospect";
            case "ZombiePiano" -> "Piano";
            case "ZombieNewspaper" -> "Paper";
            case "ZombieArcade" -> "Arcade";
            default -> alias.replace("Zombie", "");
        };
    }
}
