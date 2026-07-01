package model.quest.condition;

import model.quest.event.GameEvent;

import java.util.HashSet;
import java.util.Set;

public final class QuestConditions {

    private QuestConditions() {
    }

    // ======================================================================
    // 1. Daily Sun Collector
    // ======================================================================

    public static final class CollectSunCondition implements QuestCondition {
        private final int targetSun;
        private int collected;

        public CollectSunCondition(int targetSun) {
            this.targetSun = targetSun;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.SunCollected e) {
                collected += e.amount();
            }
        }

        @Override
        public boolean isMet() {
            return collected >= targetSun;
        }

        @Override
        public void reset() {
            collected = 0;
        }

        @Override
        public String describe() {
            return "Collect " + targetSun + " sun in a single session (collected: " + collected + ")";
        }
    }

    // ======================================================================
    // 2. Chapter Hunter
    // ======================================================================

    public static final class KillZombiesInChapterCondition implements QuestCondition {
        private final String chapterId;
        private final int targetCount;
        private int killed;

        public KillZombiesInChapterCondition(String chapterId, int targetCount) {
            this.chapterId = chapterId;
            this.targetCount = targetCount;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.ZombieKilled e && chapterId.equalsIgnoreCase(e.chapterId())) {
                killed++;
            }
        }

        @Override
        public boolean isMet() {
            return killed >= targetCount;
        }

        @Override
        public void reset() {
            killed = 0;
        }

        @Override
        public String describe() {
            return "Kill " + targetCount + " zombies in chapter " + chapterId + " (killed: " + killed + ")";
        }
    }

    // ======================================================================
    // 3. Plant Pro
    // ======================================================================

    public static final class KillOnlyWithPlantCondition implements QuestCondition {
        private final String plantType;
        private final int targetCount;
        private int kills;
        private boolean violated;

        public KillOnlyWithPlantCondition(String plantType, int targetCount) {
            this.plantType = plantType;
            this.targetCount = targetCount;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.ZombieKilled e) {
                if (plantType.equalsIgnoreCase(e.killerPlantType())) {
                    kills++;
                } else if (e.killerPlantType() != null) {
                    // A different plant scored a kill — quest is permanently failed this session
                    violated = true;
                }
            }
        }

        @Override
        public boolean isMet() {
            return !violated && kills >= targetCount;
        }

        @Override
        public void reset() {
            kills = 0;
            violated = false;
        }

        @Override
        public String describe() {
            return "Kill " + targetCount + " zombies using only " + plantType +
                    " (kills: " + kills + ", violated: " + violated + ")";
        }
    }

    // ======================================================================
    // 4. Finish with Zero Sun
    // ======================================================================

    public static final class FinishWithZeroSunCondition implements QuestCondition {
        private boolean wonWithZero;

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.GameFinished e && e.won() && e.sunRemaining() == 0) {
                wonWithZero = true;
            }
        }

        @Override
        public boolean isMet() {
            return wonWithZero;
        }

        @Override
        public void reset() {
            wonWithZero = false;
        }

        @Override
        public String describe() {
            return "Win a level with exactly 0 sun remaining";
        }
    }

    // ======================================================================
    // 5. Speed Kill
    // ======================================================================

    public static final class SpeedKillCondition implements QuestCondition {
        private final int targetKills;
        private final double timeLimitSeconds;
        private int kills;
        private boolean met;

        public SpeedKillCondition(int targetKills, double timeLimitSeconds) {
            this.targetKills = targetKills;
            this.timeLimitSeconds = timeLimitSeconds;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (met) return;
            if (event instanceof GameEvent.ZombieKilled e && e.secondsSinceWaveStart() <= timeLimitSeconds) {
                kills++;
                if (kills >= targetKills) met = true;
            }
        }

        @Override
        public boolean isMet() {
            return met;
        }

        @Override
        public void reset() {
            kills = 0;
            met = false;
        }

        @Override
        public String describe() {
            return "Kill " + targetKills + " zombies within " + timeLimitSeconds + "s of wave 1 (kills: " + kills + ")";
        }
    }

    // ======================================================================
    // 6. Explosive Expert
    // ======================================================================

    public static final class PlantExplosivesCondition implements QuestCondition {
        private final int targetCount;
        private static final Set<String> EXPLOSIVE_FAMILIES = Set.of("EXPLOSIVE");
        private int planted;

        public PlantExplosivesCondition(int targetCount) {
            this.targetCount = targetCount;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.PlantPlanted e
                    && EXPLOSIVE_FAMILIES.contains(e.plantFamily().toUpperCase())) {
                planted++;
            }
        }

        @Override
        public boolean isMet() {
            return planted >= targetCount;
        }

        @Override
        public void reset() {
            planted = 0;
        }

        @Override
        public String describe() {
            return "Plant " + targetCount + " explosive plants in one level (planted: " + planted + ")";
        }
    }

    // ======================================================================
    // 7. Symmetry
    // ======================================================================

    public static final class SymmetricBoardCondition implements QuestCondition {
        private boolean symmetric;
        // plantTypeAt[row][col] — injected by controller before GameFinished
        private String[][] boardSnapshot;

        /**
         * Call this before publishing GameFinished so the condition can evaluate.
         */
        public void setBoardSnapshot(String[][] board) {
            this.boardSnapshot = board;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.GameFinished e && e.won() && boardSnapshot != null) {
                symmetric = isBoardSymmetric(boardSnapshot);
            }
        }

        private boolean isBoardSymmetric(String[][] board) {
            for (String[] row : board) {
                int cols = row.length;
                for (int c = 0; c < cols / 2; c++) {
                    String left = row[c];
                    String right = row[cols - 1 - c];
                    boolean bothEmpty = (left == null || left.isEmpty()) && (right == null || right.isEmpty());
                    boolean match = bothEmpty || (left != null && left.equals(right));
                    if (!match) return false;
                }
            }
            return true;
        }

        @Override
        public boolean isMet() {
            return symmetric;
        }

        @Override
        public void reset() {
            symmetric = false;
            boardSnapshot = null;
        }

        @Override
        public String describe() {
            return "Win a level with a symmetric plant layout";
        }
    }

    // ======================================================================
    // 8. Family Exclusive
    // ======================================================================

    public static final class FamilyOnlyKillsCondition implements QuestCondition {
        private final String family;
        private boolean violated;
        private boolean won;

        public FamilyOnlyKillsCondition(String family) {
            this.family = family;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.ZombieKilled e) {
                if (e.killerPlantType() != null
                        && !family.equalsIgnoreCase(/* plant family lookup */ e.killerPlantType())) {
                    // NOTE: replace the condition above with a PlantRegistry lookup once available:
                    // PlantRegistry.getInstance().getFamily(e.killerPlantType()).equals(family)
                }
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                won = true;
            }
        }

        // Package-visible so GamePlayController can report a family-mismatch kill
        public void reportFamilyViolation() {
            violated = true;
        }

        @Override
        public boolean isMet() {
            return won && !violated;
        }

        @Override
        public void reset() {
            violated = false;
            won = false;
        }

        @Override
        public String describe() {
            return "Win using only " + family + " family plants for kills";
        }
    }

    // ======================================================================
    // 9. Family Banned
    // ======================================================================

    public static final class NoFamilyPlantedCondition implements QuestCondition {
        private final String bannedFamily;
        private boolean plantedBanned;
        private boolean won;

        public NoFamilyPlantedCondition(String bannedFamily) {
            this.bannedFamily = bannedFamily;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.PlantPlanted e && bannedFamily.equalsIgnoreCase(e.plantFamily())) {
                plantedBanned = true;
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                won = true;
            }
        }

        @Override
        public boolean isMet() {
            return won && !plantedBanned;
        }

        @Override
        public void reset() {
            plantedBanned = false;
            won = false;
        }

        @Override
        public String describe() {
            return "Win without planting any " + bannedFamily + " family plant";
        }
    }

    // ======================================================================
    // 10. Night Plants Day Level
    // ======================================================================

    public static final class NightPlantsInDayLevelCondition implements QuestCondition {
        private boolean usedDayPlant;
        private boolean wonDayLevel;

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.GameStarted e && !e.isNightLevel()) {
                // We're in a day level — monitoring starts
            }
            if (event instanceof GameEvent.PlantPlanted e && !e.isNightPlant()) {
                usedDayPlant = true;
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                wonDayLevel = true;
            }
        }

        @Override
        public boolean isMet() {
            return wonDayLevel && !usedDayPlant;
        }

        @Override
        public void reset() {
            usedDayPlant = false;
            wonDayLevel = false;
        }

        @Override
        public String describe() {
            return "Win a day level using only mushroom/night plants";
        }
    }

    // ======================================================================
    // 11. Win Streak
    // ======================================================================

    public static final class WinStreakCondition implements QuestCondition {
        private final int requiredWins;
        private int streak;

        public WinStreakCondition(int requiredWins) {
            this.requiredWins = requiredWins;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.GameFinished e) {
                if (e.won()) {
                    streak++;
                } else {
                    streak = 0;
                }
            }
        }

        @Override
        public boolean isMet() {
            return streak >= requiredWins;
        }

        @Override
        public void reset() {
            streak = 0;
        }

        @Override
        public String describe() {
            return "Win " + requiredWins + " consecutive levels (streak: " + streak + ")";
        }
    }

    // ======================================================================
    // 12. Near Victory
    // ======================================================================

    public static final class KillInFirstColumnNoMowerCondition implements QuestCondition {
        private final int targetKills;
        private final Set<Integer> rowsWithMowers;
        private int kills;

        public KillInFirstColumnNoMowerCondition(int targetKills, Set<Integer> rowsWithMowers) {
            this.targetKills = targetKills;
            this.rowsWithMowers = new HashSet<>(rowsWithMowers);
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.ZombieKilled e && e.column() == 0 && !rowsWithMowers.contains(e.row())) {
                kills++;
            }
        }

        @Override
        public boolean isMet() {
            return kills >= targetKills;
        }

        @Override
        public void reset() {
            kills = 0;
        }

        @Override
        public String describe() {
            return "Kill " + targetKills + " zombies in column 0 in rows without a mower (killed: " + kills + ")";
        }
    }

    // ======================================================================
    // 13. Lawnmower Kills
    // ======================================================================

    public static final class LawnMowerKillsCondition implements QuestCondition {
        private final int targetKills;
        private int kills;

        public LawnMowerKillsCondition(int targetKills) {
            this.targetKills = targetKills;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.LawnMowerTriggered e) {
                kills += e.zombiesKilled();
            }
        }

        @Override
        public boolean isMet() {
            return kills >= targetKills;
        }

        @Override
        public void reset() {
            kills = 0;
        }

        @Override
        public String describe() {
            return "Have lawnmowers kill " + targetKills + " zombies (killed: " + kills + ")";
        }
    }

    // ======================================================================
    // 14. Frugal Victor
    // ======================================================================

    public static final class LowPlantLossCondition implements QuestCondition {
        private final int maxLost;
        private boolean met;

        public LowPlantLossCondition(int maxLost) {
            this.maxLost = maxLost;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.GameFinished e && e.won() && e.plantsLost() <= maxLost) {
                met = true;
            }
        }

        @Override
        public boolean isMet() {
            return met;
        }

        @Override
        public void reset() {
            met = false;
        }

        @Override
        public String describe() {
            return "Win a level losing at most " + maxLost + " plants";
        }
    }

    // ======================================================================
    // 15. Minimal Sun Producers
    // ======================================================================

    public static final class LimitedSunProducersCondition implements QuestCondition {
        private final int maxProducers;
        private static final String SUN_PRODUCER_FAMILY = "SUN_PRODUCER";
        private int producersPlanted;
        private boolean won;
        private boolean violated;

        public LimitedSunProducersCondition(int maxProducers) {
            this.maxProducers = maxProducers;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.PlantPlanted e && SUN_PRODUCER_FAMILY.equalsIgnoreCase(e.plantFamily())) {
                producersPlanted++;
                if (producersPlanted > maxProducers) violated = true;
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                won = true;
            }
        }

        @Override
        public boolean isMet() {
            return won && !violated;
        }

        @Override
        public void reset() {
            producersPlanted = 0;
            violated = false;
            won = false;
        }

        @Override
        public String describe() {
            return "Win using at most " + maxProducers + " sun-producing plants (planted: " + producersPlanted + ")";
        }
    }

    // ======================================================================
    // 16. Empty Column
    // ======================================================================

    public static final class EmptyColumnCondition implements QuestCondition {
        private final int forbiddenColumn;
        private boolean plantedInColumn;
        private boolean won;

        public EmptyColumnCondition(int forbiddenColumn) {
            this.forbiddenColumn = forbiddenColumn;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.PlantPlanted e && e.column() == forbiddenColumn) {
                plantedInColumn = true;
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                won = true;
            }
        }

        @Override
        public boolean isMet() {
            return won && !plantedInColumn;
        }

        @Override
        public void reset() {
            plantedInColumn = false;
            won = false;
        }

        @Override
        public String describe() {
            return "Win without planting in column " + forbiddenColumn;
        }
    }

    // ======================================================================
    // 17. Empty Row
    // ======================================================================

    public static final class EmptyRowCondition implements QuestCondition {
        private final int forbiddenRow;
        private boolean plantedInRow;
        private boolean won;

        public EmptyRowCondition(int forbiddenRow) {
            this.forbiddenRow = forbiddenRow;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.PlantPlanted e && e.row() == forbiddenRow) {
                plantedInRow = true;
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                won = true;
            }
        }

        @Override
        public boolean isMet() {
            return won && !plantedInRow;
        }

        @Override
        public void reset() {
            plantedInRow = false;
            won = false;
        }

        @Override
        public String describe() {
            return "Win without planting in row " + forbiddenRow;
        }
    }

    // ======================================================================
    // 18. Empty Cross
    // ======================================================================

    public static final class EmptyCrossCondition implements QuestCondition {
        private final int forbiddenRow;
        private final int forbiddenCol;
        private boolean violated;
        private boolean won;

        public EmptyCrossCondition(int forbiddenRow, int forbiddenCol) {
            this.forbiddenRow = forbiddenRow;
            this.forbiddenCol = forbiddenCol;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.PlantPlanted e && (e.row() == forbiddenRow || e.column() == forbiddenCol)) {
                violated = true;
            }
            if (event instanceof GameEvent.GameFinished e && e.won()) {
                won = true;
            }
        }

        @Override
        public boolean isMet() {
            return won && !violated;
        }

        @Override
        public void reset() {
            violated = false;
            won = false;
        }

        @Override
        public String describe() {
            return "Win without planting in row " + forbiddenRow + " or column " + forbiddenCol;
        }
    }

    // ======================================================================
    // 19. OCD Asymmetry
    // ======================================================================

    public static final class AsymmetricBoardCondition implements QuestCondition {
        private boolean met;
        private String[][] boardSnapshot;

        public void setBoardSnapshot(String[][] board) {
            this.boardSnapshot = board;
        }

        @Override
        public void onEvent(GameEvent event) {
            if (event instanceof GameEvent.GameFinished e && e.won() && boardSnapshot != null) {
                met = !hasAnySymmetry(boardSnapshot);
            }
        }

        private boolean hasAnySymmetry(String[][] board) {
            int rows = board.length;
            for (int r = 0; r < rows; r++) {
                if (rows % 2 == 1 && r == rows / 2) continue; // skip middle row
                String[] row = board[r];
                int cols = row.length;
                for (int c = 0; c < cols / 2; c++) {
                    String left = row[c];
                    String right = row[cols - 1 - c];
                    if (left != null && left.equals(right)) return true;
                }
            }
            return false;
        }

        @Override
        public boolean isMet() {
            return met;
        }

        @Override
        public void reset() {
            met = false;
            boardSnapshot = null;
        }

        @Override
        public String describe() {
            return "Win with a fully asymmetric plant layout";
        }
    }
}
