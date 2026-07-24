package model.quest;

import model.App;
import model.adventure.ChapterId;
import model.definition.PlantRegistry;
import model.definition.plant.PlantDefinition;
import model.game.board.GameBoard;
import model.game.entity.plant.PlantCategory;
import model.quest.condition.QuestConditions;
import model.quest.reward.QuestReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class QuestFactory {

    private QuestFactory() {
    }

    public static List<Quest> createAllQuests() {
        List<Quest> all = new ArrayList<>();
        for (int sun : List.of(3000, 4000, 5000)) all.add(createDailySunCollector(sun));
        PlantRegistry registry = App.getInstance().getPlantRegistry();
        if (registry != null) {
            for (PlantDefinition def : registry.getAllDefinitions()) {
                if (def.getDamage() > 0) {
                    all.add(createPlantProKiller(def.getName()));
                }
            }
        }
        all.add(createOnlyCactusKiller());
        all.add(createExplosiveExpert(3));
        all.add(createSymmetryWin());
        for (PlantCategory category : PlantCategory.values()) {
            all.add(createFamilyExclusiveKills(category.name()));
            all.add(createFamilyBanned(category.name()));
        }
        all.add(createWinStreak(5));
        all.add(createNearVictory(10, Set.of()));
        all.add(createAsymmetryWin());
        all.add(createLimitedSunProducers(3));
        for (int col = 0; col < GameBoard.DEFAULT_COLS; col++) all.add(createEmptyColumn(col));
        for (int row = 0; row < GameBoard.DEFAULT_ROWS; row++) all.add(createEmptyRow(row));
        int crossLimit = Math.min(GameBoard.DEFAULT_ROWS, GameBoard.DEFAULT_COLS);
        for (int n = 0; n < crossLimit; n++) {
            all.add(createEmptyCross(n, n));
        }
        for (ChapterId chapterId : ChapterId.values()) {
            all.add(createChapterHunter(chapterId.getKey(), 50));
        }
        for (int n = 0; n <= 5; n++) {
            all.add(createLowPlantLoss(n));
        }
        all.add(createFinishWithZeroSun());
        all.add(createSpeedKill(10, 30.0));
        all.add(createNightPlantsInDayLevel());
        for (int n = 10; n <= 50; n += 10) {
            all.add(createLawnMowerKills(n));
        }
        return all;
    }

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

    public static Quest createPlantProKiller(String plantType) {
        String idSuffix = plantType == null ? "unknown" : plantType.replace(' ', '_').toLowerCase();
        return new Quest(
                "plant_pro_" + idSuffix,
                "حرفه‌ای " + plantType,
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.KillOnlyWithPlantCondition(plantType, 10),
                QuestReward.randomPlantUnlock()
        );
    }

    public static Quest createOnlyCactusKiller() {
        return new Quest(
                "only_cactus",
                "only cactus",
                Quest.Category.DAILY,
                Quest.Priority.HIGH,
                new QuestConditions.KillOnlyWithPlantCondition("Cactus", 10),
                QuestReward.diamonds(20)
        );
    }

    public static Quest createLowPlantLoss(int maxLost) {
        int reward = 20 - maxLost;
        return new Quest(
                "low_plant_loss_" + maxLost,
                "گیاه خوار اقتصادی (max " + maxLost + " lost)",
                Quest.Category.MAIN,
                Quest.Priority.HIGH,
                new QuestConditions.LowPlantLossCondition(maxLost),
                QuestReward.seedPackets("ANY", Math.max(1, reward))
        );
    }

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
