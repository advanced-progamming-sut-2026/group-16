package io.github.finalwave.network.match;

public final class MatchReactions {

    public static final String TEXT = "text";
    public static final String EMOJI = "emoji";
    public static final String STICKER = "sticker";

    private static final String[] MESSAGES = {
            "Good luck!",
            "Well played!",
            "Thanks!",
            "Good game!",
    };

    private static final String[] FACES = {"❤️", "🔥", "💀", "💪"};

    private static final String[] STICKERS = {"Chomper", "Jalapeno", "Bonk Choy"};

    private static final String[] STICKER_LABELS = {"Chomper", "Jalapeno", "Bonk Choy"};

    private static final String[] FULL_MESSAGES = {
            "Good luck!",
            "Well played!",
            "Thanks!",
            "Good game!",
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

    public static int textCount() {
        return MESSAGES.length;
    }

    public static int emojiCount() {
        return FACES.length;
    }

    public static int stickerCount() {
        return STICKERS.length;
    }

    public static int count() {
        return MESSAGES.length;
    }

    public static String describe(String kind, int index) {
        if (EMOJI.equals(kind)) {
            return FACES[clamp(index, FACES.length)];
        }
        if (STICKER.equals(kind)) {
            return STICKER_LABELS[clamp(index, STICKER_LABELS.length)];
        }
        return FULL_MESSAGES[clamp(index, FULL_MESSAGES.length)];
    }

    public static String hint(String kind, int index) {
        return describe(kind, index);
    }

    private static int clamp(int index, int length) {
        if (length <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(length - 1, index));
    }
}
