package io.github.finalwave.model.adventure;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class AdventureRegistry {

    private static final AdventureRegistry INSTANCE = new AdventureRegistry();

    private final Map<ChapterId, ChapterConfig> chapters = new EnumMap<>(ChapterId.class);

    private AdventureRegistry() {
        chapters.put(ChapterId.ANCIENT_EGYPT, egypt());
        chapters.put(ChapterId.FROSTBITE_CAVES, frostbite());
        chapters.put(ChapterId.BIG_WAVE_BEACH, beach());
        chapters.put(ChapterId.DARK_AGES, darkAges());
    }

    public static AdventureRegistry getInstance() {
        return INSTANCE;
    }

    public ChapterConfig getChapter(ChapterId id) {
        return chapters.get(id);
    }

    public ChapterConfig getChapterByName(String name) {
        ChapterId id = ChapterId.fromName(name);
        return id == null ? null : chapters.get(id);
    }

    public List<ChapterConfig> getAllChapters() {
        return List.copyOf(chapters.values());
    }

    private static ChapterConfig egypt() {
        List<String> zombies = List.of(
                "ZombieDefault", "ZombieArmor1", "ZombieArmor2",
                "ZombieRa", "ZombieExplorer", "ZombieTombRaiser");
        return new ChapterConfig(ChapterId.ANCIENT_EGYPT, ChapterRules.ancientEgypt(), List.of(
                LevelConfig.normal(1, 3, 50, 300, zombies),
                LevelConfig.special(2, LevelType.CONVEYOR_BELT, "conveyor", zombies),
                LevelConfig.special(3, LevelType.LOCKED_PLANTS, "locked", zombies),
                LevelConfig.boss(4)));
    }

    private static ChapterConfig frostbite() {
        List<String> zombies = List.of(
                "ZombieDefault", "ZombieArmor1", "ZombieArmor2",
                "ZombieIceAgeDodo", "ZombieIceAgeHunter", "ZombieIceAgeTroglobite");
        return new ChapterConfig(ChapterId.FROSTBITE_CAVES, ChapterRules.frostbiteCaves(), List.of(
                LevelConfig.normal(1, 3, 50, 350, zombies),
                LevelConfig.special(2, LevelType.SAVE_OUR_SEEDS, "sos", zombies),
                LevelConfig.special(3, LevelType.TIMED_WAR, "timed", zombies),
                LevelConfig.boss(4)));
    }

    private static ChapterConfig beach() {
        List<String> zombies = List.of(
                "ZombieDefault", "ZombieArmor1", "ZombieArmor2",
                "ZombieBeachFisherman", "ZombieBeachOctopus", "ZombieBeachSnorkel");
        return new ChapterConfig(ChapterId.BIG_WAVE_BEACH, ChapterRules.bigWaveBeach(), List.of(
                LevelConfig.normal(1, 3, 75, 400, zombies),
                LevelConfig.special(2, LevelType.NIGHT_OPS, "night_ops", zombies),
                LevelConfig.special(3, LevelType.DEAD_LINE, "deadline", zombies),
                LevelConfig.boss(4)));
    }

    private static ChapterConfig darkAges() {
        List<String> zombies = List.of(
                "ZombieDefault", "ZombieArmor1", "ZombieArmor2", "ZombieDarkArmor3",
                "ZombieDarkJuggler", "ZombieWizard", "ZombieDarkKing", "ZombieDarkImpDragon");
        return new ChapterConfig(ChapterId.DARK_AGES, ChapterRules.darkAges(), List.of(
                LevelConfig.normal(1, 3, 50, 400, zombies),
                LevelConfig.special(2, LevelType.LOVE_YOUR_PLANTS, "love", zombies),
                LevelConfig.special(3, LevelType.PLANT_WHAT_YOU_GET, "plant_what", zombies),
                LevelConfig.boss(4)));
    }
}
