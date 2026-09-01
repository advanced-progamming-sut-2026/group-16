package io.github.finalwave.view.gui.audio;

import io.github.finalwave.model.adventure.ChapterId;


public final class SoundIds {
    public static final String MENU_BGM = "sounds/bgm/1-01. Title Screen.mp3";
    public static final String BATTLE_EGYPT =
            "sounds/bgm/Ancient_Egypt_First_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time.mp3";
    public static final String BATTLE_DARK =
            "sounds/bgm/Dark_Ages_Final_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3";
    public static final String BATTLE_BEACH =
            "sounds/bgm/Big_Wave_Beach_First_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time.mp3";
    public static final String BATTLE_FROST =
            "sounds/bgm/Frostbite_Caves_Final_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time.mp3";
    public static final String ZEN_GARDEN = "sounds/bgm/zen garden.mp3";
    public static final String ZOMBOSS_PHASE_1 =
            "sounds/bgm/Zomboss_Phase_1_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3";
    public static final String ZOMBOSS_PHASE_2 =
            "sounds/bgm/Zomboss_Phase_2_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3";
    public static final String ZOMBOSS_PHASE_3 =
            "sounds/bgm/Zomboss_Phase_3_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3";

    public static final String THROW = "sounds/sfx/plant.mp3";
    public static final String PLANT = "sounds/sfx/plant.mp3";
    public static final String HIT = "sounds/sfx/zombie hit.mp3";
    public static final String EXPLOSION = "sounds/sfx/explosion.mp3";
    public static final String SHOVEL = "sounds/sfx/shovel.mp3";
    public static final String WIN = "sounds/sfx/win audio.mp3";
    public static final String LOSS = "sounds/sfx/loss audio.mp3";
    public static final String MOWER = "sounds/sfx/lawn mower.mp3";
    public static final String WAVE_ALERT = "sounds/sfx/zombies are comming.mp3";
    public static final String COLLECT = "sounds/sfx/1-36. SFX floop.mp3";
    public static final String ZOMBIE_EAT_1 = "sounds/sfx/zombie eat 1.mp3";
    public static final String ZOMBIE_EAT_2 = "sounds/sfx/zombie eat 2.mp3";
    public static final String PLANT_WATER = "sounds/sfx/plant water.mp3";
    public static final String BOWLING_IMPACT = "sounds/sfx/1-14. SFX bowlingimpact.mp3";
    public static final String PLANT_BOWLING = "sounds/sfx/plant bowling.mp3";
    public static final String FIRE_PEA = "sounds/sfx/1-35. SFX firepea.mp3";
    public static final String KERNEL = "sounds/sfx/1-53. SFX kernelpult2.mp3";

    private SoundIds() {
    }

    public static String battleBgm(ChapterId chapterId) {
        if (chapterId == null) {
            return BATTLE_EGYPT;
        }
        return switch (chapterId) {
            case DARK_AGES -> BATTLE_DARK;
            case BIG_WAVE_BEACH -> BATTLE_BEACH;
            case FROSTBITE_CAVES -> BATTLE_FROST;
            case ANCIENT_EGYPT -> BATTLE_EGYPT;
        };
    }

    public static String zombossBgm(int phase) {
        return switch (Math.max(1, Math.min(3, phase))) {
            case 1 -> ZOMBOSS_PHASE_1;
            case 2 -> ZOMBOSS_PHASE_2;
            default -> ZOMBOSS_PHASE_3;
        };
    }
}
