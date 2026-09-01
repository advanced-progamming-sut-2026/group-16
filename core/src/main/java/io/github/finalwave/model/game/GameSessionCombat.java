package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.plant.ability.ChomperAbility;
import io.github.finalwave.model.game.entity.plant.ability.IcebergLettuceAbility;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpAbility;
import io.github.finalwave.model.game.entity.plant.ability.ExplosiveAbility;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.quest.event.GameEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


final class GameSessionCombat {

    private final GameSession session;

    GameSessionCombat(GameSession session) {
        this.session = session;
    }

    void runTick() {
        if (!session.isRunning() || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        session.incrementCurrentTick();
        session.getCooldownTracker().tick();
        session.getTileEffects().expireTimedEffects();
        session.getTileEffects().tickFireTiles();
        session.getTileEffects().tickGooPuddles();
        session.getCraterSystem().tick(session.getBoard(), session.getCurrentTick());
        session.getTileEffects().tickCoveringsAndObstacles();
        session.getTileEffects().tickAdjacentFireIceMelt();
        tickLivingPlants();
        tickZombies();
        session.getTileEffects().followPushedObstacles();
        tickMowers();
        session.getProjectileSystem().tick(
                session.getBoard(),
                session.zombieList(),
                session::handleProjectileKill,
                session.getContext());
        session.getJalapenoFireSystem().tick(
                session.getBoard(),
                session.zombieList(),
                session.getContext(),
                session.getCurrentTick());
        if (session.getMiniGameState().isWalnutBowlingActive()) {
            session.getMiniGameState().getBowlingNutSystem().tick(session);
        }
        session.getTileEffects().removeDeadCoveringsAndObstacles();
        cleanupDeadZombies();
        session.getPlanting().tickSunItems();
        session.getPlanting().tickSkySun();
        cleanupDeadPlants();
        if (session.getWaveManager() != null) {
            session.getWaveManager().tick(session);
            session.getWaveManager().publishClearedWaves(session);
        }
        checkWinCondition();
        session.getPlanting().tickGroundSeedPackets();
        notifyHandlersOnTick();
    }

    private void tickLivingPlants() {
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (plant.isAlive()) {
                plant.onTickUpdate(session.getContext());
            }
        }
    }

    private void notifyHandlersOnTick() {
        if (session.getActiveSpecialLevelHandler() != null) {
            session.getActiveSpecialLevelHandler().onTick(session);
        }
        if (session.getActiveMiniGameHandler() != null) {
            session.getActiveMiniGameHandler().onTick(session);
        }
    }

    void cleanupDeadPlants() {
        MatchListener matchListener = session.getMatchListener();
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (plant.isDead()) {
                if (matchListener != null) {
                    matchListener.onPlantDestroyed(plant, plant.getCol(), plant.getRow());
                }
                session.getBoard().removePlant(plant);
            }
        }
    }

    void start() {
        session.setRunning(true);
        session.setMatchResult(MatchResult.IN_PROGRESS);
        if (session.isWavesAutoStart()
                && session.getWaveManager() != null
                && !session.getWaveManager().areWavesStarted()) {
            session.getWaveManager().startWaves(session);
        }
        session.getEventBus().publish(new GameEvent.GameStarted(
                session.getLevelId(), session.getChapterId(), session.isNightLevel()));
    }

    void startZombieWaves() {
        GameSessionSpecialLevelState special = session.getSpecialLevelState();
        boolean endingPrep = special.isPrepPhaseActive() && special.isPlantWhatYouGetActive();
        if (endingPrep) {
            session.endPrepPhase();
        }
        if (session.getWaveManager() != null) {
            session.getWaveManager().startWaves(session);
        }
        if (endingPrep && session.getMatchListener() != null) {
            session.getMatchListener().onPlantWhatYouGetWavesStarted();
        }
    }

    void addZombie(Zombie zombie) {
        if (zombie == null) {
            return;
        }
        zombie.bindContext(session.getContext());
        if (zombie.isDead()) {
            session.handleZombieKilled(zombie);
        } else if (session.isTickingZombies()) {
            session.pendingZombieList().add(zombie);
        } else {
            session.zombieList().add(zombie);
        }
    }

    Zombie spawnZombieOfType(String alias, int row, double x) {
        if (session.getZombieFactory() == null) {
            throw new IllegalStateException("This session has no ZombieFactory");
        }
        if (row < 0 || row >= session.getBoard().getRows() || !Double.isFinite(x)
                || x < 0 || x > session.getBoard().getCols()) {
            throw new IllegalArgumentException("Zombie spawn position is outside the board");
        }
        Zombie zombie = session.getZombieFactory().createZombie(
                alias, x, row, session.getZombieDifficulty());
        addZombie(zombie);
        return zombie;
    }

    void tickZombies() {
        if (session.isIZombieActive()) {
            for (Zombie zombie : session.zombieList()) {
                zombie.lockLane();
            }
            for (Zombie zombie : session.pendingZombieList()) {
                zombie.lockLane();
            }
        }
        List<Zombie> zombies = session.zombieList();
        List<Zombie> pending = session.pendingZombieList();
        session.setTickingZombies(true);
        try {
            Iterator<Zombie> zombieIterator = zombies.iterator();
            while (zombieIterator.hasNext()) {
                Zombie zombie = zombieIterator.next();
                if (zombie.isDead()) {
                    session.handleZombieKilled(zombie);
                    zombieIterator.remove();
                    continue;
                }
                zombie.tickStatuses();
                if (zombie.isDead()) {
                    session.handleZombieKilled(zombie);
                    zombieIterator.remove();
                    continue;
                }
                zombie.snapshotPose();
                zombie.onTickUpdate(session.getContext());
                checkArmedTraps(zombie);
                if (zombie.isDead()) {
                    session.handleZombieKilled(zombie);
                    zombieIterator.remove();
                }
            }
        } finally {
            session.setTickingZombies(false);
            zombies.addAll(pending);
            pending.clear();
        }
    }

    private void checkArmedTraps(Zombie zombie) {
        if (zombie.isTrapImmune()) {
            return;
        }
        int col = (int) Math.floor(zombie.getX());
        for (int row : zombie.occupiedRows()) {
            Plant plant = session.getBoard().getGroundPlantAt(col, row);
            Plant inFront = session.getBoard().getPlantInFront(zombie.getX(), row);
            Plant chomper = plant != null && plant.isChomper() ? plant
                    : (inFront != null && inFront.isChomper() ? inFront : null);
            if (chomper != null && chomper.isAlive()
                    && chomper.getAbility() instanceof ChomperAbility ability) {
                ability.onZombieContact(chomper, zombie, session.getContext());
            }
            if (plant == null || !plant.isAlive() || plant.isSquash()
                    || !plant.hasTag(PlantTag.TRAP) || !plant.isArmedTrap()) {
                continue;
            }
            if (plant.getAbility() instanceof TangleKelpAbility kelp) {
                kelp.startGrab(plant, zombie, session.getContext());
                continue;
            }
            if (plant.getAbility() instanceof IcebergLettuceAbility iceberg) {
                iceberg.startTrapFreeze(plant, zombie, session.getContext());
                continue;
            }
            if (plant.getAbility() instanceof ExplosiveAbility explosive) {
                explosive.detonate(plant, session.getContext());
            } else {
                session.getContext().explode(plant, plant.getStats().damage(), 1.0);
                plant.consumeInstantly();
            }
        }
    }

    void handleZombieKilled(Zombie zombie, String killerPlantType, String projectileId) {
        if (zombie == null || !zombie.isDead()) {
            return;
        }
        zombie.runDeathBehaviors(session.getContext());
        if (!session.killedZombieIds().add(zombie.getId())) {
            return;
        }
        session.getSpecialLevelState().registerTimedWarKill();
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onZombieDied(zombie.getType(), zombie.getX(), zombie.getRow());
        }
        if (zombie.isGlowing() && session.getPlantFoodCount() < GameSession.MAX_PLANT_FOOD) {
            session.spawnPlantFoodDrop(
                    (int) Math.floor(zombie.getX()),
                    zombie.getRow(),
                    zombie.getX());
        }
        rollZombieLootDrop();
        int currentTick = session.getCurrentTick();
        double secondsSinceWave = Math.max(0,
                (currentTick - session.getWaveStartTick()) / (double) GameSession.TICKS_PER_SECOND);
        int firstWaveTick = session.getFirstWaveStartTick();
        double secondsSinceFirstWave = Math.max(0,
                (currentTick - firstWaveTick) / (double) GameSession.TICKS_PER_SECOND);
        String killerFamily = null;
        if (killerPlantType != null) {
            var definition = session.getPlantRegistry().getDefinition(killerPlantType);
            if (definition != null) {
                killerFamily = definition.getCategory();
            }
        }
        session.getEventBus().publish(new GameEvent.ZombieKilled(
                zombie.getType(),
                killerPlantType,
                killerFamily,
                session.getChapterId(),
                (int) zombie.getX(),
                zombie.getRow(),
                secondsSinceWave,
                secondsSinceFirstWave,
                projectileId,
                currentTick));
    }

    private void rollZombieLootDrop() {
        if (session.getRandom().nextInt(100) >= 10) {
            return;
        }
        MatchListener matchListener = session.getMatchListener();
        if (matchListener == null) {
            return;
        }
        int roll = session.getRandom().nextInt(3);
        if (roll == 0) {
            matchListener.onItemDropped("coin", 50);
        } else if (roll == 1) {
            matchListener.onItemDropped("diamond", 1);
        } else {
            matchListener.onItemDropped("pot", 1);
        }
    }

    void handleZombieReachedHouse(Zombie zombie) {
        if (session.isSandboxPractice()) {
            return;
        }
        if (zombie == null || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        int row = zombie.getRow();
        GameSessionMiniGameState miniGameState = session.getMiniGameState();
        if (miniGameState.isIZombieActive()) {
            MatchListener matchListener = session.getMatchListener();
            if (miniGameState.markBrainEatenIfNew(row) && matchListener != null) {
                matchListener.onBrainEaten(row);
            }
            if (zombie.isAlive()) {
                zombie.setStationary(true);
                zombie.setPosition(0, row);
            }
            return;
        }
        List<LawnMower> lawnMowers = session.lawnMowerList();
        if (row < 0 || row >= lawnMowers.size()) {
            session.loseMatch();
            return;
        }
        LawnMower mower = lawnMowers.get(row);
        MatchListener matchListener = session.getMatchListener();
        if (mower.isUsed()) {
            if (matchListener != null) {
                matchListener.onLawnMowerFailed(row + 1);
            }
            session.loseMatch();
            return;
        }
        boolean started = mower.trigger();
        List<Zombie> killed = new ArrayList<>();
        if (zombie.isAlive() && !isBossZombie(zombie)) {
            zombie.takeDirectDamage(zombie.getHealth() + 99999);
            session.handleZombieKilled(zombie);
            killed.add(zombie);
        }
        if (!session.isTickingZombies()) {
            session.zombieList().removeIf(Zombie::isDead);
        }
        if (zombie.isAlive()) {
            if (matchListener != null) {
                matchListener.onLawnMowerFailed(row + 1);
            }
            session.loseMatch();
            return;
        }
        if (started) {
            if (matchListener != null) {
                matchListener.onLawnMowerTriggered(row + 1, killed);
            }
            session.getEventBus().publish(new GameEvent.LawnMowerTriggered(row, killed.size()));
        }
    }

    private void tickMowers() {
        if (session.isIZombieActive()) {
            return;
        }
        int cols = session.getBoard().getCols();
        for (LawnMower mower : session.lawnMowerList()) {
            if (mower == null || !mower.isActive()) {
                continue;
            }
            mower.tick(cols);
            if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
                continue;
            }
            for (Zombie candidate : List.copyOf(session.zombieList())) {
                if (!candidate.isAlive()
                        || !candidate.occupiesRow(mower.getRow())
                        || isBossZombie(candidate)
                        || !mower.hits(candidate.getX())) {
                    continue;
                }
                candidate.takeDirectDamage(candidate.getHealth() + 99999);
                session.handleZombieKilled(candidate);
            }
        }
        if (!session.isTickingZombies()) {
            session.zombieList().removeIf(Zombie::isDead);
        }
    }

    private static boolean isBossZombie(Zombie zombie) {
        if (zombie == null) {
            return false;
        }
        if (zombie.isBoss()) {
            return true;
        }
        String type = zombie.getType();
        return type != null && (type.contains("Gargantuar") || type.contains("King"));
    }

    void checkWinCondition() {
        if (session.isSandboxPractice()) {
            return;
        }
        if (session.getMatchResult() != MatchResult.IN_PROGRESS || session.getWaveManager() == null) {
            return;
        }
        if (session.getSpecialLevelState().isTimedWarActive()
                || session.getMiniGameState().isBeghouledActive()
                || session.getSpecialLevelState().isBossActive()) {
            return;
        }
        if (session.getWaveManager().areAllWavesCleared() && livingZombieCount() == 0) {
            session.winMatch();
        }
    }

    private int livingZombieCount() {
        int count = 0;
        for (Zombie zombie : session.zombieList()) {
            if (zombie.countsAsEnemy()) {
                count++;
            }
        }
        return count;
    }

    void winMatch() {
        if (session.isSandboxPractice()) {
            return;
        }
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        session.setMatchResult(MatchResult.WON);
        session.setRunning(false);
        if (session.getAttachedQuestTracker() != null) {
            session.getAttachedQuestTracker().prepareBoardSnapshots(session);
        }
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onWin();
        }
        session.getEventBus().publish(new GameEvent.GameFinished(
                true, session.getSunBalance(), session.getPlantsLost(),
                session.getCurrentTick() / (long) GameSession.TICKS_PER_SECOND,
                session.getUserDifficultyLevel()));
    }

    void loseMatch() {
        if (session.isSandboxPractice() || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        session.setMatchResult(MatchResult.LOST);
        session.setRunning(false);
        if (session.getAttachedQuestTracker() != null) {
            session.getAttachedQuestTracker().prepareBoardSnapshots(session);
        }
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onLose();
        }
        session.getEventBus().publish(new GameEvent.GameFinished(
                false, session.getSunBalance(), session.getPlantsLost(),
                session.getCurrentTick() / (long) GameSession.TICKS_PER_SECOND,
                session.getUserDifficultyLevel()));
    }

    void nukeAllZombies() {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) {
                zombie.takeDirectDamage(zombie.getHealth() + 99999);
                session.handleZombieKilled(zombie);
            }
        }
        session.zombieList().removeIf(Zombie::isDead);
    }

    void cleanupDeadZombies() {
        session.zombieList().removeIf(zombie -> {
            if (!zombie.isDead()) {
                return false;
            }
            session.handleZombieKilled(zombie);
            return true;
        });
    }

    void explodeRadioactiveSun(int col, int row) {
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onRadioactiveSunExploded(col, row);
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zCol - col) <= 2 && Math.abs(zombie.getRow() - row) <= 2) {
                zombie.markPowderDeath();
                zombie.takeDirectDamage(150);
                if (zombie.isDead()) {
                    session.handleZombieKilled(zombie);
                }
            }
        }
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (!plant.isAlive()) {
                continue;
            }
            if (Math.abs(plant.getCol() - col) <= 1 && Math.abs(plant.getRow() - row) <= 1) {
                plant.takeDamage(80);
            }
        }
        cleanupDeadZombies();
        cleanupDeadPlants();
        session.queueLawnBurst(new LawnBurst(LawnBurst.Kind.GENERIC, col, row));
    }
}
