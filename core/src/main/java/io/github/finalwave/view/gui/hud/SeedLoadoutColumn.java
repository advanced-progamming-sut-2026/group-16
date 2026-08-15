package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PlantCardActor;

import java.util.List;


public final class SeedLoadoutColumn extends Table {
    private static final float CARD_WIDTH = 116f;
    private static final float CARD_HEIGHT = 84f;
    private static final float CARD_GAP = 3f;

    private final GameAssets assets;

    public SeedLoadoutColumn(GameAssets assets) {
        this.assets = assets;
        defaults().pad(CARD_GAP);
        top();
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
            add(card).size(CARD_WIDTH, CARD_HEIGHT).row();
        }
    }

    private void bindFilled(PlantSelectionController controller, PlantCardActor card, String name) {
        card.setPlant(name);
        card.setCost(0);
        card.setLevel(0);
        card.setFamily(null);
        card.setBoosted(controller.isBoosted(name));
        PlantDefinition definition = controller.plantRegistry().getDefinition(name);
        if (definition != null) {
            card.setCost(definition.getCost());
        }
        card.setOnClick(() -> controller.removePlant(name));
    }
}
