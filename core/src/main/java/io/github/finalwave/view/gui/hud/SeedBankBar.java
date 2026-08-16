package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.PlantArmor;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.widget.PlantCardActor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public final class SeedBankBar extends Table {
    private static final float CARD_WIDTH = PlantCardActor.WIDTH;
    private static final float CARD_HEIGHT = PlantCardActor.HEIGHT;
    private static final float CARD_GAP = 2f;
    private static final int MAX_CARDS = 8;

    private final GameAssets assets;
    private final Consumer<String> onSelect;
    private final List<PlantCardActor> cards = new ArrayList<>();
    private List<String> loadout = List.of();

    public SeedBankBar(GameAssets assets, Consumer<String> onSelect) {
        this.assets = assets;
        this.onSelect = onSelect;
        padLeft(22f);
        defaults().pad(CARD_GAP);
        top().left();
        setClip(false);
    }

    public void refresh(GameSession session, User user, Set<String> boosted, ToolMode mode) {
        if (session == null || session.isConveyorBeltActive()) {
            setVisible(false);
            return;
        }
        setVisible(true);
        List<String> next = namesOf(session.getSelectedLoadout());
        if (!next.equals(loadout)) {
            rebuild(next);
        }
        String selected = mode instanceof ToolMode.Seed seed ? seed.plantName() : null;
        PlantArmor.PlantCooldownTracker cooldowns = session.getCooldownTracker();
        int sun = session.getSunBalance();
        for (PlantCardActor card : cards) {
            String name = card.plantName();
            PlantDefinition definition = session.getPlantRegistry().getDefinition(name);
            int cost = definition == null ? 0 : definition.getCost();
            double recharge = definition == null ? 0d : definition.getRecharge();
            int remaining = cooldowns == null ? 0 : cooldowns.ticksRemaining(name);
            float ratio = recharge <= 0d ? 0f : remaining / (float) (recharge * GameSession.TICKS_PER_SECOND);
            boolean affordable = sun >= cost;
            card.setCost(cost);
            card.setCooldownRatio(ratio);
            card.setBoosted(boosted != null && boosted.contains(name));
            card.setAffordable(affordable);
            card.setDisabled(false);
            card.setSelected(name.equals(selected));
            card.setFamily(definition == null ? null : definition.getCategory());
            if (user != null) {
                card.setLevel(user.getPlantProgress().getOwnedPlant(name)
                        .map(owned -> owned.getLevel())
                        .orElse(1));
            }
        }
    }

    private void rebuild(List<String> names) {
        loadout = names;
        cards.clear();
        clearChildren();
        int count = Math.min(MAX_CARDS, names.size());
        for (int i = 0; i < count; i++) {
            String name = names.get(i);
            PlantCardActor card = new PlantCardActor(assets, assets.skin(), name);
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.setOnClick(() -> onSelect.accept(name));
            cards.add(card);
            add(card).size(CARD_WIDTH, CARD_HEIGHT).row();
        }
    }

    private static List<String> namesOf(Set<String> loadout) {
        if (loadout == null || loadout.isEmpty()) {
            return List.of();
        }
        return List.copyOf(loadout);
    }
}
