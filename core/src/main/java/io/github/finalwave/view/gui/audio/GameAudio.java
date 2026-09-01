package io.github.finalwave.view.gui.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import io.github.finalwave.model.adventure.ChapterId;


public final class GameAudio implements Disposable {
    private static final String TAG = "GameAudio";

    private enum Track {
        MENU,
        BATTLE,
        ZEN,
        ZOMBOSS
    }

    private final Music menu;
    private final Music battleEgypt;
    private final Music battleDark;
    private final Music battleBeach;
    private final Music battleFrost;
    private final Music zenGarden;
    private final Music zomboss1;
    private final Music zomboss2;
    private final Music zomboss3;

    private final Sound throwSfx;
    private final Sound plantSfx;
    private final Sound hitSfx;
    private final Sound explosionSfx;
    private final Sound shovelSfx;
    private final Sound winSfx;
    private final Sound lossSfx;
    private final Sound mowerSfx;
    private final Sound waveAlertSfx;
    private final Sound collectSfx;
    private final Sound zombieEat1Sfx;
    private final Sound zombieEat2Sfx;
    private final Sound plantWaterSfx;
    private final Sound bowlingImpactSfx;
    private final Sound plantBowlingSfx;
    private final Sound firePeaSfx;
    private final Sound kernelSfx;

    private Music current;
    private Track currentTrack;
    private boolean zombieEatToggle;

    public GameAudio() {
        menu = loadMusic(SoundIds.MENU_BGM);
        battleEgypt = loadMusic(SoundIds.BATTLE_EGYPT);
        battleDark = loadMusic(SoundIds.BATTLE_DARK);
        battleBeach = loadMusic(SoundIds.BATTLE_BEACH);
        battleFrost = loadMusic(SoundIds.BATTLE_FROST);
        zenGarden = loadMusic(SoundIds.ZEN_GARDEN);
        zomboss1 = loadMusic(SoundIds.ZOMBOSS_PHASE_1);
        zomboss2 = loadMusic(SoundIds.ZOMBOSS_PHASE_2);
        zomboss3 = loadMusic(SoundIds.ZOMBOSS_PHASE_3);

        throwSfx = loadSound(SoundIds.THROW);
        plantSfx = loadSound(SoundIds.PLANT);
        hitSfx = loadSound(SoundIds.HIT);
        explosionSfx = loadSound(SoundIds.EXPLOSION);
        shovelSfx = loadSound(SoundIds.SHOVEL);
        winSfx = loadSound(SoundIds.WIN);
        lossSfx = loadSound(SoundIds.LOSS);
        mowerSfx = loadSound(SoundIds.MOWER);
        waveAlertSfx = loadSound(SoundIds.WAVE_ALERT);
        collectSfx = loadSound(SoundIds.COLLECT);
        zombieEat1Sfx = loadSound(SoundIds.ZOMBIE_EAT_1);
        zombieEat2Sfx = loadSound(SoundIds.ZOMBIE_EAT_2);
        plantWaterSfx = loadSound(SoundIds.PLANT_WATER);
        bowlingImpactSfx = loadSound(SoundIds.BOWLING_IMPACT);
        plantBowlingSfx = loadSound(SoundIds.PLANT_BOWLING);
        firePeaSfx = loadSound(SoundIds.FIRE_PEA);
        kernelSfx = loadSound(SoundIds.KERNEL);
    }

    public void playMenu() {
        play(Track.MENU, menu);
    }

    public void playBattle() {
        playBattle(null);
    }

    public void playBattle(ChapterId chapterId) {
        play(Track.BATTLE, battleMusicFor(chapterId));
    }

    public void playZenGarden() {
        play(Track.ZEN, zenGarden);
    }

    public void playZomboss(int phase) {
        play(Track.ZOMBOSS, zombossMusicFor(phase));
    }

    public void playThrow() {
        playSfx(throwSfx);
    }

    public void playHit() {
        playSfx(hitSfx);
    }

    public void playExplosion() {
        playSfx(explosionSfx);
    }

    public void playPlant() {
        playSfx(plantSfx);
    }

    public void playShovel() {
        playSfx(shovelSfx);
    }

    public void playWin() {
        playSfx(winSfx);
    }

    public void playLoss() {
        playSfx(lossSfx);
    }

    public void playMower() {
        playSfx(mowerSfx);
    }

    public void playWaveAlert() {
        playSfx(waveAlertSfx, 1f);
    }

    public void playCollect() {
        playSfx(collectSfx, 1f);
    }

    public void playZombieEat() {
        playSfx(zombieEatToggle ? zombieEat2Sfx : zombieEat1Sfx);
        zombieEatToggle = !zombieEatToggle;
    }

    public void playPlantWater() {
        playSfx(plantWaterSfx);
    }

    public void playBowlingImpact() {
        playSfx(bowlingImpactSfx);
    }

    public void playPlantBowling() {
        playSfx(plantBowlingSfx);
    }

    public void playFirePea() {
        playSfx(firePeaSfx);
    }

    public void playKernel() {
        playSfx(kernelSfx);
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
        disposeMusic(battleEgypt);
        disposeMusic(battleDark);
        disposeMusic(battleBeach);
        disposeMusic(battleFrost);
        disposeMusic(zenGarden);
        disposeMusic(zomboss1);
        disposeMusic(zomboss2);
        disposeMusic(zomboss3);
        disposeSound(throwSfx);
        disposeSound(plantSfx);
        disposeSound(hitSfx);
        disposeSound(explosionSfx);
        disposeSound(shovelSfx);
        disposeSound(winSfx);
        disposeSound(lossSfx);
        disposeSound(mowerSfx);
        disposeSound(waveAlertSfx);
        disposeSound(collectSfx);
        disposeSound(zombieEat1Sfx);
        disposeSound(zombieEat2Sfx);
        disposeSound(plantWaterSfx);
        disposeSound(bowlingImpactSfx);
        disposeSound(plantBowlingSfx);
        disposeSound(firePeaSfx);
        disposeSound(kernelSfx);
    }

    private Music battleMusicFor(ChapterId chapterId) {
        if (chapterId == null) {
            return battleEgypt;
        }
        return switch (chapterId) {
            case DARK_AGES -> battleDark != null ? battleDark : battleEgypt;
            case BIG_WAVE_BEACH -> battleBeach != null ? battleBeach : battleEgypt;
            case FROSTBITE_CAVES -> battleFrost != null ? battleFrost : battleEgypt;
            case ANCIENT_EGYPT -> battleEgypt;
        };
    }

    private Music zombossMusicFor(int phase) {
        return switch (Math.max(1, Math.min(3, phase))) {
            case 1 -> zomboss1 != null ? zomboss1 : battleEgypt;
            case 2 -> zomboss2 != null ? zomboss2 : battleEgypt;
            default -> zomboss3 != null ? zomboss3 : battleEgypt;
        };
    }

    private void play(Track track, Music music) {
        if (currentTrack == track && current == music && current != null && current.isPlaying()) {
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
        playSfx(sound, 0.9f + (float) (Math.random() * 0.2f));
    }

    private void playSfx(Sound sound, float pitch) {
        if (sound == null) {
            return;
        }
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
