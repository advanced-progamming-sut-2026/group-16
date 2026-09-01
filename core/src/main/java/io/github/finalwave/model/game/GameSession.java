package io.github.finalwave.model.game;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.BoardGameContext;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.boss.BossVfx;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.game.entity.plant.*;
import io.github.finalwave.model.game.IcebergFlashSystem;
import io.github.finalwave.model.game.PhatBeetPulseSystem;
import io.github.finalwave.model.game.KiwibeastPulseSystem;
import io.github.finalwave.model.game.TangleKelpGrabSystem;
import io.github.finalwave.model.game.entity.plant.ability.JalapenoFireSystem;
import io.github.finalwave.model.game.entity.projectile.ProjectileSystem;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieFactory;
import io.github.finalwave.model.item.PlantFoodDrop;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.minigame.GroundSeedPacket;
import io.github.finalwave.model.minigame.MiniGameHandler;
import io.github.finalwave.model.minigame.beghouled.BeghouledBoard;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapResult;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeCatalog;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeResult;
import io.github.finalwave.model.minigame.bowling.BowlingNutSystem;
import io.github.finalwave.model.quest.event.GameEventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public final class GameSession {

    public static final int TICKS_PER_SECOND = 10;
    public static final int MAX_PLANT_FOOD = 5;
    public static final String ROW_EFFECT_ICE_WIND = "ice-wind";
    public static final int ICE_WIND_DURATION_TICKS = 25;

    private final GameBoard board;
    private final BoardGameContext context;
    private final PlantFactory plantFactory;
    private final PlantRegistry plantRegistry;
    private final PlantArmor.PlantCooldownTracker cooldownTracker;
    private final ProjectileSystem projectileSystem;
    private final JalapenoFireSystem jalapenoFireSystem;
    private final TangleKelpGrabSystem tangleKelpGrabSystem;
    private final IcebergFlashSystem icebergFlashSystem;
    private final PhatBeetPulseSystem phatBeetPulseSystem;
    private final KiwibeastPulseSystem kiwibeastPulseSystem;
    private final CraterSystem craterSystem;
    private final GameEventBus eventBus;
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Zombie> pendingZombies = new ArrayList<>();
    private MiniGameHandler activeMiniGameHandler;
    private final Set<String> destroyedPlantIds = new HashSet<>();
    private final Set<String> killedZombieIds = new HashSet<>();
    private final List<LawnBurst> lawnBursts = new ArrayList<>();
    private final ZombieFactory zombieFactory;
    private final int zombieDifficulty;
    private final Random random;
    private Set<String> selectedLoadout = Set.of();
    private List<String> selectedLoadoutOrder = List.of();
    private String imitaterTargetSeed;
    private final GameSessionSpecialLevelState specialLevelState = new GameSessionSpecialLevelState();
    private final GameSessionMiniGameState miniGameState;
    private final GameSessionTileEffects tileEffects;
    private final GameSessionPlanting planting;
    private final GameSessionCombat combat;
    private SpecialLevelHandler activeSpecialLevelHandler;

    private final List<LawnMower> lawnMowers = new ArrayList<>();
    private WaveManager waveManager;
    private final SkySunSystem skySunSystem;
    private MatchListener matchListener;
    private MatchResult matchResult = MatchResult.IN_PROGRESS;
    private boolean wavesAutoStart = true;
    private boolean zombiesImmuneToChill;
    private boolean sandboxPractice;

    private int currentTick;
    private int sunBalance;
    private int plantFoodCount;
    private String chapterId = "default";
    private String levelId = "level";
    private boolean nightLevel;
    private int plantsLost;
    private int waveStartTick;
    private int firstWaveStartTick = -1;
    private int userDifficultyLevel = 3;
    private boolean running;
    private boolean tickingZombies;
    private io.github.finalwave.model.quest.QuestTracker attachedQuestTracker;

    public GameSession(PlantRegistry plantRegistry) {
        this(plantRegistry, new GameBoard(), 50);
    }

    public GameSession(PlantRegistry plantRegistry, int startingSun) {
        this(plantRegistry, new GameBoard(), startingSun);
    }

    public GameSession(PlantRegistry plantRegistry, GameBoard board, int startingSun) {
        this(plantRegistry, board, startingSun, (ZombieFactory) null, 1);
    }

    public GameSession(PlantRegistry plantRegistry, ZombieRegistry zombieRegistry) {
        this(plantRegistry, new GameBoard(), 50, new ZombieFactory(zombieRegistry), 1);
    }

    public GameSession(PlantRegistry plantRegistry, ZombieRegistry zombieRegistry,
                       int zombieDifficulty) {
        this(plantRegistry, new GameBoard(), 50,
                new ZombieFactory(zombieRegistry), zombieDifficulty);
    }

    public GameSession(PlantRegistry plantRegistry, GameBoard board, int startingSun,
                       ZombieRegistry zombieRegistry, int zombieDifficulty) {
        this(plantRegistry, board, startingSun,
                new ZombieFactory(zombieRegistry), zombieDifficulty);
    }

    public GameSession(PlantRegistry plantRegistry, GameBoard board, int startingSun,
                       ZombieRegistry zombieRegistry, int zombieDifficulty, Random random) {
        this(plantRegistry, board, startingSun,
                new ZombieFactory(zombieRegistry), zombieDifficulty, random);
    }

    public GameSession(PlantRegistry plantRegistry, GameBoard board, int startingSun,
                       ZombieFactory zombieFactory, int zombieDifficulty) {
        this(plantRegistry, board, startingSun, zombieFactory, zombieDifficulty, new Random());
    }

    public GameSession(PlantRegistry plantRegistry, GameBoard board, int startingSun,
                       ZombieFactory zombieFactory, int zombieDifficulty, Random random) {
        if (plantRegistry == null || board == null) {
            throw new IllegalArgumentException("registries and board must not be null");
        }
        if (zombieDifficulty < 1) {
            throw new IllegalArgumentException("zombieDifficulty must be at least 1");
        }
        this.plantRegistry = plantRegistry;
        this.board = board;
        this.zombieFactory = zombieFactory;
        this.zombieDifficulty = zombieDifficulty;
        this.random = random == null ? new Random() : random;
        this.plantFactory = new PlantFactory(plantRegistry);
        this.cooldownTracker = new PlantArmor.PlantCooldownTracker();
        this.projectileSystem = new ProjectileSystem();
        this.jalapenoFireSystem = new JalapenoFireSystem();
        this.tangleKelpGrabSystem = new TangleKelpGrabSystem();
        this.icebergFlashSystem = new IcebergFlashSystem();
        this.phatBeetPulseSystem = new PhatBeetPulseSystem();
        this.kiwibeastPulseSystem = new KiwibeastPulseSystem();
        this.craterSystem = new CraterSystem();
        this.eventBus = new GameEventBus();
        this.context = new BoardGameContext(this);
        this.sunBalance = startingSun;
        this.plantFoodCount = 0;
        this.skySunSystem = new SkySunSystem(this.random);
        this.miniGameState = new GameSessionMiniGameState(this, this.random);
        this.tileEffects = new GameSessionTileEffects(this);
        this.planting = new GameSessionPlanting(this);
        this.combat = new GameSessionCombat(this);
        initLawnMowers();
    }

    private void initLawnMowers() {
        lawnMowers.clear();
        for (int row = 0; row < board.getRows(); row++) {
            lawnMowers.add(new LawnMower(row));
        }
    }

    public BoardGameContext getContext() {
        return context;
    }

    public GameBoard getBoard() {
        return board;
    }

    public PlantRegistry getPlantRegistry() {
        return plantRegistry;
    }

    public PlantArmor.PlantCooldownTracker getCooldownTracker() {
        return cooldownTracker;
    }

    public ProjectileSystem getProjectileSystem() {
        return projectileSystem;
    }

    public JalapenoFireSystem getJalapenoFireSystem() {
        return jalapenoFireSystem;
    }

    public TangleKelpGrabSystem getTangleKelpGrabSystem() {
        return tangleKelpGrabSystem;
    }

    public IcebergFlashSystem getIcebergFlashSystem() {
        return icebergFlashSystem;
    }

    public PhatBeetPulseSystem getPhatBeetPulseSystem() {
        return phatBeetPulseSystem;
    }

    public KiwibeastPulseSystem getKiwibeastPulseSystem() {
        return kiwibeastPulseSystem;
    }

    public CraterSystem getCraterSystem() {
        return craterSystem;
    }

    public GameEventBus getEventBus() {
        return eventBus;
    }

    public List<Zombie> getZombies() {
        if (pendingZombies.isEmpty()) {
            return List.copyOf(zombies);
        }
        List<Zombie> all = new ArrayList<>(zombies);
        all.addAll(pendingZombies);
        return List.copyOf(all);
    }

    public List<Sun> getSunItems() {
        return planting.getSunItems();
    }

    public List<PlantFoodDrop> getPlantFoodDrops() {
        return planting.getPlantFoodDrops();
    }

    public List<PlantCovering> getPlantCoverings() {
        return tileEffects.getPlantCoverings();
    }

    public List<GooPuddle> getGooPuddles() {
        return tileEffects.getGooPuddles();
    }

    public void addGooLaneTrail(Plant plant, int durationTicks) {
        tileEffects.addGooLaneTrail(plant, durationTicks);
    }

    public void addGooPuddle(int col, int row, int durationTicks) {
        tileEffects.addGooPuddle(col, row, durationTicks);
    }

    public List<ArcadeObstacle> getArcadeObstacles() {
        return tileEffects.getArcadeObstacles();
    }

    public List<PianoObstacle> getPianoObstacles() {
        return tileEffects.getPianoObstacles();
    }

    public List<PendingGraveLanding> getPendingGraveLandings() {
        return tileEffects.getPendingGraveLandings();
    }

    public List<Vase> getVases() {
        return planting.getVases();
    }

    public void addVase(Vase vase) {
        planting.addVase(vase);
    }

    public Vase getVaseAt(int col, int row) {
        return planting.getVaseAt(col, row);
    }

    public boolean smashVase(int col, int row) {
        return planting.smashVase(col, row);
    }

    public boolean areAllVasesSmashed() {
        return planting.areAllVasesSmashed();
    }

    public List<GroundSeedPacket> getGroundSeedPackets() {
        return planting.getGroundSeedPackets();
    }

    public void setSeedPacketExpiryTicks(int seedPacketExpiryTicks) {
        planting.setSeedPacketExpiryTicks(seedPacketExpiryTicks);
    }

    public int getSeedPacketExpiryTicks() {
        return planting.getSeedPacketExpiryTicks();
    }

    public void addGroundSeedPacket(String plantName, int col, int row) {
        planting.addGroundSeedPacket(plantName, col, row);
    }

    public GroundSeedPacket getGroundSeedPacketAt(int col, int row) {
        return planting.getGroundSeedPacketAt(col, row);
    }

    public PlantPlacementResult plantFromSeedPacket(int col, int row) {
        return planting.plantFromSeedPacket(col, row);
    }

    public PlantPlacementResult plantFromSeedPacket(String plantName, int col, int row) {
        return planting.plantFromSeedPacket(plantName, col, row);
    }

    public void setActiveMiniGameHandler(MiniGameHandler handler) {
        this.activeMiniGameHandler = handler;
    }

    public MiniGameHandler getActiveMiniGameHandler() {
        return activeMiniGameHandler;
    }

    public Random getRandom() {
        return random;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getSunBalance() {
        return sunBalance;
    }

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void setPlantFoodCount(int plantFoodCount) {
        this.plantFoodCount = Math.max(0, Math.min(MAX_PLANT_FOOD, plantFoodCount));
    }

    public void addPlantFood(int amount) {
        plantFoodCount = Math.min(MAX_PLANT_FOOD, plantFoodCount + Math.max(0, amount));
    }

    public void setSelectedLoadout(Set<String> plantNames) {
        selectedLoadout = plantNames == null ? Set.of() : Set.copyOf(plantNames);
    }

    public void setSelectedLoadoutOrder(List<String> plantNames) {
        selectedLoadoutOrder = plantNames == null ? List.of() : List.copyOf(plantNames);
    }

    public List<String> getSelectedLoadoutOrder() {
        return selectedLoadoutOrder;
    }

    public void setSelectedLoadout(Set<String> plantNames, List<String> order) {
        setSelectedLoadout(plantNames);
        setSelectedLoadoutOrder(order);
    }

    public Set<String> getSelectedLoadout() {
        return selectedLoadout;
    }

    public void noteImitaterTargetSeed(String plantName) {
        if (plantName == null || plantName.isBlank() || "Imitater".equals(plantName)) {
            return;
        }
        if (plantRegistry.getDefinition(plantName) == null) {
            return;
        }
        imitaterTargetSeed = plantName;
    }

    public String getImitaterTargetSeed() {
        return imitaterTargetSeed;
    }

    public void setActiveSpecialLevelHandler(SpecialLevelHandler handler) {
        this.activeSpecialLevelHandler = handler;
    }

    public SpecialLevelHandler getActiveSpecialLevelHandler() {
        return activeSpecialLevelHandler;
    }

    public void activateConveyorBelt() {
        specialLevelState.activateConveyorBelt();
    }

    public boolean isConveyorBeltActive() {
        return specialLevelState.isConveyorBeltActive();
    }

    public void addConveyorBeltPlant(String plantName) {
        specialLevelState.addConveyorBeltPlant(plantName);
    }

    public List<String> getConveyorBeltPlants() {
        return specialLevelState.getConveyorBeltPlants();
    }

    boolean hasConveyorBeltPlant(String plantName) {
        return specialLevelState.hasConveyorBeltPlant(plantName);
    }

    void removeConveyorBeltPlant(String plantName) {
        specialLevelState.removeConveyorBeltPlant(plantName);
    }

    public void activateWalnutBowling(int redLineColumn) {
        miniGameState.activateWalnutBowling(redLineColumn);
    }

    public boolean isWalnutBowlingActive() {
        return miniGameState.isWalnutBowlingActive();
    }

    public int getWalnutBowlingRedLineColumn() {
        return miniGameState.getWalnutBowlingRedLineColumn();
    }

    public BowlingNutSystem getBowlingNutSystem() {
        return miniGameState.getBowlingNutSystem();
    }

    public PlantPlacementResult tryPlantBowlingNut(String plantName, int col, int row) {
        return miniGameState.tryPlantBowlingNut(plantName, col, row);
    }

    public void activateIZombie(int placementColumn,
                                List<String> zombiePool,
                                Map<String, Integer> zombieCosts) {
        miniGameState.activateIZombie(placementColumn, zombiePool, zombieCosts);
    }

    public void setIZombieRoster(List<String> zombiePool, Map<String, Integer> zombieCosts) {
        miniGameState.setIZombieRoster(zombiePool, zombieCosts);
    }

    public boolean isIZombieActive() {
        return miniGameState.isIZombieActive();
    }

    public int getIZombiePlacementColumn() {
        return miniGameState.getIZombiePlacementColumn();
    }

    public List<String> getIZombieZombiePool() {
        return miniGameState.getIZombieZombiePool();
    }

    public Map<String, Integer> getIZombieZombieCosts() {
        return miniGameState.getIZombieZombieCosts();
    }

    public boolean isIZombieBrainEaten(int row) {
        return miniGameState.isIZombieBrainEaten(row);
    }

    public int getIZombieBrainsEatenCount() {
        return miniGameState.getIZombieBrainsEatenCount();
    }

    public boolean areAllIZombieBrainsEaten() {
        return miniGameState.areAllIZombieBrainsEaten();
    }

    public void syncIZombieBrainsFromNetwork(boolean[] eaten) {
        miniGameState.syncIZombieBrainsFromNetwork(eaten);
    }

    public int getIZombieSunBalance() {
        return miniGameState.getIZombieSunBalance();
    }

    public void setIZombieSunBalance(int amount) {
        miniGameState.setIZombieSunBalance(amount);
    }

    public void addIZombieSunBalance(int amount) {
        miniGameState.addIZombieSunBalance(amount);
    }

    public void withdrawIZombieSun(int amount) {
        miniGameState.withdrawIZombieSun(amount);
    }

    public void syncNetworkTick(long tick) {
        int next = (int) Math.max(0L, tick);
        if (next > currentTick) {
            currentTick = next;
        }
    }

    public void advanceGuestDisplayTicks(int count) {
        if (count > 0) {
            currentTick += count;
        }
    }

    public PlantPlacementResult tryPlaceZombie(String alias, int col, int row) {
        return miniGameState.tryPlaceZombie(alias, col, row);
    }

    public Plant placeDefensePlant(String plantName, int col, int row) {
        return planting.placeDefensePlant(plantName, col, row);
    }

    public int getIZombieCheapestRosterCost() {
        return miniGameState.getIZombieCheapestRosterCost();
    }

    public void activateBeghouled(List<String> plantPool,
                                  int matchTarget,
                                  BeghouledUpgradeCatalog catalog) {
        miniGameState.activateBeghouled(plantPool, matchTarget, catalog);
    }

    public boolean isBeghouledActive() {
        return miniGameState.isBeghouledActive();
    }

    public BeghouledBoard getBeghouledBoard() {
        return miniGameState.getBeghouledBoard();
    }

    public int getBeghouledMatchTarget() {
        return miniGameState.getBeghouledMatchTarget();
    }

    public BeghouledSwapResult trySwapBeghouledPlants(int colA, int rowA, int colB, int rowB) {
        return miniGameState.trySwapBeghouledPlants(colA, rowA, colB, rowB);
    }

    public BeghouledUpgradeResult tryBeghouledUpgrade(String plantName) {
        return miniGameState.tryBeghouledUpgrade(plantName);
    }

    public void activateLockedPlants(LockedPlantsRules rules) {
        specialLevelState.activateLockedPlants(rules);
    }

    public Set<String> getLevelLockedPlants() {
        return specialLevelState.getLevelLockedPlants();
    }

    public boolean isLevelLockedPlant(String plantName) {
        return specialLevelState.isLevelLockedPlant(plantName);
    }

    public Plant placeProtectedSeed(String plantName, int col, int row) {
        return planting.placeProtectedSeed(plantName, col, row);
    }

    public boolean isProtectedSeed(Plant plant) {
        return plant != null && specialLevelState.isProtectedSeedId(plant.getId());
    }

    public List<SeedPlacement> getProtectedSeedPlacements() {
        return specialLevelState.getProtectedSeedPlacements();
    }

    public List<Integer> getDangerRows() {
        return specialLevelState.getDangerRows();
    }

    public void activateTimedWar(TimedWarRules rules) {
        specialLevelState.activateTimedWar(rules);
    }

    public boolean isTimedWarActive() {
        return specialLevelState.isTimedWarActive();
    }

    public TimedWarRules getTimedWarRules() {
        return specialLevelState.getTimedWarRules();
    }

    public int getTimedWarProgress() {
        return specialLevelState.getTimedWarProgress();
    }

    public int getTimedWarRemainingTicks() {
        return specialLevelState.getTimedWarRemainingTicks();
    }

    public boolean isTimedWarGoalMet() {
        return specialLevelState.isTimedWarGoalMet();
    }

    public void advanceTimedWarTick() {
        specialLevelState.advanceTimedWarTick();
    }

    public void activateDeadLine(int column) {
        if (column < 0 || column >= board.getCols()) {
            throw new IllegalArgumentException(
                    "dead line column must be between 0 and " + (board.getCols() - 1));
        }
        specialLevelState.activateDeadLine(column);
    }

    public boolean isDeadLineActive() {
        return specialLevelState.isDeadLineActive();
    }

    public int getDeadLineColumn() {
        return specialLevelState.getDeadLineColumn();
    }

    public void activateLoveYourPlants(int maxLoss) {
        specialLevelState.activateLoveYourPlants(maxLoss);
    }

    public boolean isLoveYourPlantsActive() {
        return specialLevelState.isLoveYourPlantsActive();
    }

    public int getLoveYourPlantsMaxLoss() {
        return specialLevelState.getLoveYourPlantsMaxLoss();
    }

    public int getLoveYourPlantsRemaining() {
        return specialLevelState.getLoveYourPlantsRemaining(plantsLost);
    }

    public void activatePlantWhatYouGet(int startingSun) {
        if (startingSun < 0) {
            throw new IllegalArgumentException("startingSun must not be negative");
        }
        setSunBalance(startingSun);
        skySunSystem.setEnabled(false);
        setWavesAutoStart(false);
        specialLevelState.activatePlantWhatYouGet();
    }

    public boolean isPlantWhatYouGetActive() {
        return specialLevelState.isPlantWhatYouGetActive();
    }

    public boolean isPrepPhaseActive() {
        return specialLevelState.isPrepPhaseActive();
    }

    public void endPrepPhase() {
        specialLevelState.endPrepPhase();
    }

    public void activateBoss(int maxHealth) {
        specialLevelState.activateBoss(maxHealth);
    }

    public boolean isBossActive() {
        return specialLevelState.isBossActive();
    }

    public void syncBossHud(int phase, int health, int maxHealth) {
        specialLevelState.syncBoss(phase, health, maxHealth);
    }

    public int getBossPhase() {
        return specialLevelState.getBossPhase();
    }

    public int getBossHealth() {
        return specialLevelState.getBossHealth();
    }

    public int getBossMaxHealth() {
        return specialLevelState.getBossMaxHealth();
    }

    public void addBossVfx(BossVfx vfx) {
        specialLevelState.addBossVfx(vfx);
    }

    public List<BossVfx> drainBossVfx() {
        return specialLevelState.drainBossVfx();
    }

    public void queueLawnBurst(LawnBurst burst) {
        if (burst != null) {
            lawnBursts.add(burst);
        }
    }

    public List<LawnBurst> drainLawnBursts() {
        if (lawnBursts.isEmpty()) {
            return List.of();
        }
        List<LawnBurst> copy = List.copyOf(lawnBursts);
        lawnBursts.clear();
        return copy;
    }

    public void restoreProgress(int tick, int lostPlants, int sun, int food) {
        currentTick = Math.max(0, tick);
        plantsLost = Math.max(0, lostPlants);
        setSunBalance(sun);
        setPlantFoodCount(food);
    }

    public void clearLivingUnits() {
        for (Plant plant : List.copyOf(board.getAllPlants())) {
            board.removePlant(plant);
        }
        zombies.clear();
        pendingZombies.clear();
    }

    public Plant restorePlant(String plantName, int level, int col, int row, int health, boolean armed) {
        try {
            Plant plant = plantFactory.create(plantName, Math.max(1, level), col, row);
            board.placePlant(plant);
            plant.initializeCooldown(TICKS_PER_SECOND);
            plant.setArmedTrap(armed);
            plant.restoreHealth(health);
            return plant;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public Zombie restoreZombie(String alias, int row, double x, int health, int freezeTicks) {
        try {
            Zombie zombie = spawnZombieOfType(alias, row, x);
            if (zombie == null) {
                return null;
            }
            zombie.restoreHealth(health);
            if (freezeTicks > 0) {
                zombie.applyFreeze(freezeTicks);
            }
            return zombie;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public void restoreConveyorBelt(List<String> plants) {
        specialLevelState.replaceConveyorBeltPlants(plants);
    }

    public void setSunBalance(int amount) {
        sunBalance = Math.max(0, amount);
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId == null ? "level" : levelId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setNightLevel(boolean nightLevel) {
        this.nightLevel = nightLevel;
    }

    public boolean isNightLevel() {
        return nightLevel;
    }

    public int getPlantsLost() {
        return plantsLost;
    }

    public void markWaveStarted() {
        waveStartTick = currentTick;
        if (firstWaveStartTick < 0) {
            firstWaveStartTick = currentTick;
        }
    }

    public int getWaveStartTick() {
        return waveStartTick;
    }

    public int getFirstWaveStartTick() {
        return firstWaveStartTick < 0 ? waveStartTick : firstWaveStartTick;
    }

    public int getUserDifficultyLevel() {
        return userDifficultyLevel;
    }

    public void attachQuestTracker(io.github.finalwave.model.quest.QuestTracker questTracker) {
        this.attachedQuestTracker = questTracker;
    }

    public MatchListener getMatchListener() {
        return matchListener;
    }

    public void setMatchListener(MatchListener matchListener) {
        this.matchListener = matchListener;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public List<LawnMower> getLawnMowers() {
        return List.copyOf(lawnMowers);
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public void setWaveManager(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    public SkySunSystem getSkySunSystem() {
        return skySunSystem;
    }

    public void setWavesAutoStart(boolean wavesAutoStart) {
        this.wavesAutoStart = wavesAutoStart;
    }

    public boolean isWavesAutoStart() {
        return wavesAutoStart;
    }

    public void setZombiesImmuneToChill(boolean zombiesImmuneToChill) {
        this.zombiesImmuneToChill = zombiesImmuneToChill;
    }

    public boolean areZombiesImmuneToChill() {
        return zombiesImmuneToChill;
    }

    public void enableSandboxPractice() {
        sandboxPractice = true;
        board.setSandboxAquaticOnLand(true);
        setWavesAutoStart(false);
    }

    public boolean isSandboxPractice() {
        return sandboxPractice;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        combat.start();
    }

    public void stop() {
        running = false;
    }

    public void advanceTicks(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("tick count must be non-negative");
        }
        for (int i = 0; i < count; i++) {
            if (!running) {
                break;
            }
            tick();
        }
    }

    public void startZombieWaves() {
        combat.startZombieWaves();
    }

    public PlantPlacementResult tryPlant(String plantName, int col, int row, int level) {
        return planting.tryPlant(plantName, col, row, level);
    }

    public boolean collectSun(Sun sun) {
        return planting.collectSun(sun);
    }

    public boolean collectSunAt(int col, int row) {
        return planting.collectSunAt(col, row);
    }

    public void spawnPlantFoodDrop(int col, int row, double worldX) {
        planting.spawnPlantFoodDrop(col, row, worldX);
    }

    public boolean collectPlantFood(PlantFoodDrop drop) {
        return planting.collectPlantFood(drop);
    }

    public boolean collectPlantFoodAt(int col, int row) {
        return planting.collectPlantFoodAt(col, row);
    }

    public boolean usePlantFood(int col, int row) {
        return planting.usePlantFood(col, row);
    }

    public void addZombie(Zombie zombie) {
        combat.addZombie(zombie);
    }

    public Zombie spawnZombieOfType(String alias, int row, double x) {
        return combat.spawnZombieOfType(alias, row, x);
    }

    public ZombieFactory getZombieFactory() {
        return zombieFactory;
    }

    public int getZombieDifficulty() {
        return zombieDifficulty;
    }

    public void spawnSkySun(int col, int row, int value) {
        planting.spawnSkySun(col, row, value);
    }

    public void tick() {
        combat.runTick();
    }

    public void spawnSunItem(Sun sun) {
        planting.spawnSunItem(sun);
    }

    public Plant createClone(Plant source, int col, int row) {
        return planting.createClone(source, col, row);
    }

    public void morphImitater(Plant imitater) {
        planting.morphImitater(imitater);
    }

    public Plant createDoomShroomSeedling(Plant source, int col, int row) {
        return planting.createDoomShroomSeedling(source, col, row);
    }

    public Plant createPlantFoodClone(Plant source, int col, int row) {
        return planting.createPlantFoodClone(source, col, row);
    }

    public void addSunBalance(int amount) {
        sunBalance += amount;
    }

    public int withdrawSun(int amount) {
        int withdrawn = Math.min(sunBalance, Math.max(0, amount));
        sunBalance -= withdrawn;
        return withdrawn;
    }

    public int stealGroundSun(int maximum) {
        return planting.stealGroundSun(null, maximum);
    }

    public int stealGroundSun(Zombie thief, int maximum) {
        return planting.stealGroundSun(thief, maximum);
    }

    public boolean removePlantFromBoard(Plant plant) {
        return removePlantFromBoard(plant, true);
    }

    public boolean removePlantFromBoard(Plant plant, boolean countsAsLoss) {
        return planting.removePlantFromBoard(plant, countsAsLoss);
    }

    public void handleZombieKilled(Zombie zombie) {
        handleZombieKilled(zombie, null, null);
    }

    public void handleZombieKilled(Zombie zombie, String killerPlantType) {
        handleZombieKilled(zombie, killerPlantType, null);
    }

    public void handleProjectileKill(Zombie zombie, String killerPlantType, String projectileId) {
        handleZombieKilled(zombie, killerPlantType, projectileId);
    }

    public void handleZombieKilled(Zombie zombie, String killerPlantType, String projectileId) {
        combat.handleZombieKilled(zombie, killerPlantType, projectileId);
    }

    public void handleZombieReachedHouse(Zombie zombie) {
        combat.handleZombieReachedHouse(zombie);
    }

    public void despawnWalkOffZombie(Zombie zombie) {
        if (zombie == null) {
            return;
        }
        zombies.remove(zombie);
        pendingZombies.remove(zombie);
    }

    public void winMatch() {
        combat.winMatch();
    }

    public void loseMatch() {
        combat.loseMatch();
    }

    public boolean pluckPlant(int col, int row) {
        Plant plant = board.getPlantAt(col, row);
        if (plant == null) {
            return false;
        }
        if (isProtectedSeed(plant)) {
            return false;
        }
        return removePlantFromBoard(plant, false);
    }

    public void nukeAllZombies() {
        combat.nukeAllZombies();
    }

    public void removeAllCooldowns() {
        cooldownTracker.resetAll();
    }

    public String renderMap() {
        return MapRenderer.render(this);
    }

    public String renderPlantsStatus() {
        return MapRenderer.plantsStatus(this);
    }

    public String renderTileStatus(int col, int row) {
        return MapRenderer.tileStatus(this, col, row);
    }

    public void applyUserDifficulty(int difficultyLevel) {
        int dl = Math.max(1, Math.min(5, difficultyLevel));
        userDifficultyLevel = dl;
        skySunSystem.setDifficultyScale(WaveManager.skySunIntervalScale(dl));
        if (waveManager != null) {
            waveManager.setWaveCostDifficultyScale(WaveManager.waveCostScale(dl));
        }
    }

    public PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        return tileEffects.coverPlant(plant, type, health);
    }

    public PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health, Zombie source) {
        return tileEffects.coverPlant(plant, type, health, source);
    }

    public void registerHunterIceHit(Plant plant) {
        tileEffects.registerHunterIceHit(plant);
    }

    public void addPlantFrostStack(Plant plant) {
        tileEffects.addPlantFrostStack(plant);
    }

    public void clearGraveAt(int col, int row) {
        tileEffects.clearGraveAt(col, row);
    }

    public boolean damageGraveAt(int col, int row, int amount) {
        return tileEffects.damageGraveAt(col, row, amount);
    }

    public boolean damageIceAt(int col, int row, int amount) {
        return tileEffects.damageIceAt(col, row, amount);
    }

    public void clearIceAt(int col, int row) {
        tileEffects.clearIceAt(col, row);
    }

    public void pushArcadeObstacle(Zombie pusher) {
        tileEffects.pushArcadeObstacle(pusher);
    }

    public void releaseArcadeObstacle(String pusherId) {
        tileEffects.releaseArcadeObstacle(pusherId);
    }

    public void pushPianoObstacle(Zombie pusher) {
        tileEffects.pushPianoObstacle(pusher);
    }

    public void releasePianoObstacle(String pusherId) {
        tileEffects.releasePianoObstacle(pusherId);
    }

    public void queueGraveLanding(PendingGraveLanding landing) {
        tileEffects.queueGraveLanding(landing);
    }

    public void queueLaneLaser(int row, int fromCol, int span, int delayTicks) {
        tileEffects.queueLaneLaser(row, fromCol, span, delayTicks);
    }

    public void queueLaneLaser(int row, int fromCol, int span, int delayTicks, double originX) {
        tileEffects.queueLaneLaser(row, fromCol, span, delayTicks, originX);
    }

    public long nextBoneId() {
        return tileEffects.nextBoneId();
    }

    public boolean hasPendingGraveAt(int col, int row) {
        return tileEffects.hasPendingGraveAt(col, row);
    }

    public int pendingGraveCount() {
        return tileEffects.pendingGraveCount();
    }

    public void resetFamilyCooldowns(PlantCategory category) {
        tileEffects.resetFamilyCooldowns(category);
    }

    public void boostFamily(PlantCategory category, double durationSeconds) {
        tileEffects.boostFamily(category, durationSeconds);
    }

    public boolean isFamilyBoosted(PlantCategory category) {
        return tileEffects.isFamilyBoosted(category);
    }

    public void applyFieldModifier(int row, double magnitude, double durationSeconds) {
        tileEffects.applyFieldModifier(row, magnitude, durationSeconds);
    }

    public double getFieldModifier(int row) {
        return tileEffects.getFieldModifier(row);
    }

    public void applyRowEffect(int row, String effectType, int durationTicks) {
        tileEffects.applyRowEffect(row, effectType, durationTicks);
    }

    public boolean isRowEffectActive(int row, String effectType) {
        return tileEffects.isRowEffectActive(row, effectType);
    }

    // --- package-private support for extracted helpers ---

    GameSessionSpecialLevelState getSpecialLevelState() {
        return specialLevelState;
    }

    GameSessionMiniGameState getMiniGameState() {
        return miniGameState;
    }

    GameSessionTileEffects getTileEffects() {
        return tileEffects;
    }

    GameSessionPlanting getPlanting() {
        return planting;
    }

    GameSessionCombat getCombat() {
        return combat;
    }

    PlantFactory getPlantFactory() {
        return plantFactory;
    }

    List<Zombie> zombieList() {
        return zombies;
    }

    List<Zombie> pendingZombieList() {
        return pendingZombies;
    }

    List<LawnMower> lawnMowerList() {
        return lawnMowers;
    }

    Set<String> killedZombieIds() {
        return killedZombieIds;
    }

    Set<String> destroyedPlantIds() {
        return destroyedPlantIds;
    }

    boolean isTickingZombies() {
        return tickingZombies;
    }

    void setTickingZombies(boolean tickingZombies) {
        this.tickingZombies = tickingZombies;
    }

    void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    void incrementCurrentTick() {
        currentTick++;
    }

    io.github.finalwave.model.quest.QuestTracker getAttachedQuestTracker() {
        return attachedQuestTracker;
    }

    void incrementPlantsLost() {
        plantsLost++;
    }

    void consumePlantFood() {
        if (plantFoodCount > 0) {
            plantFoodCount--;
        }
    }
}
