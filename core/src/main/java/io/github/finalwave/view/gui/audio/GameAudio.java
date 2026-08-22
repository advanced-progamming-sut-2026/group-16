package io.github.finalwave.view.gui.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;


public final class GameAudio implements Disposable {
    private static final String TAG = "GameAudio";

    private enum Track {
        MENU,
        BATTLE
    }

    private final Music menu;
    private final Music battle;
    private final Sound throwSfx;
    private final Sound hitSfx;
    private Music current;
    private Track currentTrack;

    public GameAudio() {
        this.menu = loadMusic(SoundIds.MENU_BGM);
        this.battle = loadMusic(SoundIds.BATTLE_BGM);
        this.throwSfx = loadSound(SoundIds.THROW);
        this.hitSfx = loadSound(SoundIds.HIT);
    }

    public void playMenu() {
        play(Track.MENU, menu);
    }

    public void playBattle() {
        play(Track.BATTLE, battle);
    }

    public void playThrow() {
        playSfx(throwSfx);
    }

    public void playHit() {
        playSfx(hitSfx);
    }

    public void setMusicVolume(int volume) {
        GameAudioSettings.setMusicVolume(volume);
        applyMusicVolume();
    }

    public void setSfxVolume(int volume) {
        GameAudioSettings.setSfxVolume(volume);
    }

    @Override
    public void dispose() {
        stopCurrent();
        disposeMusic(menu);
        disposeMusic(battle);
        disposeSound(throwSfx);
        disposeSound(hitSfx);
    }

    private void play(Track track, Music music) {
        if (currentTrack == track && current != null && current.isPlaying()) {
            applyMusicVolume();
            return;
        }
        stopCurrent();
        currentTrack = track;
        current = music;
        if (current == null) {
            return;
        }
        current.setLooping(true);
        applyMusicVolume();
        current.play();
    }

    private void playSfx(Sound sound) {
        if (sound == null) {
            return;
        }
        float pitch = 0.9f + (float) (Math.random() * 0.2f);
        sound.play(GameAudioSettings.sfxGain(), pitch, 0f);
    }

    private void applyMusicVolume() {
        if (current != null) {
            current.setVolume(GameAudioSettings.musicGain());
        }
    }

    private void stopCurrent() {
        if (current != null) {
            current.stop();
        }
        current = null;
        currentTrack = null;
    }

    private static Music loadMusic(String path) {
        FileHandle file = Gdx.files.local(path);
        if (!file.exists()) {
            Gdx.app.error(TAG, "Missing music: " + path);
            return null;
        }
        try {
            Music music = Gdx.audio.newMusic(file);
            music.setLooping(true);
            music.setVolume(GameAudioSettings.musicGain());
            return music;
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "Failed to load music: " + path, e);
            return null;
        }
    }

    private static Sound loadSound(String path) {
        FileHandle file = Gdx.files.local(path);
        if (!file.exists()) {
            Gdx.app.error(TAG, "Missing sound: " + path);
            return null;
        }
        try {
            return Gdx.audio.newSound(file);
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "Failed to load sound: " + path, e);
            return null;
        }
    }

    private static void disposeMusic(Music music) {
        if (music != null) {
            music.dispose();
        }
    }

    private static void disposeSound(Sound sound) {
        if (sound != null) {
            sound.dispose();
        }
    }
}
