package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.beghouled.BeghouledBoard;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeRule;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PlantCardActor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public final class BeghouledUpgradeBar extends Table {
    private static final float CARD_WIDTH = 96f;
    private static final float CARD_HEIGHT = 72f;
    private static final float CARD_GAP = 6f;

    private final GameAssets assets;
    private final Consumer<String> onUpgrade;
    private final List<PlantCardActor> cards = new ArrayList<>();
    private List<BeghouledUpgradeRule> rules = List.of();

    public BeghouledUpgradeBar(GameAssets assets, Consumer<String> onUpgrade) {
        this.assets = assets;
        this.onUpgrade = onUpgrade;
        padLeft(8f);
        defaults().padRight(CARD_GAP);
        left().bottom();
        setVisible(false);
    }

    public void refresh(GameSession session) {
        if (session == null || !session.isBeghouledActive() || session.getBeghouledBoard() == null) {
            setVisible(false);
            return;
        }
        BeghouledBoard board = session.getBeghouledBoard();
        List<BeghouledUpgradeRule> next = board.getUpgradeCatalog() == null
                ? List.of()
                : board.getUpgradeCatalog().getRules();
        setVisible(true);
        if (!next.equals(rules)) {
            rebuild(next);
        }
        int sun = session.getSunBalance();
        for (int i = 0; i < cards.size() && i < rules.size(); i++) {
            BeghouledUpgradeRule rule = rules.get(i);
            PlantCardActor card = cards.get(i);
            PlantDefinition definition = session.getPlantRegistry() == null
                    ? null
                    : session.getPlantRegistry().getDefinition(rule.fromPlant());
            card.setCost(rule.sunCost());
            card.setAffordable(sun >= rule.sunCost());
            card.setDisabled(false);
            card.setSelected(false);
            card.setLevel(0);
            card.setCooldownRatio(0f);
            card.setFamily(definition == null ? null : definition.getCategory());
        }
    }

    private void rebuild(List<BeghouledUpgradeRule> next) {
        rules = List.copyOf(next);
        cards.clear();
        clearChildren();
        for (BeghouledUpgradeRule rule : rules) {
            PlantCardActor card = new PlantCardActor(assets, assets.skin(), rule.fromPlant());
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.setOnClick(() -> {
                if (onUpgrade != null) {
                    onUpgrade.accept(rule.fromPlant());
                }
            });
            cards.add(card);
            add(card).size(CARD_WIDTH, CARD_HEIGHT);
        }
    }
}
