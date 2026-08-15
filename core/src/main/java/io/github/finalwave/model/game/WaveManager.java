package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public final class WaveManager {

    public static final double NEXT_WAVE_HEALTH_RATIO = 0.75;
    public static final double NORMAL_WAVE_SCALE = 1.25;
    public static final double FLAG_WAVE_SCALE = 2.0;

    private final int waveCount;
    private final int baseWaveCost;
    private final List<String> zombiePool;
    private final Random random;
    private final List<Wave> waves = new ArrayList<>();
    private int currentWaveIndex = -1;
    private boolean wavesStarted;
    private boolean allWavesSpawned;
    private double waveCostDifficultyScale = 1.0;
    private int sandstormMinOffset;
    private int sandstormMaxOffset;
    private boolean sandstormOnFinalWave;

    public WaveManager(int waveCount, int baseWaveCost, List<String> zombiePool, Random random) {
        if (waveCount < 1) {
            throw new IllegalArgumentException("waveCount must be >= 1");
        }
        if (baseWaveCost < 1) {
            throw new IllegalArgumentException("baseWaveCost must be >= 1");
        }
        if (zombiePool == null || zombiePool.isEmpty()) {
            throw new IllegalArgumentException("zombiePool must not be empty");
        }
        this.waveCount = waveCount;
        this.baseWaveCost = baseWaveCost;
        this.zombiePool = List.copyOf(zombiePool);
        this.random = random == null ? new Random() : random;
        rebuildWaves();
    }

    private void rebuildWaves() {
        waves.clear();
        int previousCost = Math.max(1, (int) Math.round(baseWaveCost / waveCostDifficultyScale));
        for (int i = 1; i <= waveCount; i++) {
            boolean flag = i == waveCount;
            int cost;
            if (i == 1) {
                cost = previousCost;
            } else if (flag) {
                cost = Math.max(1, (int) Math.round(previousCost * FLAG_WAVE_SCALE));
            } else {
                cost = Math.max(1, (int) Math.round(previousCost * NORMAL_WAVE_SCALE));
            }
            waves.add(new Wave(i, cost, flag));
            previousCost = cost;
        }
    }

    public void setWaveCostDifficultyScale(double scale) {
        this.waveCostDifficultyScale = Math.max(0.1, scale);
        rebuildWaves();
        currentWaveIndex = -1;
        wavesStarted = false;
        allWavesSpawned = false;
    }

    public double getWaveCostDifficultyScale() {
        return waveCostDifficultyScale;
    }

    public void enableSandstormOnFinalWave(int minOffset, int maxOffset) {
        this.sandstormOnFinalWave = true;
        this.sandstormMinOffset = Math.max(0, minOffset);
        this.sandstormMaxOffset = Math.max(this.sandstormMinOffset, maxOffset);
    }

    public int getWaveCount() {
        return waveCount;
    }

    public List<Wave> getWaves() {
        return List.copyOf(waves);
    }

    public List<String> getZombiePool() {
        return zombiePool;
    }

    public Wave getCurrentWave() {
        if (currentWaveIndex < 0 || currentWaveIndex >= waves.size()) {
            return null;
        }
        return waves.get(currentWaveIndex);
    }

    public int getCurrentWaveNumber() {
        Wave wave = getCurrentWave();
        return wave == null ? 0 : wave.getNumber();
    }

    public boolean areWavesStarted() {
        return wavesStarted;
    }

    public boolean areAllWavesSpawned() {
        return allWavesSpawned;
    }

    public boolean areAllWavesCleared() {
        if (!allWavesSpawned) {
            return false;
        }
        for (Wave wave : waves) {
            if (!wave.isCleared()) {
                return false;
            }
        }
        return true;
    }

    public void startWaves(GameSession session) {
        if (wavesStarted) {
            return;
        }
        wavesStarted = true;
        spawnWave(session, 0);
    }

    public void tick(GameSession session) {
        if (!wavesStarted || allWavesSpawned) {
            return;
        }
        Wave current = getCurrentWave();
        if (current == null) {
            return;
        }
        if (currentWaveIndex + 1 >= waves.size()) {
            allWavesSpawned = true;
            return;
        }
        if (current.getDestroyedHealthRatio() >= NEXT_WAVE_HEALTH_RATIO) {
            spawnWave(session, currentWaveIndex + 1);
        }
    }

    private void spawnWave(GameSession session, int index) {
        currentWaveIndex = index;
        Wave wave = waves.get(index);
        wave.markStarted();
        session.markWaveStarted();
        MatchListener listener = session.getMatchListener();
        if (wave.isFlagWave()) {
            if (listener != null) listener.onFinalWave();
        } else if (listener != null) listener.onWaveStarted(wave.getNumber());
        int spent = 0;
        int rows = session.getBoard().getRows();
        int cols = session.getBoard().getCols();
        double spawnX = cols - 0.1;
        int safety = 0;
        while (spent < wave.getTargetCost() && safety < 200) {
            safety++;
            int remaining = wave.getTargetCost() - spent;
            List<String> affordable = aliasesAffordable(session, remaining);
            if (affordable.isEmpty()) {
                if (spent == 0) {
                    affordable = List.copyOf(zombiePool);
                } else {
                    break;
                }
            }
            String alias = affordable.get(random.nextInt(affordable.size()));
            int lane = random.nextInt(rows);
            double x = spawnX;
            if (wave.isFlagWave() && sandstormOnFinalWave) {
                int offset = sandstormMinOffset
                        + random.nextInt(sandstormMaxOffset - sandstormMinOffset + 1);
                x = Math.max(0.5, spawnX - offset);
            }
            Zombie zombie = session.spawnZombieOfType(alias, lane, x);
            if (random.nextInt(100) < 5) zombie.setGlowing(true);
            wave.registerSpawn(zombie);
            spent += Math.max(1, zombie.getWaveCost());
            if (listener != null) {
                listener.onZombieSpawned(zombie.getType(), wave.getNumber(), lane + 1,
                        zombie.getWaveCost());
            }
            if (spent >= wave.getTargetCost()) break;
        }
        if (index == waves.size() - 1) {
            allWavesSpawned = true;
        }
    }

    public void publishClearedWaves(GameSession session) {
        if (!wavesStarted || session == null) {
            return;
        }
        for (Wave wave : waves) {
            if (wave.isStarted() && wave.isCleared() && !wave.isCompleted()) {
                wave.markCompleted();
                session.getEventBus().publish(new io.github.finalwave.model.quest.event.GameEvent.WaveCompleted(
                        wave.getNumber(), wave.isFlagWave()));
            }
        }
    }

    private List<String> aliasesAffordable(GameSession session, int remainingBudget) {
        List<String> result = new ArrayList<>();
        for (String alias : zombiePool) {
            int cost = resolveWaveCost(session, alias);
            if (cost > 0 && cost <= remainingBudget) {
                result.add(alias);
            }
        }
        return result;
    }

    private int resolveWaveCost(GameSession session, String alias) {
        if (session.getZombieFactory() == null) {
            return 100;
        }
        try {
            Zombie sample = session.getZombieFactory().createZombie(alias, 0.0, 0,
                    session.getZombieDifficulty());
            return Math.max(1, sample.getWaveCost());
        } catch (RuntimeException ex) {
            return -1;
        }
    }

    public static double waveCostScale(int difficultyLevel) {
        return Math.max(1, difficultyLevel) / 3.0;
    }

    public static double zombieStatScale(int difficultyLevel) {
        return Math.max(1, difficultyLevel) / 3.0;
    }

    public static double skySunIntervalScale(int difficultyLevel) {
        return Math.max(1, difficultyLevel) / 3.0;
    }
}
