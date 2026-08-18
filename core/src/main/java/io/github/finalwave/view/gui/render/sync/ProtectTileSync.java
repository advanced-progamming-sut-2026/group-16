package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.SeedPlacement;
import io.github.finalwave.view.gui.render.HazardStripeFrames;
import io.github.finalwave.view.gui.render.LawnLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class ProtectTileSync {
    public static final String PAM_PATH = "768/INITIAL/BACKGROUNDS/PROTECT_TILE/PROTECT_TILE.PAM";

    private final LawnLayout layout;
    private final Group layer;
    private final HazardStripeFrames stripes;
    private final Map<String, Image> tiles = new HashMap<>();
    private boolean disposed;

    public ProtectTileSync(LawnLayout layout, Group layer) {
        this.layout = layout;
        this.layer = layer;
        this.stripes = new HazardStripeFrames();
    }

    public void sync(GameSession session) {
        if (disposed) {
            return;
        }
        List<SeedPlacement> placements = session == null
                ? List.of()
                : session.getProtectedSeedPlacements();
        Map<String, SeedPlacement> live = new HashMap<>();
        for (SeedPlacement placement : placements) {
            if (placement != null) {
                live.put(key(placement), placement);
            }
        }
        Iterator<Map.Entry<String, Image>> iterator = tiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Image> entry = iterator.next();
            if (!live.containsKey(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
        for (Map.Entry<String, SeedPlacement> entry : live.entrySet()) {
            Image tile = tiles.get(entry.getKey());
            if (tile == null) {
                tile = spawn();
                tiles.put(entry.getKey(), tile);
            }
            layoutTile(tile, entry.getValue());
        }
    }

    public void clear() {
        for (Image tile : tiles.values()) {
            tile.remove();
        }
        tiles.clear();
        if (!disposed) {
            stripes.dispose();
            disposed = true;
        }
    }

    private Image spawn() {
        Image tile = new Image(stripes.drawable());
        tile.setTouchable(Touchable.disabled);
        tile.setScaling(Scaling.stretch);
        tile.setAlign(Align.center);
        layer.addActor(tile);
        return tile;
    }

    private void layoutTile(Image tile, SeedPlacement placement) {
        Vector2 origin = layout.cellOrigin(placement.getCol(), placement.getRow());
        float bleed = HazardStripeFrames.BLEED;
        tile.setBounds(
                origin.x - bleed,
                origin.y - bleed,
                layout.tileWidth() + bleed * 2f,
                layout.tileHeight() + bleed * 2f);
    }

    private static String key(SeedPlacement placement) {
        return placement.getCol() + ":" + placement.getRow();
    }
}
