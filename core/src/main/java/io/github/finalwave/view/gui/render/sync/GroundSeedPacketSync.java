package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.GroundSeedPacket;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PlantCardActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class GroundSeedPacketSync {
    private static final float TILE_WIDTH_FIT = 0.9f;
    private static final float TILE_HEIGHT_FIT = 0.72f;
    private static final float DROP_HEIGHT_RATIO = 0.55f;
    private static final float DROP_SECONDS = 0.36f;
    private static final float POP_SECONDS = 0.28f;
    private static final float FADE_SECONDS = 0.12f;
    private static final float START_SCALE = 0.78f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PlantCardActor> cards = new HashMap<>();

    public GroundSeedPacketSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        List<GroundSeedPacket> packets = session == null
                ? List.of()
                : session.getGroundSeedPackets();
        Map<String, GroundSeedPacket> live = new HashMap<>();
        for (GroundSeedPacket packet : packets) {
            if (packet == null || packet.plantName() == null || packet.plantName().isBlank()) {
                continue;
            }
            live.put(key(packet), packet);
        }
        Iterator<Map.Entry<String, PlantCardActor>> iterator = cards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PlantCardActor> entry = iterator.next();
            GroundSeedPacket packet = live.get(entry.getKey());
            if (packet == null || !packet.plantName().equals(entry.getValue().plantName())) {
                entry.getValue().clearActions();
                entry.getValue().remove();
                iterator.remove();
            }
        }
        for (Map.Entry<String, GroundSeedPacket> entry : live.entrySet()) {
            GroundSeedPacket packet = entry.getValue();
            PlantCardActor card = cards.get(entry.getKey());
            boolean spawned = card == null;
            if (spawned) {
                card = spawn(packet.plantName());
                cards.put(entry.getKey(), card);
            }
            layoutCard(card, packet, session, spawned);
        }
    }

    public void clear() {
        for (PlantCardActor card : cards.values()) {
            card.clearActions();
            card.remove();
        }
        cards.clear();
    }

    private PlantCardActor spawn(String plantName) {
        PlantCardActor card = new PlantCardActor(assets, assets.skin(), plantName);
        card.setTouchable(Touchable.disabled);
        card.setOnClick(null);
        layer.addActor(card);
        return card;
    }

    private void layoutCard(PlantCardActor card, GroundSeedPacket packet, GameSession session, boolean spawned) {
        float scale = Math.min(
                layout.tileWidth() * TILE_WIDTH_FIT / PlantCardActor.WIDTH,
                layout.tileHeight() * TILE_HEIGHT_FIT / PlantCardActor.HEIGHT);
        float width = PlantCardActor.WIDTH * scale;
        float height = PlantCardActor.HEIGHT * scale;
        Vector2 origin = layout.cellOrigin(packet.col(), packet.row());
        float restX = origin.x + (layout.tileWidth() - width) / 2f;
        float restY = origin.y + (layout.tileHeight() - height) / 2f;
        card.setSize(width, height);
        bindLook(card, packet, session);
        if (spawned) {
            playDrop(card, restX, restY);
            return;
        }
        if (card.getActions().size == 0) {
            card.setPosition(restX, restY);
            card.setScale(1f);
            card.getColor().a = 1f;
        }
    }

    private void bindLook(PlantCardActor card, GroundSeedPacket packet, GameSession session) {
        card.setCost(0);
        card.setCooldownRatio(0f);
        card.setBoosted(false);
        card.setAffordable(true);
        card.setDisabled(false);
        card.setLocked(false);
        card.setLevel(0);
        card.setSeedProgress(0, 0);
        PlantDefinition definition = session == null || session.getPlantRegistry() == null
                ? null
                : session.getPlantRegistry().getDefinition(packet.plantName());
        card.setFamily(definition == null ? null : definition.getCategory());
        card.setUserObject(packet.row());
    }

    private void playDrop(PlantCardActor card, float restX, float restY) {
        float dropHeight = layout.tileHeight() * DROP_HEIGHT_RATIO;
        card.setPosition(restX, restY + dropHeight);
        card.setScale(START_SCALE);
        card.getColor().a = 0f;
        card.addAction(Actions.parallel(
                Actions.moveTo(restX, restY, DROP_SECONDS, Interpolation.swingOut),
                Actions.scaleTo(1f, 1f, POP_SECONDS, Interpolation.sineOut),
                Actions.alpha(1f, FADE_SECONDS)));
    }

    private static String key(GroundSeedPacket packet) {
        return packet.col() + ":" + packet.row();
    }
}
