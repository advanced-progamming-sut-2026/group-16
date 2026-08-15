package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PlantCardActor;

import java.util.List;


public final class SeedLoadoutBar extends Table {
    private static final float CARD_WIDTH = 90f;
    private static final float CARD_HEIGHT = 60f;

    private final GameAssets assets;

    public SeedLoadoutBar(GameAssets assets) {
        this.assets = assets;
        defaults().pad(5f);
        center();
    }

    public void refresh(PlantSelectionController controller) {
        clearChildren();
        if (controller == null || controller.getLevel() == null) {
            return;
        }
        int slots = Math.max(1, controller.getLevel().getPlantSlotCount());
        List<String> selected = controller.selectedPlants();
        for (int i = 0; i < slots; i++) {
            PlantCardActor card = new PlantCardActor(assets, assets.skin(), null);
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            if (i < selected.size()) {
                bindFilled(controller, card, selected.get(i));
            } else {
                card.setEmpty();
            }
            add(card).size(CARD_WIDTH, CARD_HEIGHT);
        }
    }

    private void bindFilled(PlantSelectionController controller, PlantCardActor card, String name) {
        card.setPlant(name);
        PlantDefinition definition = controller.plantRegistry().getDefinition(name);
        card.setCost(0);
        card.setLevel(0);
        card.setFamily(null);
        card.setBoosted(controller.isBoosted(name));
        if (definition != null) {
            card.setCost(definition.getCost());
        }
        card.setOnClick(() -> controller.removePlant(name));
    }
}
