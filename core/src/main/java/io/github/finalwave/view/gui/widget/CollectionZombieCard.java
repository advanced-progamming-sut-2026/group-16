package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.collection.CollectionZombieEntry;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class CollectionZombieCard extends Table {
    public static final float CARD_WIDTH = 168f;
    public static final float CARD_HEIGHT = 210f;

    private final GameAssets assets;

    public CollectionZombieCard(GameAssets assets, Skin skin) {
        this.assets = assets;
        setTouchable(Touchable.enabled);
    }

    public void bind(CollectionZombieEntry entry) {
        clearChildren();
        Stack stack = new Stack();
        Image background = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.ALMANAC_ZOMBIE_READY)));
        background.setScaling(Scaling.stretch);
        background.setFillParent(true);
        background.setTouchable(Touchable.disabled);
        stack.add(background);

        if (entry != null && entry.seen()) {
            Image portrait = new Image(new TextureRegionDrawable(assets.region(packetImageId(assets, entry.alias()))));
            portrait.setScaling(Scaling.fit);
            portrait.setTouchable(Touchable.disabled);
            Table art = new Table();
            art.setTouchable(Touchable.disabled);
            art.add(portrait).grow().pad(Value.percentHeight(0.09f, this));
            stack.add(art);
        }
        add(stack).grow();
    }

    public static String packetImageId(GameAssets assets, String alias) {
        if (alias == null || alias.isBlank()) {
            return MenuAssetIds.ALMANAC_ZOMBIE_EMPTY;
        }
        String mapped = CollectionCardLooks.zombiePacketSuffix(alias);
        if (mapped != null) {
            String id = MenuAssetIds.ZOMBIE_PACKET_PREFIX + mapped;
            if (assets.hasImage(id)) {
                return id;
            }
        }
        for (String suffix : packetSuffixes(alias)) {
            String id = MenuAssetIds.ZOMBIE_PACKET_PREFIX + suffix;
            if (assets.hasImage(id)) {
                return id;
            }
        }
        return MenuAssetIds.ALMANAC_ZOMBIE_EMPTY;
    }

    private static List<String> packetSuffixes(String alias) {
        String rest = alias.startsWith("Zombie") ? alias.substring("Zombie".length()) : alias;
        if (rest.isBlank()) {
            rest = alias;
        }
        List<String> suffixes = new ArrayList<>();
        if (rest.equalsIgnoreCase("Default") || rest.equalsIgnoreCase("Tutorial")) {
            suffixes.add("TUTORIAL");
        }
        String snake = camelToSnake(rest);
        suffixes.add(snake);
        suffixes.add(snake.replace("_", ""));
        suffixes.add(collapseWorld(snake));
        if (rest.toUpperCase(Locale.ROOT).startsWith("ARMOR")) {
            suffixes.add("TUTORIAL_" + snake.replace("_", ""));
            suffixes.add("MUMMY_" + snake.replace("_", ""));
            suffixes.add("TUTORIAL_" + snake);
        }
        String[] worlds = {
                "TUTORIAL_", "EGYPT_", "MUMMY_", "DARK_", "ICEAGE_", "BEACH_",
                "LOSTCITY_", "MODERN_", "EIGHTIES_"
        };
        for (String world : worlds) {
            suffixes.add(world + snake);
            suffixes.add(world + collapseWorld(snake));
            suffixes.add(world + rest.toUpperCase(Locale.ROOT));
        }
        return suffixes;
    }

    private static String camelToSnake(String value) {
        return value
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("([A-Za-z])([0-9])", "$1_$2")
                .toUpperCase(Locale.ROOT);
    }

    private static String collapseWorld(String snake) {
        int last = snake.lastIndexOf('_');
        if (last <= 0) {
            return snake.replace("_", "");
        }
        return snake.substring(0, last).replace("_", "") + snake.substring(last);
    }
}
