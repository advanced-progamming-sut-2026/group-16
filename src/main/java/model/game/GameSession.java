package model.game;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.definition.plant.PlantDefinition;
import model.game.board.BoardGameContext;
import model.game.board.GameBoard;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.*;
import model.game.entity.plant.ability.ExplosiveAbility;
import model.game.entity.projectile.ProjectileSystem;
import model.game.entity.zombie.ArcadeObstacle;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieFactory;
import model.item.Sun;
import model.quest.event.GameEvent;
import model.quest.event.GameEventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
    private final Set<String> destroyedPlantIds = new HashSet<>();
    private final Set<String> killedZombieIds = new HashSet<>();
    private final Map<PlantCategory, Integer> familyBoostEndTicks = new HashMap<>();
    private final Map<Integer, FieldModifier> rowModifiers = new HashMap<>();
    private final Map<Integer, Map<String, Integer>> rowEffects = new HashMap<>();
    private final ZombieFactory zombieFactory;
    private final int zombieDifficulty;
    private final Random random;
    private Set<String> selectedLoadout = Set.of();

    private int currentTick;
    private int sunBalance;
    private int plantFoodCount;
    private String chapterId = "default";
    private boolean running;
    private boolean tickingZombies;

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

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public PlantPlacementResult tryPlant(String plantName, int col, int row, int level) {
        PlantDefinition definition = plantRegistry.getDefinition(plantName);
        if (definition == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (level < 1 || level > definition.getMaxLevel()) {
            return PlantPlacementResult.INVALID_LEVEL;
        }
        if (!selectedLoadout.isEmpty() && !selectedLoadout.contains(plantName)) {
            return PlantPlacementResult.NOT_IN_LOADOUT;
        }
        PlantStatsAtLevel stats = new PlantStatsAtLevel(definition, level);
        if (!cooldownTracker.isReady(plantName)) {
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
        cooldownTracker.startCooldown(plantName, plant.getStats().recharge(), TICKS_PER_SECOND);
        eventBus.publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getCategory().name(),
                col,
                row,
                plant.hasTag(PlantTag.NIGHT) || plant.hasTag(PlantTag.SHROOM)));
        eventBus.publish(new GameEvent.SunSpent(stats.cost()));
        return PlantPlacementResult.SUCCESS;
    }

    public boolean collectSun(Sun sun) {
        if (sun == null || !sunItems.remove(sun)) {
            return false;
        }
        sunBalance += sun.getValue();
        eventBus.publish(new GameEvent.SunCollected(sun.getValue()));
        return true;
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
        if (!running) {
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
        cleanupDeadPlants();
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
            sun.tick();
            if (sun.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void cleanupDeadPlants() {
        for (Plant plant : board.getAllPlants()) {
            if (plant.isDead()) {
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
        sunItems.add(sun);
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
        if (plant == null || destroyedPlantIds.contains(plant.getId())) {
            return false;
        }
        destroyedPlantIds.add(plant.getId());
        board.removePlant(plant);
        eventBus.publish(new GameEvent.PlantDestroyed(
                plant.getName(),
                plant.getCategory().name()));
        return true;
    }

    public void handleZombieKilled(Zombie zombie) {
        if (zombie == null || !zombie.isDead()) {
            return;
        }
        zombie.runDeathBehaviors(context);
        if (!killedZombieIds.add(zombie.getId())) {
            return;
        }
        eventBus.publish(new GameEvent.ZombieKilled(
                zombie.getType(),
                null,
                chapterId,
                (int) zombie.getX(),
                zombie.getRow(),
                currentTick / (double) TICKS_PER_SECOND));
    }

    public void handleZombieReachedHouse(Zombie zombie) {
        running = false;
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
        if (plant == null || !plant.isAlive()) {
            return;
        }
        int hits = plant.addHostileIceStack("hunter");
        if (hits >= 3) {
            coverPlant(plant, PlantCovering.Type.HUNTER_ICE, 600);
            plant.clearHostileIce();
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
