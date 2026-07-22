package model.game;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.definition.plant.PlantDefinition;
import model.game.board.BoardGameContext;
import model.game.board.GameBoard;
import model.game.board.PlantPlacementResult;
import model.game.board.tile.GraveTile;
import model.game.entity.Vase;
import model.game.entity.plant.*;
import model.game.entity.plant.ability.ExplosiveAbility;
import model.game.entity.projectile.ProjectileSystem;
import model.game.entity.zombie.ArcadeObstacle;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieFactory;
import model.item.Sun;
import model.minigame.GroundSeedPacket;
import model.minigame.MiniGameHandler;
import model.quest.event.GameEvent;
import model.quest.event.GameEventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Random;

public final class GameSession {

    public static final int TICKS_PER_SECOND = 10;

    private final GameBoard board;
    private final BoardGameContext context;
    private final PlantFactory plantFactory;
    private final PlantRegistry plantRegistry;
    private final PlantArmor.PlantCooldownTracker cooldownTracker;
    private final ProjectileSystem projectileSystem;
    private final GameEventBus eventBus;
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Zombie> pendingZombies = new ArrayList<>();
    private final List<PlantCovering> plantCoverings = new ArrayList<>();
    private final List<ArcadeObstacle> arcadeObstacles = new ArrayList<>();
    private final List<Sun> sunItems = new ArrayList<>();
    private final List<Vase> vases = new ArrayList<>();
    private final List<GroundSeedPacket> groundSeedPackets = new ArrayList<>();
    private int seedPacketExpiryTicks = 100;
    private MiniGameHandler activeMiniGameHandler;
    private final Set<String> destroyedPlantIds = new HashSet<>();
    private final Set<String> killedZombieIds = new HashSet<>();
    private final Map<PlantCategory, Integer> familyBoostEndTicks = new HashMap<>();
    private final Map<Integer, FieldModifier> rowModifiers = new HashMap<>();
    private final Map<Integer, Map<String, Integer>> rowEffects = new HashMap<>();
    private final ZombieFactory zombieFactory;
    private final int zombieDifficulty;
    private final Random random;
    private Set<String> selectedLoadout = Set.of();
    private final List<String> conveyorBeltPlants = new ArrayList<>();
    private boolean conveyorBeltActive;
    private final Set<String> levelLockedPlants = new HashSet<>();
    private final Set<String> protectedSeedPlantIds = new HashSet<>();
    private final List<SeedPlacement> protectedSeedPlacements = new ArrayList<>();
    private boolean timedWarActive;
    private TimedWarRules timedWarRules;
    private int timedWarTicksElapsed;
    private int timedWarProgress;
    private Integer deadLineColumn;
    private Integer loveYourPlantsMaxLoss;
    private boolean plantWhatYouGetActive;
    private boolean prepPhaseActive;
    private SpecialLevelHandler activeSpecialLevelHandler;

    private final List<LawnMower> lawnMowers = new ArrayList<>();
    private WaveManager waveManager;
    private final SkySunSystem skySunSystem;
    private MatchListener matchListener;
    private MatchResult matchResult = MatchResult.IN_PROGRESS;
    private boolean wavesAutoStart = true;
    private boolean zombiesImmuneToChill;

    private int currentTick;
    private int sunBalance;
    private int plantFoodCount;
    private String chapterId = "default";
    private String levelId = "level";
    private boolean nightLevel;
    private int plantsLost;
    private int waveStartTick;
    private boolean running;
    private boolean tickingZombies;
    private model.quest.QuestTracker attachedQuestTracker;

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
        this.eventBus = new GameEventBus();
        this.context = new BoardGameContext(this);
        this.sunBalance = startingSun;
        this.plantFoodCount = 0;
        this.skySunSystem = new SkySunSystem(this.random);
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
        return List.copyOf(sunItems);
    }

    public List<PlantCovering> getPlantCoverings() {
        return List.copyOf(plantCoverings);
    }

    public List<ArcadeObstacle> getArcadeObstacles() {
        return List.copyOf(arcadeObstacles);
    }

    public List<Vase> getVases() {
        return List.copyOf(vases);
    }

    public void addVase(Vase vase) {
        if (vase != null && vase.isAlive()) {
            vases.add(vase);
        }
    }

    public Vase getVaseAt(int col, int row) {
        for (Vase vase : vases) {
            if (vase.isAlive()
                    && (int) Math.floor(vase.getX()) == col
                    && (int) Math.floor(vase.getY()) == row) {
                return vase;
            }
        }
        return null;
    }

    public boolean smashVase(int col, int row) {
        Vase vase = getVaseAt(col, row);
        if (vase == null) {
            return false;
        }
        Vase.Content content = vase.getContent();
        vase.smash(context);
        vases.remove(vase);
        if (matchListener != null) {
            matchListener.onVaseSmashed(col, row, content);
        }
        if (activeMiniGameHandler != null) {
            activeMiniGameHandler.onTick(this);
        }
        return true;
    }

    public boolean areAllVasesSmashed() {
        return vases.isEmpty() || vases.stream().noneMatch(Vase::isAlive);
    }

    public List<GroundSeedPacket> getGroundSeedPackets() {
        return List.copyOf(groundSeedPackets);
    }

    public void setSeedPacketExpiryTicks(int seedPacketExpiryTicks) {
        this.seedPacketExpiryTicks = Math.max(1, seedPacketExpiryTicks);
    }

    public int getSeedPacketExpiryTicks() {
        return seedPacketExpiryTicks;
    }

    public void addGroundSeedPacket(String plantName, int col, int row) {
        if (plantName == null || plantName.isBlank() || !board.inBounds(col, row)) {
            return;
        }
        groundSeedPackets.removeIf(packet -> packet.col() == col && packet.row() == row);
        GroundSeedPacket packet = new GroundSeedPacket(
                plantName, col, row, currentTick + seedPacketExpiryTicks);
        groundSeedPackets.add(packet);
        if (matchListener != null) {
            matchListener.onSeedPacketDropped(plantName, col, row);
        }
    }

    public GroundSeedPacket getGroundSeedPacketAt(int col, int row) {
        for (GroundSeedPacket packet : groundSeedPackets) {
            if (packet.col() == col && packet.row() == row) {
                return packet;
            }
        }
        return null;
    }

    public PlantPlacementResult plantFromSeedPacket(int col, int row) {
        GroundSeedPacket packet = getGroundSeedPacketAt(col, row);
        if (packet == null) {
            return PlantPlacementResult.NO_SEED_PACKET;
        }
        PlantDefinition definition = plantRegistry.getDefinition(packet.plantName());
        if (definition == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (getVaseAt(col, row) != null) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        PlantPlacementResult placement = board.canPlace(definition, col, row);
        if (placement != PlantPlacementResult.SUCCESS) {
            return placement;
        }
        Plant plant = plantFactory.create(definition, 1, col, row);
        board.placePlant(plant);
        plant.onPlanted(context);
        groundSeedPackets.remove(packet);
        if (matchListener != null) {
            matchListener.onSeedPacketPlanted(packet.plantName(), col, row);
        }
        eventBus.publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getCategory().name(),
                col,
                row,
                plant.hasTag(PlantTag.NIGHT) || plant.hasTag(PlantTag.SHROOM)));
        return PlantPlacementResult.SUCCESS;
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
        this.plantFoodCount = Math.max(0, plantFoodCount);
    }

    public void addPlantFood(int amount) {
        plantFoodCount += Math.max(0, amount);
    }

    public void setSelectedLoadout(Set<String> plantNames) {
        selectedLoadout = plantNames == null ? Set.of() : Set.copyOf(plantNames);
    }

    public Set<String> getSelectedLoadout() {
        return selectedLoadout;
    }

    public void setActiveSpecialLevelHandler(SpecialLevelHandler handler) {
        this.activeSpecialLevelHandler = handler;
    }

    public SpecialLevelHandler getActiveSpecialLevelHandler() {
        return activeSpecialLevelHandler;
    }

    public void activateConveyorBelt() {
        conveyorBeltActive = true;
    }

    public boolean isConveyorBeltActive() {
        return conveyorBeltActive;
    }

    public void addConveyorBeltPlant(String plantName) {
        if (plantName != null) {
            conveyorBeltPlants.add(plantName);
        }
    }

    public List<String> getConveyorBeltPlants() {
        return List.copyOf(conveyorBeltPlants);
    }

    public void activateLockedPlants(LockedPlantsRules rules) {
        levelLockedPlants.clear();
        if (rules != null) {
            levelLockedPlants.addAll(rules.getLockedPlants());
        }
    }

    public Set<String> getLevelLockedPlants() {
        return Set.copyOf(levelLockedPlants);
    }

    public boolean isLevelLockedPlant(String plantName) {
        return plantName != null && levelLockedPlants.contains(plantName);
    }

    public Plant placeProtectedSeed(String plantName, int col, int row) {
        PlantDefinition definition = plantRegistry.getDefinition(plantName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown plant: " + plantName);
        }
        Plant plant = plantFactory.create(definition, 1, col, row);
        board.placePlant(plant);
        plant.onPlanted(context);
        protectedSeedPlantIds.add(plant.getId());
        protectedSeedPlacements.add(new SeedPlacement(plantName, col, row));
        return plant;
    }

    public boolean isProtectedSeed(Plant plant) {
        return plant != null && protectedSeedPlantIds.contains(plant.getId());
    }

    public List<SeedPlacement> getProtectedSeedPlacements() {
        return List.copyOf(protectedSeedPlacements);
    }

    public List<Integer> getDangerRows() {
        Set<Integer> rows = new LinkedHashSet<>();
        for (SeedPlacement placement : protectedSeedPlacements) {
            rows.add(placement.getRow());
        }
        return List.copyOf(rows);
    }

    public void activateTimedWar(TimedWarRules rules) {
        if (rules == null || !rules.isActiveRules()) {
            timedWarActive = false;
            timedWarRules = null;
            timedWarTicksElapsed = 0;
            timedWarProgress = 0;
            return;
        }
        timedWarActive = true;
        timedWarRules = rules;
        timedWarTicksElapsed = 0;
        timedWarProgress = 0;
    }

    public boolean isTimedWarActive() {
        return timedWarActive;
    }

    public TimedWarRules getTimedWarRules() {
        return timedWarRules;
    }

    public int getTimedWarProgress() {
        return timedWarProgress;
    }

    public int getTimedWarRemainingTicks() {
        if (!timedWarActive || timedWarRules == null) {
            return 0;
        }
        return Math.max(0, timedWarRules.getDurationTicks() - timedWarTicksElapsed);
    }

    public boolean isTimedWarGoalMet() {
        return timedWarActive && timedWarRules != null && timedWarRules.isGoalMet(timedWarProgress);
    }

    public void advanceTimedWarTick() {
        if (timedWarActive) {
            timedWarTicksElapsed++;
        }
    }

    public void activateDeadLine(int column) {
        if (column < 0 || column >= board.getCols()) {
            throw new IllegalArgumentException(
                    "dead line column must be between 0 and " + (board.getCols() - 1));
        }
        deadLineColumn = column;
    }

    public boolean isDeadLineActive() {
        return deadLineColumn != null;
    }

    public int getDeadLineColumn() {
        if (deadLineColumn == null) {
            throw new IllegalStateException("dead line is not active");
        }
        return deadLineColumn;
    }

    public void activateLoveYourPlants(int maxLoss) {
        if (maxLoss < 1) {
            throw new IllegalArgumentException("maxLoss must be at least 1");
        }
        loveYourPlantsMaxLoss = maxLoss;
    }

    public boolean isLoveYourPlantsActive() {
        return loveYourPlantsMaxLoss != null;
    }

    public int getLoveYourPlantsMaxLoss() {
        if (loveYourPlantsMaxLoss == null) {
            throw new IllegalStateException("love your plants mode is not active");
        }
        return loveYourPlantsMaxLoss;
    }

    public int getLoveYourPlantsRemaining() {
        if (loveYourPlantsMaxLoss == null) {
            throw new IllegalStateException("love your plants mode is not active");
        }
        return Math.max(0, loveYourPlantsMaxLoss - plantsLost);
    }

    public void activatePlantWhatYouGet(int startingSun) {
        if (startingSun < 0) {
            throw new IllegalArgumentException("startingSun must not be negative");
        }
        setSunBalance(startingSun);
        skySunSystem.setEnabled(false);
        setWavesAutoStart(false);
        plantWhatYouGetActive = true;
        prepPhaseActive = true;
    }

    public boolean isPlantWhatYouGetActive() {
        return plantWhatYouGetActive;
    }

    public boolean isPrepPhaseActive() {
        return prepPhaseActive;
    }

    public void endPrepPhase() {
        prepPhaseActive = false;
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
    }

    public int getWaveStartTick() {
        return waveStartTick;
    }

    public void attachQuestTracker(model.quest.QuestTracker questTracker) {
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

    public boolean isRunning() {
        return running;
    }

    public void start() {
        running = true;
        matchResult = MatchResult.IN_PROGRESS;
        if (wavesAutoStart && waveManager != null && !waveManager.areWavesStarted()) {
            waveManager.startWaves(this);
        }
        eventBus.publish(new GameEvent.GameStarted(levelId, chapterId, nightLevel));
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
        boolean endingPrep = prepPhaseActive && plantWhatYouGetActive;
        if (endingPrep) {
            endPrepPhase();
        }
        if (waveManager != null) {
            waveManager.startWaves(this);
        }
        if (endingPrep && matchListener != null) {
            matchListener.onPlantWhatYouGetWavesStarted();
        }
    }

    public PlantPlacementResult tryPlant(String plantName, int col, int row, int level) {
        PlantDefinition definition = plantRegistry.getDefinition(plantName);
        if (definition == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (level < 1 || level > definition.getMaxLevel()) {
            return PlantPlacementResult.INVALID_LEVEL;
        }
        if (conveyorBeltActive) {
            if (!conveyorBeltPlants.contains(plantName)) {
                return PlantPlacementResult.NOT_ON_CONVEYOR_BELT;
            }
        } else if (!levelLockedPlants.isEmpty() && levelLockedPlants.contains(plantName)) {
            return PlantPlacementResult.LEVEL_PLANT_LOCKED;
        } else if (!selectedLoadout.isEmpty() && !selectedLoadout.contains(plantName)) {
            return PlantPlacementResult.NOT_IN_LOADOUT;
        }
        PlantStatsAtLevel stats = new PlantStatsAtLevel(definition, level);
        if (!prepPhaseActive && !cooldownTracker.isReady(plantName)) {
            return PlantPlacementResult.ON_COOLDOWN;
        }
        if (sunBalance < stats.cost()) {
            return PlantPlacementResult.INSUFFICIENT_SUN;
        }
        PlantPlacementResult placement = board.canPlace(definition, col, row);
        if (placement != PlantPlacementResult.SUCCESS) {
            return placement;
        }
        Plant plant = plantFactory.create(definition, level, col, row);
        sunBalance -= stats.cost();
        board.placePlant(plant);
        plant.onPlanted(context);
        if (!prepPhaseActive) {
            cooldownTracker.startCooldown(plantName, plant.getStats().recharge(), TICKS_PER_SECOND);
        }
        eventBus.publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getCategory().name(),
                col,
                row,
                plant.hasTag(PlantTag.NIGHT) || plant.hasTag(PlantTag.SHROOM)));
        eventBus.publish(new GameEvent.SunSpent(stats.cost()));
        if (conveyorBeltActive) {
            conveyorBeltPlants.remove(plantName);
        }
        return PlantPlacementResult.SUCCESS;
    }

    public boolean collectSun(Sun sun) {
        if (sun == null || !sunItems.contains(sun)) {
            return false;
        }
        if (sun.getType() == model.item.SunType.RADIOACTIVE && sun.isFalling()) {
            sunItems.remove(sun);
            explodeRadioactiveSun(sun.getCol(), sun.getRow());
            return true;
        }
        if (!sunItems.remove(sun)) {
            return false;
        }
        sunBalance += sun.getValue();
        eventBus.publish(new GameEvent.SunCollected(sun.getValue()));
        return true;
    }

    public boolean collectSunAt(int col, int row) {
        Sun target = null;
        for (Sun sun : sunItems) {
            if (sun.getCol() == col && sun.getRow() == row) {
                target = sun;
                break;
            }
        }
        return collectSun(target);
    }

    private void explodeRadioactiveSun(int col, int row) {
        if (matchListener != null) {
            matchListener.onRadioactiveSunExploded(col, row);
        }
        for (Zombie zombie : getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zCol - col) <= 2 && Math.abs(zombie.getRow() - row) <= 2) {
                zombie.takeDirectDamage(150);
                if (zombie.isDead()) {
                    handleZombieKilled(zombie);
                }
            }
        }
        for (Plant plant : board.getAllPlants()) {
            if (!plant.isAlive()) {
                continue;
            }
            if (Math.abs(plant.getCol() - col) <= 1 && Math.abs(plant.getRow() - row) <= 1) {
                plant.takeDamage(80);
            }
        }
        cleanupDeadZombies();
        cleanupDeadPlants();
    }

    public boolean usePlantFood(int col, int row) {
        if (plantFoodCount <= 0) {
            return false;
        }
        Plant plant = board.getPlantAt(col, row);
        if (plant == null || !plant.isAlive()) {
            return false;
        }
        plantFoodCount--;
        plant.activatePlantFoodEffect(context);
        return true;
    }

    public void addZombie(Zombie zombie) {
        if (zombie == null) {
            return;
        }
        zombie.bindContext(context);
        if (zombie.isDead()) {
            handleZombieKilled(zombie);
        } else if (tickingZombies) {
            pendingZombies.add(zombie);
        } else {
            zombies.add(zombie);
        }
    }

    public Zombie spawnZombieOfType(String alias, int row, double x) {
        if (zombieFactory == null) {
            throw new IllegalStateException("This session has no ZombieFactory");
        }
        if (row < 0 || row >= board.getRows() || !Double.isFinite(x)
                || x < 0 || x > board.getCols()) {
            throw new IllegalArgumentException("Zombie spawn position is outside the board");
        }
        Zombie zombie = zombieFactory.createZombie(alias, x, row, zombieDifficulty);
        addZombie(zombie);
        return zombie;
    }

    public ZombieFactory getZombieFactory() {
        return zombieFactory;
    }

    public int getZombieDifficulty() {
        return zombieDifficulty;
    }

    public void spawnSkySun(int col, int row, int value) {
        if (board.inBounds(col, row) && value > 0) {
            sunItems.add(new Sun(col, row, value, model.item.SunType.NORMAL, false));
        }
    }

    public void tick() {
        if (!running || matchResult != MatchResult.IN_PROGRESS) {
            return;
        }
        currentTick++;
        cooldownTracker.tick();
        familyBoostEndTicks.entrySet().removeIf(entry -> entry.getValue() <= currentTick);
        rowModifiers.entrySet().removeIf(entry -> entry.getValue().endTick() <= currentTick);
        rowEffects.values().forEach(effects ->
                effects.entrySet().removeIf(entry -> entry.getValue() <= currentTick));
        rowEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        plantCoverings.removeIf(covering -> {
            covering.onTickUpdate(context);
            return covering.isDead();
        });
        arcadeObstacles.removeIf(ArcadeObstacle::isDead);

        for (Plant plant : board.getAllPlants()) {
            if (plant.isAlive()) {
                plant.onTickUpdate(context);
            }
        }

        tickingZombies = true;
        try {
            Iterator<Zombie> zombieIterator = zombies.iterator();
            while (zombieIterator.hasNext()) {
                Zombie zombie = zombieIterator.next();
                if (zombie.isDead()) {
                    handleZombieKilled(zombie);
                    zombieIterator.remove();
                    continue;
                }
                zombie.tickStatuses();
                if (zombie.isDead()) {
                    handleZombieKilled(zombie);
                    zombieIterator.remove();
                    continue;
                }
                zombie.onTickUpdate(context);
                checkArmedTraps(zombie);
                if (zombie.isDead()) {
                    handleZombieKilled(zombie);
                    zombieIterator.remove();
                }
            }
        } finally {
            tickingZombies = false;
            zombies.addAll(pendingZombies);
            pendingZombies.clear();
        }

        projectileSystem.tick(board, zombies, this::handleZombieKilled, context);
        plantCoverings.removeIf(PlantCovering::isDead);
        arcadeObstacles.removeIf(ArcadeObstacle::isDead);
        cleanupDeadZombies();
        tickSunItems();
        tickSkySun();
        cleanupDeadPlants();
        if (waveManager != null) {
            waveManager.tick(this);
            waveManager.publishClearedWaves(this);
        }
        checkWinCondition();
        tickGroundSeedPackets();
        if (activeSpecialLevelHandler != null) {
            activeSpecialLevelHandler.onTick(this);
        }
        if (activeMiniGameHandler != null) {
            activeMiniGameHandler.onTick(this);
        }
    }

    private void tickGroundSeedPackets() {
        Iterator<GroundSeedPacket> iterator = groundSeedPackets.iterator();
        while (iterator.hasNext()) {
            GroundSeedPacket packet = iterator.next();
            if (packet.expiresAtTick() <= currentTick) {
                iterator.remove();
                if (matchListener != null) {
                    matchListener.onSeedPacketExpired(packet.plantName(), packet.col(), packet.row());
                }
            }
        }
    }

    private void tickSkySun() {
        Sun sun = skySunSystem.tick(currentTick, TICKS_PER_SECOND, board.getCols(), board.getRows());
        if (sun != null) {
            sunItems.add(sun);
            if (matchListener != null) {
                matchListener.onSunDropped(sun.getType(), sun.getCol(), sun.getRow());
            }
        }
    }

    private void checkArmedTraps(Zombie zombie) {
        if (zombie.isTrapImmune()) {
            return;
        }
        int col = (int) Math.floor(zombie.getX());
        int row = zombie.getRow();
        Plant plant = board.getGroundPlantAt(col, row);
        if (plant == null || !plant.isAlive() || !plant.hasTag(PlantTag.TRAP) || !plant.isArmedTrap()) {
            return;
        }
        if (plant.getAbility() instanceof ExplosiveAbility explosive) {
            explosive.detonate(plant, context);
        } else {
            context.explode(plant, plant.getStats().damage(), 1.0);
            plant.consumeInstantly();
        }
    }

    private void tickSunItems() {
        Iterator<Sun> iterator = sunItems.iterator();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            boolean justLanded = sun.tick();
            if (justLanded && matchListener != null) {
                matchListener.onSunReachedGround(sun.getCol(), sun.getRow());
            }
            if (sun.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void cleanupDeadPlants() {
        for (Plant plant : board.getAllPlants()) {
            if (plant.isDead()) {
                if (matchListener != null) {
                    matchListener.onPlantDestroyed(plant, plant.getCol(), plant.getRow());
                }
                board.removePlant(plant);
            }
        }
    }

    private void cleanupDeadZombies() {
        zombies.removeIf(zombie -> {
            if (!zombie.isDead()) {
                return false;
            }
            handleZombieKilled(zombie);
            return true;
        });
    }

    public void spawnSunItem(Sun sun) {
        if (sun == null) {
            return;
        }
        sunItems.add(sun);
        if (timedWarActive && timedWarRules != null && timedWarRules.getMode() == TimedWarMode.SUN) {
            timedWarProgress += Math.max(0, sun.getValue());
        }
    }

    public Plant createClone(Plant source, int col, int row) {
        Plant clone = plantFactory.create(
                source.getDefinition(), source.getLevel(), col, row);
        board.placePlant(clone);
        clone.onPlanted(context);
        return clone;
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
        int remaining = Math.max(0, maximum);
        int stolen = 0;
        Iterator<Sun> iterator = sunItems.iterator();
        while (iterator.hasNext() && remaining > 0) {
            Sun sun = iterator.next();
            int value = sun.takeValue(remaining);
            stolen += value;
            remaining -= value;
            if (sun.getValue() == 0) {
                iterator.remove();
            }
        }
        return stolen;
    }

    public boolean removePlantFromBoard(Plant plant) {
        return removePlantFromBoard(plant, true);
    }

    public boolean removePlantFromBoard(Plant plant, boolean countsAsLoss) {
        if (plant == null || destroyedPlantIds.contains(plant.getId())) {
            return false;
        }
        boolean wasProtectedSeed = protectedSeedPlantIds.contains(plant.getId());
        destroyedPlantIds.add(plant.getId());
        board.removePlant(plant);
        if (countsAsLoss) {
            plantsLost++;
            if (activeSpecialLevelHandler != null) {
                activeSpecialLevelHandler.onPlantLost(this, plant);
            }
            if (matchListener != null) {
                matchListener.onPlantDestroyed(plant, plant.getCol(), plant.getRow());
            }
        }
        eventBus.publish(new GameEvent.PlantDestroyed(
                plant.getName(),
                plant.getCategory().name()));
        if (countsAsLoss && wasProtectedSeed) {
            if (matchListener != null) {
                matchListener.onProtectedSeedDestroyed(plant, plant.getCol(), plant.getRow());
            }
            loseMatch();
        }
        return true;
    }

    public void handleZombieKilled(Zombie zombie) {
        handleZombieKilled(zombie, null);
    }

    public void handleZombieKilled(Zombie zombie, String killerPlantType) {
        if (zombie == null || !zombie.isDead()) {
            return;
        }
        zombie.runDeathBehaviors(context);
        if (!killedZombieIds.add(zombie.getId())) {
            return;
        }
        if (timedWarActive && timedWarRules != null && timedWarRules.getMode() == TimedWarMode.KILL) {
            timedWarProgress++;
        }
        if (matchListener != null) {
            matchListener.onZombieDied(zombie.getType(), zombie.getX(), zombie.getRow());
        }
        if (zombie.isGlowing() && plantFoodCount < 3) {
            plantFoodCount++;
            if (matchListener != null) {
                matchListener.onGlowingZombieDroppedFood(plantFoodCount);
            }
        }
        rollZombieLootDrop();
        double secondsSinceWave = Math.max(0, (currentTick - waveStartTick) / (double) TICKS_PER_SECOND);
        eventBus.publish(new GameEvent.ZombieKilled(
                zombie.getType(),
                killerPlantType,
                chapterId,
                (int) zombie.getX(),
                zombie.getRow(),
                secondsSinceWave));
    }

    private void rollZombieLootDrop() {
        if (random.nextInt(100) >= 10) {
            return;
        }
        int roll = random.nextInt(3);
        if (matchListener == null) {
            return;
        }
        if (roll == 0) {
            matchListener.onItemDropped("coin", 50);
        } else if (roll == 1) {
            matchListener.onItemDropped("diamond", 1);
        } else {
            matchListener.onItemDropped("pot", 1);
        }
    }

    public void handleZombieReachedHouse(Zombie zombie) {
        if (zombie == null || matchResult != MatchResult.IN_PROGRESS) {
            return;
        }
        int row = zombie.getRow();
        if (row < 0 || row >= lawnMowers.size()) {
            loseMatch();
            return;
        }
        LawnMower mower = lawnMowers.get(row);
        if (mower.trigger()) {
            List<Zombie> killed = new ArrayList<>();
            for (Zombie candidate : List.copyOf(zombies)) {
                if (!candidate.isAlive() || candidate.getRow() != row) {
                    continue;
                }
                if (isBossZombie(candidate)) {
                    continue;
                }
                candidate.takeDirectDamage(candidate.getHealth() + 99999);
                handleZombieKilled(candidate);
                killed.add(candidate);
            }
            if (!tickingZombies) {
                zombies.removeIf(Zombie::isDead);
            }
            if (matchListener != null) {
                matchListener.onLawnMowerTriggered(row + 1, killed);
            }
            eventBus.publish(new GameEvent.LawnMowerTriggered(row, killed.size()));
        } else {
            if (matchListener != null) {
                matchListener.onLawnMowerFailed(row + 1);
            }
            loseMatch();
        }
    }

    private static boolean isBossZombie(Zombie zombie) {
        String type = zombie.getType();
        return type != null && (type.contains("Gargantuar") || type.contains("King"));
    }

    private void checkWinCondition() {
        if (matchResult != MatchResult.IN_PROGRESS || waveManager == null) {
            return;
        }
        if (timedWarActive) {
            return;
        }
        if (waveManager.areAllWavesCleared() && getLivingZombieCount() == 0) {
            winMatch();
        }
    }

    private int getLivingZombieCount() {
        int count = 0;
        for (Zombie zombie : zombies) {
            if (zombie.isAlive()) {
                count++;
            }
        }
        return count;
    }

    public void winMatch() {
        if (matchResult != MatchResult.IN_PROGRESS) {
            return;
        }
        matchResult = MatchResult.WON;
        running = false;
        if (attachedQuestTracker != null) {
            attachedQuestTracker.prepareBoardSnapshots(this);
        }
        if (matchListener != null) {
            matchListener.onWin();
        }
        eventBus.publish(new GameEvent.GameFinished(true, sunBalance, plantsLost,
                currentTick / (long) TICKS_PER_SECOND));
    }

    public void loseMatch() {
        if (matchResult != MatchResult.IN_PROGRESS) {
            return;
        }
        matchResult = MatchResult.LOST;
        running = false;
        if (attachedQuestTracker != null) {
            attachedQuestTracker.prepareBoardSnapshots(this);
        }
        if (matchListener != null) {
            matchListener.onLose();
        }
        eventBus.publish(new GameEvent.GameFinished(false, sunBalance, plantsLost,
                currentTick / (long) TICKS_PER_SECOND));
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
        for (Zombie zombie : getZombies()) {
            if (zombie.isAlive()) {
                zombie.takeDirectDamage(zombie.getHealth() + 99999);
                handleZombieKilled(zombie);
            }
        }
        zombies.removeIf(Zombie::isDead);
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
        skySunSystem.setDifficultyScale(WaveManager.skySunIntervalScale(dl));
        if (waveManager != null) {
            waveManager.setWaveCostDifficultyScale(WaveManager.waveCostScale(dl));
        }
    }

    public PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        if (plant == null || !plant.isAlive() || type == null) {
            return null;
        }
        for (PlantCovering covering : plantCoverings) {
            if (covering.isAlive() && covering.getCoveredPlant() == plant
                    && covering.getType() == type) {
                return covering;
            }
        }
        PlantCovering covering = new PlantCovering(type, plant, Math.max(1, health));
        plantCoverings.add(covering);
        return covering;
    }

    public void registerHunterIceHit(Plant plant) {
        addPlantFrostStack(plant);
    }

    public void addPlantFrostStack(Plant plant) {
        if (plant == null || !plant.isAlive()) {
            return;
        }
        int hits = plant.addHostileIceStack("frost");
        if (hits >= 3) {
            coverPlant(plant, PlantCovering.Type.HUNTER_ICE, 600);
            plant.clearHostileIce();
        }
    }

    public void clearGraveAt(int col, int row) {
        if (!board.inBounds(col, row) || !board.getTile(col, row).isGrave()) {
            return;
        }
        var tile = board.getTile(col, row);
        GraveTile.Loot loot = GraveTile.Loot.NONE;
        if (tile instanceof GraveTile grave) {
            loot = grave.getLoot();
        }
        board.setTile(col, row, new model.game.board.tile.NormalTile());
        if (loot == GraveTile.Loot.SUN_50) {
            addSunBalance(50);
        } else if (loot == GraveTile.Loot.PLANT_FOOD) {
            addPlantFood(1);
        }
    }

    public void pushArcadeObstacle(Zombie pusher) {
        if (pusher == null || pusher.isDead()) {
            return;
        }
        ArcadeObstacle obstacle = arcadeObstacles.stream()
                .filter(candidate -> pusher.getId().equals(candidate.getPusherId()))
                .findFirst()
                .orElseGet(() -> {
                    ArcadeObstacle created = new ArcadeObstacle(pusher);
                    arcadeObstacles.add(created);
                    return created;
                });
        obstacle.follow(pusher);
        int col = (int) Math.floor(obstacle.getX());
        Plant plant = board.getPlantAt(col, obstacle.getRow());
        if (plant != null && plant.canBeTargetedByZombie()) {
            plant.takeDamage(plant.getHealth());
        }
        for (Zombie zombie : getZombies()) {
            if (zombie != pusher && zombie.isAlive() && zombie.isHypnotized()
                    && zombie.getRow() == obstacle.getRow()
                    && Math.abs(zombie.getX() - obstacle.getX()) <= 0.55) {
                zombie.takeDirectDamage(zombie.getHealth());
                handleZombieKilled(zombie);
            }
        }
    }

    public void releaseArcadeObstacle(String pusherId) {
        arcadeObstacles.forEach(obstacle -> obstacle.releasePusher(pusherId));
    }

    public void resetFamilyCooldowns(PlantCategory category) {
        cooldownTracker.resetCategory(plantRegistry, category.name());
    }

    public void boostFamily(PlantCategory category, double durationSeconds) {
        int endTick = currentTick + (int) Math.ceil(durationSeconds * TICKS_PER_SECOND);
        familyBoostEndTicks.merge(category, endTick, Math::max);
    }

    public boolean isFamilyBoosted(PlantCategory category) {
        return familyBoostEndTicks.getOrDefault(category, 0) > currentTick;
    }

    public void applyFieldModifier(int row, double magnitude, double durationSeconds) {
        int endTick = currentTick + (int) Math.ceil(durationSeconds * TICKS_PER_SECOND);
        rowModifiers.put(row, new FieldModifier(magnitude, endTick));
    }

    public double getFieldModifier(int row) {
        FieldModifier modifier = rowModifiers.get(row);
        return modifier == null || modifier.endTick() <= currentTick ? 0.0 : modifier.magnitude();
    }

    public void applyRowEffect(int row, String effectType, int durationTicks) {
        if (row < 0 || row >= board.getRows() || effectType == null
                || effectType.isBlank() || durationTicks <= 0) {
            return;
        }
        int endTick = currentTick + durationTicks;
        rowEffects.computeIfAbsent(row, ignored -> new HashMap<>())
                .merge(effectType, endTick, Math::max);
    }

    public boolean isRowEffectActive(int row, String effectType) {
        return rowEffects.getOrDefault(row, Map.of())
                .getOrDefault(effectType, 0) > currentTick;
    }

    private record PlantStatsAtLevel(PlantDefinition definition, int level) {
        int cost() {
            return model.game.entity.plant.PlantStatsCalculator.compute(definition, level).cost();
        }
    }

    private record FieldModifier(double magnitude, int endTick) {
    }
}
