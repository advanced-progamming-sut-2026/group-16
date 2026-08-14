package io.github.finalwave.model.quest;

import io.github.finalwave.model.App;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.quest.condition.QuestConditions;
import io.github.finalwave.model.quest.reward.QuestReward;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class QuestFactory {

    private QuestFactory() {
    }

    public static List<Quest> createAllQuests() {
        return createAllQuests(0L);
    }

    public static List<Quest> createAllQuests(long seed) {
        Random random = new Random(seed);
        int sunAmount = randomChoice(random, List.of(3000, 4000, 5000));
        PlantRegistry registry = App.getInstance().getPlantRegistry();
        List<String> damagingPlants = damagingPlantNames(registry);
        if (damagingPlants.isEmpty()) {
            damagingPlants = List.of("Peashooter");
        }
        String plantType = randomChoice(random, damagingPlants);
        ChapterId chapter = randomChoice(random, List.of(ChapterId.values()));
        int maxLost = random.nextInt(6);
        PlantCategory familyOnly = randomChoice(random, List.of(PlantCategory.values()));
        PlantCategory familyBanned = randomChoice(random, List.of(PlantCategory.values()));
        int column = random.nextInt(9);
        int row = random.nextInt(5);
        int cross = random.nextInt(5);
        int mowerKills = randomChoice(random, List.of(10, 20, 30, 40, 50));

        return List.of(
                createDailySunCollector(sunAmount),
                createChapterHunter(chapter.getKey(), chapter.getDisplayName(), 50),
                createPlantProKiller(plantType),
                createOnlyCactusKiller(),
                createLowPlantLoss(maxLost),
                createFinishWithZeroSun(),
                createSpeedKill(10, 30.0),
                createExplosiveExpert(3),
                createSymmetryWin(),
                createFamilyExclusiveKills(familyOnly.name()),
                createFamilyBanned(familyBanned.name()),
                createNightPlantsInDayLevel(),
                createWinStreak(5),
                createNearVictory(10, Set.of()),
                createAsymmetryWin(),
                createLimitedSunProducers(3),
                createEmptyColumn(column),
                createEmptyRow(row),
                createEmptyCross(cross, cross),
                createLawnMowerKills(mowerKills));
    }

    public static Quest createDailySunCollector(int sunAmount) {
        return new Quest(
                "daily_sunblock",
                "Daily Sunblock",
                "Collect " + sunAmount + " of sun during one day",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.CollectSunCondition(sunAmount),
                QuestReward.coins(sunAmount / 100)
        );
    }

    public static Quest createChapterHunter(String chapterId, int targetCount) {
        ChapterId chapter = ChapterId.fromName(chapterId);
        String chapterName = chapter == null ? chapterId : chapter.getDisplayName();
        return createChapterHunter(chapterId, chapterName, targetCount);
    }

    private static Quest createChapterHunter(String chapterId, String chapterName, int targetCount) {
        return new Quest(
                "chapter_hunter",
                "Chapter Hunter",
                "Defeat " + targetCount + " zombies from " + chapterName,
                Quest.Category.MAIN,
                Quest.Priority.HIGH,
                new QuestConditions.KillZombiesInChapterCondition(chapterId, targetCount),
                QuestReward.seedPackets("ANY", 10)
        );
    }

    public static Quest createPlantProKiller(String plantType) {
        return new Quest(
                "pro_plant_player",
                "Pro Plant Player",
                "Kill 10 zombies using only a specific plant",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.KillOnlyWithPlantCondition(plantType, 10),
                QuestReward.randomPlantUnlock()
        );
    }

    public static Quest createOnlyCactusKiller() {
        return new Quest(
                "only_cactus",
                "Only Cactus",
                "Kill 10 zombies using only Cactus",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.KillOnlyWithPlantCondition("Cactus", 10),
                QuestReward.diamonds(20)
        );
    }

    public static Quest createLowPlantLoss(int maxLost) {
        int reward = 20 - maxLost;
        return new Quest(
                "economical_herbivore",
                "Economical Herbivore",
                "Win a level losing no more than " + maxLost + " plants",
                Quest.Category.MAIN,
                Quest.Priority.HIGH,
                new QuestConditions.LowPlantLossCondition(maxLost),
                QuestReward.seedPackets("ANY", Math.max(1, reward))
        );
    }

    public static Quest createFinishWithZeroSun() {
        return new Quest(
                "defense_master",
                "Defense Master",
                "Finish a level with exactly 0 suns",
                Quest.Category.EPIC_CHALLENGE,
                Quest.Priority.CRITICAL,
                new QuestConditions.FinishWithZeroSunCondition(),
                QuestReward.diamonds(200)
        );
    }

    public static Quest createSpeedKill(int kills, double timeLimitSeconds) {
        return new Quest(
                "quick_reaction",
                "Quick Reaction",
                "Kill " + kills + " zombies in less than "
                        + formatSeconds(timeLimitSeconds) + " seconds from the first wave",
                Quest.Category.MAIN,
                Quest.Priority.MEDIUM,
                new QuestConditions.SpeedKillCondition(kills, timeLimitSeconds),
                QuestReward.coins(500)
        );
    }

    public static Quest createExplosiveExpert(int count) {
        return new Quest(
                "pro_demolition",
                "Pro Demolition",
                "Use " + count + " explosive plants in one level",
                Quest.Category.DAILY,
                Quest.Priority.LOW,
                new QuestConditions.PlantExplosivesCondition(count),
                QuestReward.coins(100)
        );
    }

    public static Quest createSymmetryWin() {
        return new Quest(
                "symmetry",
                "Symmetry",
                "Garden must be symmetrical at the end",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.SymmetricBoardCondition(),
                QuestReward.coins(500)
        );
    }

    public static Quest createFamilyExclusiveKills(String family) {
        return new Quest(
                "family_massacre",
                "Family Massacre",
                "Use only " + displayFamily(family) + " plants to kill zombies",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.FamilyOnlyKillsCondition(family),
                QuestReward.coins(1000)
        );
    }

    public static Quest createFamilyBanned(String family) {
        return new Quest(
                "flourish_in_limits",
                "Flourish in Limits",
                "Win without using any plant from " + displayFamily(family),
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.NoFamilyPlantedCondition(family),
                QuestReward.diamonds(100)
        );
    }

    public static Quest createNightPlantsInDayLevel() {
        return new Quest(
                "night_or_morning",
                "Night or Morning",
                "Finish a day level with night plants (mushrooms)",
                Quest.Category.EPIC_CHALLENGE,
                Quest.Priority.HIGH,
                new QuestConditions.NightPlantsInDayLevelCondition(),
                QuestReward.diamonds(20)
        );
    }

    public static Quest createWinStreak(int n) {
        return new Quest(
                "win_streak",
                "Win Streak",
                "Win " + n + " levels in a row on highest difficulty",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.WinStreakCondition(n),
                QuestReward.coins(5000)
        );
    }

    public static Quest createNearVictory(int kills, Set<Integer> rowsWithMowers) {
        return new Quest(
                "almost_won",
                "Almost Won",
                "Kill " + kills + " zombies in the first column of a row without a lawnmower",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.KillInFirstColumnNoMowerCondition(kills, rowsWithMowers),
                QuestReward.coins(300)
        );
    }

    public static Quest createAsymmetryWin() {
        return new Quest(
                "what_ocd",
                "What OCD?",
                "Win a level with completely asymmetrical garden (except middle row)",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.AsymmetricBoardCondition(),
                QuestReward.coins(800)
        );
    }

    public static Quest createLimitedSunProducers(int max) {
        return new Quest(
                "cloudy_day",
                "Cloudy Day",
                "Beat a level using only " + max + " sun-producing plants",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.LimitedSunProducersCondition(max),
                QuestReward.diamonds(10)
        );
    }

    public static Quest createEmptyColumn(int column) {
        return new Quest(
                "one_column_less",
                "One Column Less",
                "Win a level without planting in the " + (column + 1) + " column",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.EmptyColumnCondition(column),
                QuestReward.diamonds(10)
        );
    }

    public static Quest createEmptyRow(int row) {
        return new Quest(
                "defenseless_row",
                "Defenseless Row",
                "Win a level without planting in the " + (row + 1) + " row",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.EmptyRowCondition(row),
                QuestReward.diamonds(20)
        );
    }

    public static Quest createEmptyCross(int row, int col) {
        return new Quest(
                "defenseless_cross",
                "Defenseless Cross",
                "Win a level with the " + (row + 1) + " row and column empty",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.EmptyCrossCondition(row, col),
                QuestReward.diamonds(25)
        );
    }

    public static Quest createLawnMowerKills(int n) {
        return new Quest(
                "lawnmower_time",
                "Lawnmower Time",
                "Kill at least " + n + " zombies with lawnmowers",
                Quest.Category.EPIC_CHALLENGE,
                Quest.Priority.MEDIUM,
                new QuestConditions.LawnMowerKillsCondition(n),
                QuestReward.diamonds(n)
        );
    }

    private static List<String> damagingPlantNames(PlantRegistry registry) {
        if (registry == null) {
            return List.of();
        }
        return registry.getAllDefinitions().stream()
                .filter(definition -> definition.getDamage() > 0)
                .map(PlantDefinition::getName)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private static <T> T randomChoice(Random random, List<T> choices) {
        return choices.get(random.nextInt(choices.size()));
    }

    private static String displayFamily(String family) {
        return family.toLowerCase().replace('_', ' ');
    }

    private static String formatSeconds(double seconds) {
        if (seconds == Math.rint(seconds)) {
            return Integer.toString((int) seconds);
        }
        return Double.toString(seconds);
    }

}
