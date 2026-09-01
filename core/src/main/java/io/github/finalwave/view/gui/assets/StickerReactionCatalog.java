package io.github.finalwave.view.gui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import io.github.finalwave.network.match.MatchReactions;
import io.github.finalwave.view.gui.widget.StickerAtlasActor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StickerReactionCatalog {
    public static final float FRAME_DURATION = 0.08f;
    private static final String TAG = "StickerReactionCatalog";

    private static final String[] ATLAS_BASES = {
            "STICKERS/chomper_sticker",
            "STICKERS/jalapeno_sticker",
            "STICKERS/bonkchoy_sticker",
    };

    private static final Pattern TILE_PATTERN = Pattern.compile("tile(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROW_COL_PATTERN = Pattern.compile("row-(\\d+)-column-(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOUNDS_PATTERN = Pattern.compile(
            "bounds:\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final Map<Integer, CachedSticker> CACHE = new HashMap<>();

    private StickerReactionCatalog() {
    }

    public static int count() {
        return ATLAS_BASES.length;
    }

    public static String atlasBaseFor(int index) {
        return ATLAS_BASES[clamp(index)];
    }

    public static StickerAtlasActor createActor(GameAssets assets, int index, float size) {
        Animation<TextureRegion> animation = animationFor(assets, index);
        if (animation == null) {
            return null;
        }
        StickerAtlasActor actor = new StickerAtlasActor(animation);
        actor.setSize(size, size);
        actor.setTouchable(Touchable.disabled);
        return actor;
    }

    static Animation<TextureRegion> animationFor(GameAssets assets, int index) {
        CachedSticker cached = loadCached(assets, clamp(index));
        if (cached == null || cached.frames.isEmpty()) {
            return null;
        }
        Array<TextureRegion> frameArray = new Array<>(cached.frames.size());
        for (TextureRegion frame : cached.frames) {
            frameArray.add(frame);
        }
        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, frameArray);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    static List<AtlasRect> parseAtlasBounds(String atlasText) {
        List<AtlasRect> regions = new ArrayList<>();
        if (atlasText == null || atlasText.isBlank()) {
            return regions;
        }
        String currentName = null;
        for (String rawLine : atlasText.split("\\R")) {
            String trimmed = rawLine.stripTrailing().trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher bounds = BOUNDS_PATTERN.matcher(trimmed);
            if (bounds.matches() && currentName != null) {
                regions.add(new AtlasRect(
                        currentName,
                        Integer.parseInt(bounds.group(1)),
                        Integer.parseInt(bounds.group(2)),
                        Integer.parseInt(bounds.group(3)),
                        Integer.parseInt(bounds.group(4))));
                continue;
            }
            if (isPageProperty(trimmed)) {
                continue;
            }
            currentName = trimmed;
        }
        return regions;
    }

    static int atlasYToTextureY(int atlasY, int imageHeight, int regionHeight) {
        return imageHeight - atlasY - regionHeight;
    }

    static int tileOrder(String name) {
        if (name == null) {
            return Integer.MAX_VALUE;
        }
        Matcher matcher = TILE_PATTERN.matcher(name);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return Integer.MAX_VALUE;
    }

    static int[] rowColOrder(String name) {
        if (name == null) {
            return new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE};
        }
        Matcher matcher = ROW_COL_PATTERN.matcher(name);
        if (matcher.find()) {
            return new int[] {Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
        }
        return new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE};
    }

    static void sortRects(List<AtlasRect> regions, int index) {
        sortByName(regions, index, rect -> rect.name);
    }

    private static CachedSticker loadCached(GameAssets assets, int index) {
        CachedSticker cached = CACHE.get(index);
        if (cached != null) {
            return cached;
        }
        String base = ATLAS_BASES[index];
        String[] pathBases = {base, "assets/" + base};
        for (String pathBase : pathBases) {
            for (FileHandle pngFile : fileCandidates(assets, pathBase + ".png")) {
                for (FileHandle atlasFile : fileCandidates(assets, pathBase + ".atlas")) {
                    try {
                        CachedSticker loaded = loadFromHandles(pngFile, atlasFile, index);
                        if (loaded != null) {
                            CACHE.put(index, loaded);
                            return loaded;
                        }
                    } catch (RuntimeException e) {
                        error("Failed " + pathBase + " via " + describe(pngFile) + " / "
                                + describe(atlasFile) + ": " + e.getMessage());
                    }
                }
            }
        }
        error("Missing sticker files for " + base + ".png / " + base + ".atlas");
        return null;
    }

    private static CachedSticker loadFromHandles(FileHandle pngFile, FileHandle atlasFile, int index) {
        Texture texture = new Texture(pngFile);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        List<TextureRegion> frames = framesFromAtlasData(texture, atlasFile, index);
        if (frames == null || frames.isEmpty()) {
            frames = framesFromBounds(texture, atlasFile, index);
        }
        if (frames == null || frames.isEmpty()) {
            texture.dispose();
            return null;
        }
        return new CachedSticker(texture, frames);
    }

    private static List<TextureRegion> framesFromAtlasData(Texture texture, FileHandle atlasFile, int index) {
        try {
            TextureAtlas.TextureAtlasData data =
                    new TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false);
            for (TextureAtlas.TextureAtlasData.Page page : data.getPages()) {
                page.texture = texture;
            }
            TextureAtlas atlas = new TextureAtlas(data);
            List<TextureAtlas.AtlasRegion> sorted = new ArrayList<>();
            for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
                sorted.add(region);
            }
            sortByName(sorted, index, region -> region.name);
            List<TextureRegion> frames = new ArrayList<>(sorted.size());
            for (TextureAtlas.AtlasRegion region : sorted) {
                frames.add(new TextureRegion(region));
            }
            return frames;
        } catch (RuntimeException e) {
            error("TextureAtlasData failed for " + describe(atlasFile) + ": " + e.getMessage());
            return null;
        }
    }

    private static List<TextureRegion> framesFromBounds(Texture texture, FileHandle atlasFile, int index) {
        try {
            List<AtlasRect> rects = parseAtlasBounds(atlasFile.readString("UTF-8"));
            sortRects(rects, index);
            if (rects.isEmpty()) {
                return List.of();
            }
            int imageHeight = texture.getHeight();
            List<TextureRegion> frames = new ArrayList<>(rects.size());
            for (AtlasRect rect : rects) {
                int y = atlasYToTextureY(rect.y, imageHeight, rect.height);
                frames.add(new TextureRegion(texture, rect.x, y, rect.width, rect.height));
            }
            return frames;
        } catch (RuntimeException e) {
            error("Bounds parse failed for " + describe(atlasFile) + ": " + e.getMessage());
            return null;
        }
    }

    private static List<FileHandle> fileCandidates(GameAssets assets, String path) {
        List<FileHandle> candidates = new ArrayList<>();
        if (Gdx.files != null) {
            candidates.add(Gdx.files.internal(path));
            candidates.add(Gdx.files.local(path));
        }
        if (assets != null && assets.root() != null) {
            candidates.add(assets.root().child(path));
        }
        return candidates;
    }

    private static <T> void sortByName(List<T> items, int index, java.util.function.Function<T, String> name) {
        if (index == 0) {
            items.sort(Comparator.comparingInt(item -> tileOrder(name.apply(item))));
            return;
        }
        items.sort(Comparator
                .comparingInt((T item) -> rowColOrder(name.apply(item))[0])
                .thenComparingInt(item -> rowColOrder(name.apply(item))[1]));
    }

    private static boolean isPageProperty(String trimmed) {
        if (trimmed.endsWith(".png") || trimmed.endsWith(".jpg") || trimmed.endsWith(".jpeg")) {
            return true;
        }
        int colon = trimmed.indexOf(':');
        if (colon <= 0) {
            return false;
        }
        String key = trimmed.substring(0, colon).trim().toLowerCase();
        return key.equals("size")
                || key.equals("format")
                || key.equals("filter")
                || key.equals("repeat")
                || key.equals("pma");
    }

    private static String describe(FileHandle file) {
        return file == null ? "null" : file.path();
    }

    private static void error(String message) {
        if (Gdx.app != null) {
            Gdx.app.error(TAG, message);
        }
    }

    private static int clamp(int index) {
        int max = Math.min(ATLAS_BASES.length, MatchReactions.stickerCount());
        if (max <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(max - 1, index));
    }

    static final class AtlasRect {
        final String name;
        final int x;
        final int y;
        final int width;
        final int height;

        AtlasRect(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static final class CachedSticker {
        final Texture texture;
        final List<TextureRegion> frames;

        CachedSticker(Texture texture, List<TextureRegion> frames) {
            this.texture = texture;
            this.frames = frames;
        }
    }
}
