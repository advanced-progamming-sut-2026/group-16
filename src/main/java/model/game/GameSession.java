package model.game;

import model.definition.PlantRegistry;
import model.definition.plant.PlantDefinition;
import model.game.board.BoardGameContext;
import model.game.board.GameBoard;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.*;
import model.game.entity.plant.ability.ExplosiveAbility;
import model.game.entity.projectile.ProjectileSystem;
import model.game.entity.zombie.Zombie;
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
    private final List<Sun> sunItems = new ArrayList<>();
    private final Set<String> destroyedPlantIds = new HashSet<>();
    private final Set<String> killedZombieIds = new HashSet<>();
    private final Map<PlantCategory, Integer> familyBoostEndTicks = new HashMap<>();
    private final Map<Integer, FieldModifier> rowModifiers = new HashMap<>();
    private Set<String> selectedLoadout = Set.of();

    private int currentTick;
    private int sunBalance;
    private int plantFoodCount;
    private String chapterId = "default";
    private boolean running;

    public GameSession(PlantRegistry plantRegistry) {
        this(plantRegistry, new GameBoard(), 50);
    }

    public GameSession(PlantRegistry plantRegistry, int startingSun) {
        this(plantRegistry, new GameBoard(), startingSun);
    }

    public GameSession(PlantRegistry plantRegistry, GameBoard board, int startingSun) {
        this.plantRegistry = plantRegistry;
        this.board = board;
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
        return List.copyOf(zombies);
    }

    public List<Sun> getSunItems() {
        return List.copyOf(sunItems);
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
        if (zombie != null && zombie.isAlive()) {
            zombies.add(zombie);
        }
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

        for (Plant plant : board.getAllPlants()) {
            if (plant.isAlive()) {
                plant.onTickUpdate(context);
            }
        }

        Iterator<Zombie> zombieIterator = zombies.iterator();
        while (zombieIterator.hasNext()) {
            Zombie zombie = zombieIterator.next();
            if (zombie.isDead()) {
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
                zombieIterator.remove();
            }
        }

        projectileSystem.tick(board, zombies, this::handleZombieKilled);
        tickSunItems();
        cleanupDeadPlants();
    }

    private void checkArmedTraps(Zombie zombie) {
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

    void addSunBalance(int amount) {
        sunBalance += amount;
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
        if (zombie == null || !killedZombieIds.add(zombie.getId())) {
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

    private record PlantStatsAtLevel(PlantDefinition definition, int level) {
        int cost() {
            return model.game.entity.plant.PlantStatsCalculator.compute(definition, level).cost();
        }
    }

    private record FieldModifier(double magnitude, int endTick) {
    }
}
