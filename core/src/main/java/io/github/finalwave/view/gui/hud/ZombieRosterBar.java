package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.PlantArmor;
import io.github.finalwave.model.minigame.izombie.IZombiePacketRecharge;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.widget.PlantCardActor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public final class ZombieRosterBar extends Table {
    private static final float CARD_WIDTH = PlantCardActor.WIDTH;
    private static final float CARD_HEIGHT = PlantCardActor.HEIGHT;
    private static final float CARD_GAP = 2f;
    private static final int MAX_CARDS = 8;

    private final GameAssets assets;
    private final Consumer<String> onSelect;
    private final List<PlantCardActor> cards = new ArrayList<>();
    private List<String> roster = List.of();
    private String keyboardSelected;

    public ZombieRosterBar(GameAssets assets, Consumer<String> onSelect) {
        this.assets = assets;
        this.onSelect = onSelect;
        padLeft(22f);
        defaults().pad(CARD_GAP);
        top().left();
        setClip(false);
    }

    public void keyboardSelect(String alias) {
        this.keyboardSelected = alias;
    }

    public void setInputEnabled(boolean enabled) {
        setTouchable(enabled ? Touchable.childrenOnly : Touchable.disabled);
    }

    public void refresh(GameSession session, ToolMode mode) {
        if (session == null || !session.isIZombieActive()) {
            setVisible(false);
            return;
        }
        setVisible(true);
        List<String> next = session.getIZombieZombiePool();
        if (!next.equals(roster)) {
            rebuild(next);
        }
        String selected;
        if (keyboardSelected != null) {
            selected = keyboardSelected;
        } else {
            selected = mode instanceof ToolMode.Zombie zombie ? zombie.alias() : null;
        }
        PlantArmor.PlantCooldownTracker cooldowns = session.getCooldownTracker();
        Map<String, Integer> costs = session.getIZombieZombieCosts();
        int sun = session.isIZombieActive() ? session.getIZombieSunBalance() : session.getSunBalance();
        for (PlantCardActor card : cards) {
            String alias = card.plantName();
            int cost = costs.getOrDefault(alias, 0);
            double recharge = IZombiePacketRecharge.secondsFor(alias);
            int remaining = cooldowns == null ? 0 : cooldowns.ticksRemaining(alias);
            float ratio = recharge <= 0d ? 0f : remaining / (float) (recharge * GameSession.TICKS_PER_SECOND);
            card.setCost(cost);
            card.setCooldownRatio(ratio);
            card.setAffordable(sun >= cost);
            card.setDisabled(remaining > 0);
            card.setSelected(alias != null && alias.equals(selected));
            card.setLevel(0);
        }
    }

    private void rebuild(List<String> names) {
        roster = List.copyOf(names);
        cards.clear();
        clearChildren();
        int count = Math.min(MAX_CARDS, names.size());
        for (int i = 0; i < count; i++) {
            String alias = names.get(i);
            PlantCardActor card = new PlantCardActor(assets, assets.skin(), alias);
            card.setZombie(alias);
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.setOnClick(() -> onSelect.accept(alias));
            cards.add(card);
            add(card).size(CARD_WIDTH, CARD_HEIGHT).row();
        }
    }
}
