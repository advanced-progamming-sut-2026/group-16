package io.github.finalwave.view.gui.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;


public final class GameAudioSettings {
    public static final int MAX = 100;
    private static final String PREFS = "finalwave-audio";
    private static final String MUSIC = "musicVolume";
    private static final String SFX = "sfxVolume";

    private GameAudioSettings() {
    }

    public static int musicVolume() {
        return clamp(prefs().getInteger(MUSIC, MAX));
    }

    public static int sfxVolume() {
        return clamp(prefs().getInteger(SFX, MAX));
    }

    public static void setMusicVolume(int volume) {
        prefs().putInteger(MUSIC, clamp(volume));
        prefs().flush();
    }

    public static void setSfxVolume(int volume) {
        prefs().putInteger(SFX, clamp(volume));
        prefs().flush();
    }

    public static float musicGain() {
        return musicVolume() / (float) MAX;
    }

    public static float sfxGain() {
        return sfxVolume() / (float) MAX;
    }

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREFS);
    }

    private static int clamp(int volume) {
        return Math.max(0, Math.min(MAX, volume));
    }
}
