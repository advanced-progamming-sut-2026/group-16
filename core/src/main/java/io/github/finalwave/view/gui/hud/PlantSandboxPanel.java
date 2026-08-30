package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LoadoutOrder;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.widget.PlantCardActor;
import io.github.finalwave.view.gui.widget.StoreChrome;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public final class PlantSandboxPanel extends Table {
    public static final float PANEL_WIDTH = 268f;

    private static final float CARD_WIDTH = PlantCardActor.WIDTH;
    private static final float CARD_HEIGHT = PlantCardActor.HEIGHT;
    private static final float CARD_GAP = 8f;
    private static final float LIST_PAD = 16f;
    private static final float SCROLL_PAD = 8f;

    private final GameAssets assets;
    private final Consumer<String> onSelect;
    private final Table list = new Table();
    private final List<PlantCardActor> cards = new ArrayList<>();
    private List<String> loadout = List.of();

    public PlantSandboxPanel(GameAssets assets, Consumer<String> onSelect) {
        this.assets = assets;
        this.onSelect = onSelect;
        Skin skin = assets.skin();
        setBackground(StoreChrome.panel());
        pad(StoreChrome.PANEL_PAD_TOP, StoreChrome.PANEL_PAD_LEFT,
                StoreChrome.PANEL_PAD_BOTTOM, StoreChrome.PANEL_PAD_RIGHT);
        defaults().growX();
        top();

        Label title = new Label("Plant sandbox", skin, "medium");
        title.setAlignment(Align.center);
        title.setFontScale(0.86f);
        add(title).padBottom(10f).row();

        list.defaults().pad(CARD_GAP).center();
        list.pad(LIST_PAD);
        list.top();
        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, true);
        add(scroll).grow().pad(SCROLL_PAD).row();
        setWidth(PANEL_WIDTH);
    }

    public void refresh(GameSession session, ToolMode mode) {
        if (session == null) {
            setVisible(false);
            return;
        }
        setVisible(true);
        List<String> next = LoadoutOrder.effective(session);
        if (!next.equals(loadout)) {
            rebuild(next);
        }
        String selected = mode instanceof ToolMode.Seed seed ? seed.plantName() : null;
        for (PlantCardActor card : cards) {
            String name = card.plantName();
            PlantDefinition definition = session.getPlantRegistry().getDefinition(name);
            int cost = definition == null ? 0 : definition.getCost();
            card.setCost(cost);
            card.setCooldownRatio(0f);
            card.setAffordable(true);
            card.setDisabled(false);
            card.setSelected(name != null && name.equals(selected));
            card.setFamily(definition == null ? null : definition.getCategory());
            card.setLevel(0);
            card.setNameOverlayIndex(definition == null ? 0 : definition.getId());
        }
    }

    private void rebuild(List<String> names) {
        loadout = names;
        cards.clear();
        list.clearChildren();
        for (String name : names) {
            PlantCardActor card = new PlantCardActor(assets, assets.skin(), name);
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.setNameOverlay(true);
            card.setOnClick(() -> onSelect.accept(name));
            cards.add(card);
            list.add(card).size(CARD_WIDTH, CARD_HEIGHT).row();
        }
    }
}
