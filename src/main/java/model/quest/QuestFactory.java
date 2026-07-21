package model.quest;

import model.adventure.ChapterId;
import model.quest.condition.QuestConditions;
import model.quest.reward.QuestReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class QuestFactory {

    private QuestFactory() {}
    
    public static List<Quest> createAllQuests() {
        List<Quest> all = new ArrayList<>();

        // --- DAILY ---
        all.add(createDailySunCollector(3000));
        all.add(createPlantProKiller("peashooter"));       // example plant
        all.add(createOnlyCactusKiller());
        all.add(createExplosiveExpert(3));
        all.add(createSymmetryWin());
        all.add(createFamilyExclusiveKills("SHOOTER"));    // parameterized variant
        all.add(createFamilyBanned("SHROOM"));             // parameterized variant
        all.add(createWinStreak(5));
        all.add(createNearVictory(10, Set.of()));          // rowsWithMowers injected at runtime
        all.add(createAsymmetryWin());
        all.add(createLimitedSunProducers(3));
        all.add(createEmptyColumn(1));                     // example: column 1
        all.add(createEmptyRow(2));                        // example: row 2
        all.add(createEmptyCross(2, 3));
        all.add(createLawnMowerKills(10));

        // --- MAIN ---
        all.add(createChapterHunter(ChapterId.ANCIENT_EGYPT.getKey(), 50));
        all.add(createLowPlantLoss(2));

        // --- EPIC CHALLENGE ---
        all.add(createFinishWithZeroSun());
        all.add(createSpeedKill(10, 30.0));
        all.add(createNightPlantsInDayLevel());

        return all;
    }

    // -----------------------------------------------------------------------
    // Individual factory methods (one per quest type in the xlsx)
    // -----------------------------------------------------------------------

    /** آفتاب گیر روزانه  Collect sunAmount sun in one day. Valid targets: 3000, 4000, 5000 */
    public static Quest createDailySunCollector(int sunAmount) {
        return new Quest(
                "daily_sun_" + sunAmount,
                "آفتاب گیر روزانه (" + sunAmount + ")",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.CollectSunCondition(sunAmount),
                QuestReward.coins(sunAmount / 100)
        );
    }

    /** شکارچی chapter  Kill 50 zombies from a specific chapter. One quest per chapter. */
    public static Quest createChapterHunter(String chapterId, int targetCount) {
        return new Quest(
                "chapter_hunter_" + chapterId,
                "شکارچی " + chapterId,
                Quest.Category.MAIN,
                Quest.Priority.HIGH,
                new QuestConditions.KillZombiesInChapterCondition(chapterId, targetCount),
                QuestReward.seedPackets("ANY", 10)
        );
    }

    /** plant باز حرفه‌ای  Kill 10 zombies using only the given plant. */
    public static Quest createPlantProKiller(String plantType) {
        return new Quest(
                "plant_pro_" + plantType,
                "حرفه‌ای " + plantType,
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.KillOnlyWithPlantCondition(plantType, 10),
                QuestReward.randomPlantUnlock()
        );
    }

    /** only cactus  Kill 10 zombies with only cactus. */
    public static Quest createOnlyCactusKiller() {
        return new Quest(
                "only_cactus",
                "only cactus",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.KillOnlyWithPlantCondition("cactus", 10),
                QuestReward.diamonds(20)
        );
    }

    /** گیاه خوار اقتصادی  Win losing at most n plants. Valid n: 0-5 */
    public static Quest createLowPlantLoss(int maxLost) {
        int reward = 20 - maxLost; // 20-n seed packets per xlsx
        return new Quest(
                "low_plant_loss_" + maxLost,
                "گیاه خوار اقتصادی (max " + maxLost + " lost)",
                Quest.Category.MAIN,
                Quest.Priority.HIGH,
                new QuestConditions.LowPlantLossCondition(maxLost),
                QuestReward.seedPackets("ANY", Math.max(1, reward))
        );
    }

    /** استاد دفاع  Finish a level with exactly 0 sun. Epic. */
    public static Quest createFinishWithZeroSun() {
        return new Quest(
                "finish_zero_sun",
                "استاد دفاع",
                Quest.Category.EPIC_CHALLENGE,
                Quest.Priority.CRITICAL,
                new QuestConditions.FinishWithZeroSunCondition(),
                QuestReward.diamonds(200)
        );
    }

    /** سرعت عمل  Kill 10 zombies in under 30 seconds. */
    public static Quest createSpeedKill(int kills, double timeLimitSeconds) {
        return new Quest(
                "speed_kill",
                "سرعت عمل",
                Quest.Category.MAIN,
                Quest.Priority.MEDIUM,
                new QuestConditions.SpeedKillCondition(kills, timeLimitSeconds),
                QuestReward.coins(500)
        );
    }

    /** تخریب گر حرفه ای  Plant 3 explosive plants in one level. */
    public static Quest createExplosiveExpert(int count) {
        return new Quest(
                "explosive_expert",
                "تخریب گر حرفه ای",
                Quest.Category.DAILY,
                Quest.Priority.LOW,
                new QuestConditions.PlantExplosivesCondition(count),
                QuestReward.coins(100)
        );
    }

    /** تقارن  Win with a symmetric layout. */
    public static Quest createSymmetryWin() {
        return new Quest(
                "symmetry_win",
                "تقارن",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.SymmetricBoardCondition(),
                QuestReward.coins(500)
        );
    }

    /** کشتار خانوادگی  Win using only the given family to score kills. */
    public static Quest createFamilyExclusiveKills(String family) {
        return new Quest(
                "family_kills_" + family,
                "کشتار خانوادگی (" + family + ")",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.FamilyOnlyKillsCondition(family),
                QuestReward.coins(1000)
        );
    }

    /** شکوفایی در محدودیت‌ها  Win without using any plant from the banned family. */
    public static Quest createFamilyBanned(String family) {
        return new Quest(
                "family_banned_" + family,
                "شکوفایی در محدودیت‌ها (" + family + ")",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.NoFamilyPlantedCondition(family),
                QuestReward.diamonds(100)
        );
    }

    /** شب یا صبح  Win a day level using only night plants. Epic. */
    public static Quest createNightPlantsInDayLevel() {
        return new Quest(
                "night_plants_day",
                "شب یا صبح",
                Quest.Category.EPIC_CHALLENGE,
                Quest.Priority.HIGH,
                new QuestConditions.NightPlantsInDayLevelCondition(),
                QuestReward.diamonds(20)
        );
    }

    /** برد پشت برد  Win n consecutive levels. */
    public static Quest createWinStreak(int n) {
        return new Quest(
                "win_streak_" + n,
                "برد پشت برد (" + n + ")",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.WinStreakCondition(n),
                QuestReward.coins(5000)
        );
    }

    /** تقریبا پیروز  Kill 10 zombies in column 0 in rows without a mower. */
    public static Quest createNearVictory(int kills, Set<Integer> rowsWithMowers) {
        return new Quest(
                "near_victory",
                "تقریبا پیروز",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.KillInFirstColumnNoMowerCondition(kills, rowsWithMowers),
                QuestReward.coins(300)
        );
    }

    /** OCD نَمَنَ  Win with a fully asymmetric layout (no symmetry at all). */
    public static Quest createAsymmetryWin() {
        return new Quest(
                "ocd_asymmetry",
                "OCD نَمَنَ",
                Quest.Category.DAILY,
                Quest.Priority.MEDIUM,
                new QuestConditions.AsymmetricBoardCondition(),
                QuestReward.coins(800)
        );
    }

    /** روز ابری  Win using at most 3 sun-producing plants. */
    public static Quest createLimitedSunProducers(int max) {
        return new Quest(
                "limited_sun_producers",
                "روز ابری",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.LimitedSunProducersCondition(max),
                QuestReward.diamonds(10)
        );
    }

    /** یه ستون کمتر  Win without planting in the given column. */
    public static Quest createEmptyColumn(int column) {
        return new Quest(
                "empty_col_" + column,
                "یه ستون کمتر (col " + column + ")",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.EmptyColumnCondition(column),
                QuestReward.diamonds(10)
        );
    }

    /** سطر بی دفاع  Win without planting in the given row. */
    public static Quest createEmptyRow(int row) {
        return new Quest(
                "empty_row_" + row,
                "سطر بی دفاع (row " + row + ")",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.EmptyRowCondition(row),
                QuestReward.diamonds(20)
        );
    }

    /** صلیب بی دفاع  Win without planting in the given row or column. */
    public static Quest createEmptyCross(int row, int col) {
        return new Quest(
                "empty_cross_" + row + "_" + col,
                "صلیب بی دفاع (row " + row + ", col " + col + ")",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.EmptyCrossCondition(row, col),
                QuestReward.diamonds(25)
        );
    }

    /** وقت چمن‌زنی  Kill n zombies with lawnmowers. Valid n: 10-50 (Epic) */
    public static Quest createLawnMowerKills(int n) {
        return new Quest(
                "lawnmower_kills_" + n,
                "وقت چمن‌زنی (" + n + ")",
                Quest.Category.EPIC_CHALLENGE,
                Quest.Priority.MEDIUM,
                new QuestConditions.LawnMowerKillsCondition(n),
                QuestReward.diamonds(n)
        );
    }
}
