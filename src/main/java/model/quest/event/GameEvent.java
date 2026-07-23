package model.quest.event;

public sealed interface GameEvent permits
        GameEvent.GameStarted,
        GameEvent.GameFinished,
        GameEvent.ZombieKilled,
        GameEvent.PlantPlanted,
        GameEvent.PlantDestroyed,
        GameEvent.SunCollected,
        GameEvent.SunSpent,
        GameEvent.WaveCompleted,
        GameEvent.LawnMowerTriggered {

    record GameStarted(String levelId, String chapterId, boolean isNightLevel) implements GameEvent {
    }

    record GameFinished(boolean won,
                        int sunRemaining,
                        int plantsLost,
                        long durationSeconds) implements GameEvent {
    }


    record ZombieKilled(String zombieType,
                        String killerPlantType,
                        String chapterId,
                        int column,
                        int row,
                        double secondsSinceWaveStart,
                        String projectileId,
                        long tick) implements GameEvent {
        public ZombieKilled(String zombieType,
                            String killerPlantType,
                            String chapterId,
                            int column,
                            int row,
                            double secondsSinceWaveStart) {
            this(zombieType, killerPlantType, chapterId, column, row, secondsSinceWaveStart, null, 0L);
        }
    }

    record PlantPlanted(String plantType,
                        String plantFamily,
                        int column,
                        int row,
                        boolean isNightPlant) implements GameEvent {
    }

    record PlantDestroyed(String plantType, String plantFamily) implements GameEvent {
    }

    record SunCollected(int amount) implements GameEvent {
    }

    record SunSpent(int amount) implements GameEvent {
    }

    record WaveCompleted(int waveIndex, boolean isFinalWave) implements GameEvent {
    }

    record LawnMowerTriggered(int row, int zombiesKilled) implements GameEvent {
    }
}
