package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.screen.MenuScreen;
import io.github.finalwave.view.gui.widget.CollectionPlantCard;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public final class ConveyorBeltBar extends Group {
    private static final float INNER_PAD_RATIO = 0.055f;
    private static final float CARD_SPEED = 240f;
    private static final float BELT_SPEED = 72f;
    private static final float TOP_INSET = 8f;

    private final GameAssets assets;
    private final Consumer<String> onSelect;
    private final Group beltLayer = new Group();
    private final Group sideLayer = new Group();
    private final Group cardLayer = new Group();
    private final List<Image> beltTiles = new ArrayList<>();
    private final List<Image> sideTiles = new ArrayList<>();
    private final List<BeltCard> cards = new ArrayList<>();

    private float beltWidth = 1f;
    private float beltTileHeight = 1f;
    private float sideWidth = 1f;
    private float sideTileHeight = 1f;
    private float cardWidth = 1f;
    private float cardHeight = 1f;
    private boolean paused;
    private float beltScroll;

    public ConveyorBeltBar(GameAssets assets, Consumer<String> onSelect) {
        this.assets = assets;
        this.onSelect = onSelect;
        setTransform(true);
        setTouchable(Touchable.childrenOnly);
        setPosition(0f, 0f);

        beltLayer.setTransform(true);
        beltLayer.setTouchable(Touchable.disabled);
        sideLayer.setTransform(true);
        sideLayer.setTouchable(Touchable.disabled);
        cardLayer.setTouchable(Touchable.childrenOnly);

        addActor(beltLayer);
        addActor(sideLayer);
        addActor(cardLayer);
        ensureStrips();
        setVisible(false);
    }

    public float stripWidth() {
        return beltWidth + sideWidth;
    }

    public void refresh(GameSession session, User user, ToolMode mode, boolean paused) {
        this.paused = paused;
        if (session == null || !session.isConveyorBeltActive()) {
            setVisible(false);
            return;
        }
        setVisible(true);
        ensureStrips();
        setPosition(0f, 0f);
        syncQueue(session.getConveyorBeltPlants(), user, session);
        assignTargets();
        String selected = mode instanceof ToolMode.Seed seed ? seed.plantName() : null;
        for (BeltCard card : cards) {
            card.actor.setSelected(card.plantName.equals(selected));
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible()) {
            return;
        }
        ensureStrips();
        if (paused) {
            return;
        }
        beltScroll += BELT_SPEED * delta;
        layoutBeltTiles();
        for (BeltCard card : cards) {
            float y = card.actor.getY();
            float target = card.targetY;
            if (y < target) {
                card.actor.setY(Math.min(target, y + CARD_SPEED * delta));
            } else if (y > target) {
                card.actor.setY(Math.max(target, y - CARD_SPEED * delta));
            }
        }
    }

    private void ensureStrips() {
        TextureRegion belt = assets.region(LawnAssetIds.CONVEYOR_BELT);
        TextureRegion side = assets.region(LawnAssetIds.CONVEYOR_SIDE);
        float nextBeltW = Math.max(1f, belt.getRegionWidth());
        float nextBeltH = Math.max(1f, belt.getRegionHeight());
        float nextSideW = Math.max(1f, side.getRegionWidth());
        float nextSideH = Math.max(1f, side.getRegionHeight());
        boolean rebuild = beltTiles.isEmpty()
                || sideTiles.isEmpty()
                || nextBeltW != beltWidth
                || nextBeltH != beltTileHeight
                || nextSideW != sideWidth
                || nextSideH != sideTileHeight;
        beltWidth = nextBeltW;
        beltTileHeight = nextBeltH;
        sideWidth = nextSideW;
        sideTileHeight = nextSideH;
        cardWidth = beltWidth * (1f - 2f * INNER_PAD_RATIO);
        cardHeight = cardWidth * (CollectionPlantCard.CARD_HEIGHT / CollectionPlantCard.CARD_WIDTH);
        setSize(beltWidth + sideWidth, MenuScreen.WORLD_HEIGHT);
        beltLayer.setBounds(0f, 0f, beltWidth, MenuScreen.WORLD_HEIGHT);
        sideLayer.setBounds(beltWidth, 0f, sideWidth, MenuScreen.WORLD_HEIGHT);
        cardLayer.setBounds(0f, 0f, beltWidth, MenuScreen.WORLD_HEIGHT);
        if (rebuild) {
            fillTiles(beltLayer, beltTiles, belt, beltWidth, beltTileHeight);
            fillTiles(sideLayer, sideTiles, side, sideWidth, sideTileHeight);
            layoutBeltTiles();
            for (BeltCard card : cards) {
                card.actor.setSize(cardWidth, cardHeight);
                card.actor.setX(beltWidth * INNER_PAD_RATIO);
            }
        }
    }

    private void fillTiles(Group layer,
                           List<Image> tiles,
                           TextureRegion region,
                           float tileWidth,
                           float tileHeight) {
        layer.clearChildren();
        tiles.clear();
        int count = Math.max(2, (int) Math.ceil(MenuScreen.WORLD_HEIGHT / tileHeight) + 2);
        TextureRegionDrawable drawable = new TextureRegionDrawable(region);
        for (int i = 0; i < count; i++) {
            Image tile = new Image(drawable);
            tile.setScaling(Scaling.stretch);
            tile.setTouchable(Touchable.disabled);
            tile.setSize(tileWidth, tileHeight);
            tile.setPosition(0f, i * tileHeight - tileHeight);
            layer.addActor(tile);
            tiles.add(tile);
        }
    }

    private void layoutBeltTiles() {
        if (beltTiles.isEmpty() || beltTileHeight <= 0f) {
            return;
        }
        float offset = beltScroll % beltTileHeight;
        if (offset < 0f) {
            offset += beltTileHeight;
        }
        for (int i = 0; i < beltTiles.size(); i++) {
            beltTiles.get(i).setY(i * beltTileHeight + offset - beltTileHeight);
        }
    }

    private void syncQueue(List<String> next, User user, GameSession session) {
        int i = 0;
        int j = 0;
        List<BeltCard> kept = new ArrayList<>();
        while (i < cards.size() && j < next.size()) {
            BeltCard current = cards.get(i);
            if (current.plantName.equals(next.get(j))) {
                kept.add(current);
                i++;
                j++;
            } else {
                current.actor.remove();
                i++;
            }
        }
        while (i < cards.size()) {
            cards.get(i).actor.remove();
            i++;
        }
        cards.clear();
        cards.addAll(kept);
        while (j < next.size()) {
            cards.add(spawn(next.get(j), user, session));
            j++;
        }
    }

    private BeltCard spawn(String plantName, User user, GameSession session) {
        CollectionPlantCard actor = new CollectionPlantCard(assets, assets.skin());
        actor.bindConveyor(entryFor(plantName, user, session));
        actor.setSize(cardWidth, cardHeight);
        actor.setOrigin(Align.center);
        actor.setX(beltWidth * INNER_PAD_RATIO);
        actor.setY(-cardHeight);
        PvzButtons.animate(actor, 1.08f, 0.92f, () -> onSelect.accept(plantName));
        cardLayer.addActor(actor);
        BeltCard card = new BeltCard(plantName, actor);
        card.targetY = -cardHeight;
        return card;
    }

    private void assignTargets() {
        float topY = getHeight() - TOP_INSET - cardHeight;
        for (int i = 0; i < cards.size(); i++) {
            BeltCard card = cards.get(i);
            card.targetY = topY - i * cardHeight;
            if (card.actor.getY() < -cardHeight && card.targetY < -cardHeight) {
                card.actor.setY(card.targetY);
            }
        }
    }

    private CollectionPlantEntry entryFor(String plantName, User user, GameSession session) {
        PlantDefinition definition = session == null ? null : session.getPlantRegistry().getDefinition(plantName);
        String category = definition == null ? "" : definition.getCategory();
        List<String> tags = definition == null || definition.getTags() == null ? List.of() : definition.getTags();
        int level = 1;
        if (user != null) {
            level = user.getPlantProgress().getOwnedPlant(plantName)
                    .map(owned -> owned.getLevel())
                    .orElse(1);
        }
        return new CollectionPlantEntry(
                plantName,
                category,
                tags,
                level,
                true,
                false,
                0,
                1,
                0,
                false,
                false);
    }

    private static final class BeltCard {
        private final String plantName;
        private final CollectionPlantCard actor;
        private float targetY;

        private BeltCard(String plantName, CollectionPlantCard actor) {
            this.plantName = plantName;
            this.actor = actor;
        }
    }
}
