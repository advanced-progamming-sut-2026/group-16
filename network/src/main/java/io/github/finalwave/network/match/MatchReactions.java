package io.github.finalwave.network.match;

public final class MatchReactions {

    public static final String TEXT = "text";
    public static final String EMOJI = "emoji";
    public static final String STICKER = "sticker";

    private static final String[] MESSAGES = {
            "Good luck!",
            "Too easy!",
            "Well played.",
    };

    private static final String[] FACES = {"Nice one!", "Brains!", "Boom!"};

    private static final String[] STICKERS = {"Sunflower", "Peashooter", "Normal"};

    private static final String[] STICKER_LABELS = {
            "Dancing Sunflower", "Cheering Peashooter", "Taunting Zombie",
    };

    private static final String[] FULL_MESSAGES = {
            "Good luck out there!",
            "Is that the best you have?",
            "Well played.",
    };

    private MatchReactions() {
    }

    public static String[] messages() {
        return MESSAGES.clone();
    }

    public static String[] faces() {
        return FACES.clone();
    }

    public static String[] stickers() {
        return STICKERS.clone();
    }

    public static String[] stickerLabels() {
        return STICKER_LABELS.clone();
    }

    public static int count() {
        return MESSAGES.length;
    }

    public static String describe(String kind, int index) {
        int safe = Math.max(0, Math.min(count() - 1, index));
        if (EMOJI.equals(kind)) {
            return FACES[safe];
        }
        if (STICKER.equals(kind)) {
            return STICKER_LABELS[safe];
        }
        return FULL_MESSAGES[safe];
    }

    public static String hint(String kind, int index) {
        return describe(kind, index);
    }
}
