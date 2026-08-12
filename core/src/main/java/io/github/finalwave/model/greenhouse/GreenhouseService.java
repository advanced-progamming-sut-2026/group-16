package io.github.finalwave.model.greenhouse;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GreenhouseService {
    static final long MARIGOLD_GROWTH_MILLIS = TimeUnit.HOURS.toMillis(2);
    static final long PLANT_GROWTH_MILLIS = TimeUnit.HOURS.toMillis(8);

    private final PlantRegistry plantRegistry;
    private final Random random;

    public GreenhouseService(PlantRegistry plantRegistry) {
        this(plantRegistry, new Random());
    }

    public GreenhouseService(PlantRegistry plantRegistry, Random random) {
        this.plantRegistry = plantRegistry;
        this.random = random;
    }

    public String formatDisplay(User user) {
        StringBuilder builder = new StringBuilder("Greenhouse:\n");
        for (int y = 1; y <= GreenhouseLayout.ROWS; y++) {
            for (int x = 1; x <= GreenhouseLayout.COLUMNS; x++) {
                GreenhousePot pot = user.getPotAt(x, y);
                if (pot == null) {
                    builder.append('[').append(x).append(',').append(y).append(": missing] ");
                    continue;
                }
                builder.append('[').append(x).append(',').append(y).append(": ")
                        .append(formatPotState(pot)).append("] ");
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    public List<GreenhouseSlotState> slotStates(User user) {
        List<GreenhouseSlotState> slots = new ArrayList<>();
        for (int y = 1; y <= GreenhouseLayout.ROWS; y++) {
            for (int x = 1; x <= GreenhouseLayout.COLUMNS; x++) {
                slots.add(describe(user.getPotAt(x, y), x, y));
            }
        }
        return slots;
    }

    public int plantableCount(User user) {
        int count = 0;
        for (GreenhouseSlotState slot : slotStates(user)) {
            if (!slot.locked() && slot.empty()) {
                count++;
            }
        }
        return count;
    }

    public UnlockResult unlockNextLockedFree(User user) {
        for (int y = 1; y <= GreenhouseLayout.ROWS; y++) {
            for (int x = 1; x <= GreenhouseLayout.COLUMNS; x++) {
                GreenhousePot pot = user.getPotAt(x, y);
                if (pot != null && pot.isLocked()) {
                    pot.setLocked(false);
                    return UnlockResult.success(x, y);
                }
            }
        }
        return UnlockResult.alreadyUnlocked();
    }

    public UnlockResult unlock(User user, int x, int y) {
        GreenhousePot pot = validatePlantablePot(user, x, y);
        if (pot == null) {
            return UnlockResult.invalidLocation();
        }
        if (!pot.isLocked()) {
            return UnlockResult.alreadyUnlocked();
        }
        if (!user.spendCoins(GreenhouseLayout.POT_UNLOCK_COST_COINS)) {
            return UnlockResult.notEnoughCoins();
        }
        pot.setLocked(false);
        return UnlockResult.success();
    }

    public PlantingResult plant(User user, int x, int y) {
        GreenhousePot pot = validatePlantablePot(user, x, y);
        if (pot == null) {
            return PlantingResult.invalidLocation();
        }
        if (pot.isLocked()) {
            return PlantingResult.locked();
        }
        if (!pot.isEmpty()) {
            return PlantingResult.occupied();
        }

        String plantType = choosePlantType(user);
        boolean isMarigold = GreenhousePot.MARIGOLD.equals(plantType);
        pot.plant(plantType, isMarigold, System.currentTimeMillis());
        return PlantingResult.success(plantType);
    }

    public CollectResult collect(User user, int x, int y) {
        GreenhousePot pot = validatePlantablePot(user, x, y);
        if (pot == null) {
            return CollectResult.invalidLocation();
        }
        if (pot.isEmpty()) {
            return CollectResult.noPlant();
        }
        if (!isReady(pot, System.currentTimeMillis())) {
            return CollectResult.notReady();
        }

        if (pot.isMarigold()) {
            user.addCoins(500);
            pot.clear();
            return CollectResult.success("500 coin(s)");
        }

        String plantType = pot.getPlantType();
        String reward = "boost stored for " + plantType;
        if (user.hasStoredBoost(plantType)) {
            reward = "pot cleared (boost for " + plantType + " was already stored)";
        } else {
            user.getStoredBoosts().add(plantType);
        }
        pot.clear();
        return CollectResult.success(reward);
    }

    public GrowResult grow(User user, int x, int y) {
        GreenhousePot pot = validatePlantablePot(user, x, y);
        if (pot == null) {
            return GrowResult.invalidLocation();
        }
        if (pot.isEmpty()) {
            return GrowResult.noPlant();
        }
        if (isReady(pot, System.currentTimeMillis())) {
            return GrowResult.alreadyReady();
        }

        int diamondsNeeded = getAccelerationCost(pot, System.currentTimeMillis());
        if (!user.spendDiamonds(diamondsNeeded)) {
            return GrowResult.notEnoughDiamonds();
        }
        long completedAt = System.currentTimeMillis() - getGrowthDurationMillis(pot);
        pot.plant(pot.getPlantType(), pot.isMarigold(), completedAt);
        return GrowResult.success(diamondsNeeded);
    }

    int getAccelerationCost(GreenhousePot pot, long nowMillis) {
        long readyAt = pot.getPlantedAtMillis() + getGrowthDurationMillis(pot);
        long remaining = Math.max(0L, readyAt - nowMillis);
        double remainingHours = remaining / (double) TimeUnit.HOURS.toMillis(1);
        return (int) Math.ceil(remainingHours);
    }

    boolean isReady(GreenhousePot pot, long nowMillis) {
        return !pot.isEmpty() && nowMillis >= pot.getPlantedAtMillis() + getGrowthDurationMillis(pot);
    }

    long getGrowthDurationMillis(GreenhousePot pot) {
        return pot.isMarigold() ? MARIGOLD_GROWTH_MILLIS : PLANT_GROWTH_MILLIS;
    }

    private GreenhousePot validatePlantablePot(User user, int x, int y) {
        if (!GreenhouseLayout.isValid(x, y)) {
            return null;
        }
        return user.getPotAt(x, y);
    }

    private GreenhouseSlotState describe(GreenhousePot pot, int x, int y) {
        if (pot == null) {
            return new GreenhouseSlotState(x, y, true, true, null, 0L, 0L);
        }
        if (pot.isLocked() || pot.isEmpty()) {
            return new GreenhouseSlotState(x, y, pot.isLocked(), pot.isEmpty(), null, 0L, 0L);
        }
        return new GreenhouseSlotState(
                x,
                y,
                false,
                false,
                pot.getPlantType(),
                pot.getPlantedAtMillis(),
                getGrowthDurationMillis(pot));
    }

    private String choosePlantType(User user) {
        List<String> candidates = new ArrayList<>();
        for (String plantName : user.getPlantProgress().getUnlockedPlantNames()) {
            PlantDefinition definition = plantRegistry.getDefinition(plantName);
            if (definition != null && definition.hasPlantFoodEffect()) {
                candidates.add(plantName);
            }
        }
        if (candidates.isEmpty() || random.nextBoolean()) {
            return GreenhousePot.MARIGOLD;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private String formatPotState(GreenhousePot pot) {
        if (pot.isLocked()) {
            return "locked";
        }
        if (pot.isEmpty()) {
            return "empty";
        }
        if (isReady(pot, System.currentTimeMillis())) {
            return pot.getPlantType() + " ready";
        }
        long readyAt = pot.getPlantedAtMillis() + getGrowthDurationMillis(pot);
        long remainingMinutes = Math.max(0L, TimeUnit.MILLISECONDS.toMinutes(readyAt - System.currentTimeMillis()));
        long hours = remainingMinutes / 60;
        long minutes = remainingMinutes % 60;
        return pot.getPlantType() + " " + hours + "h " + minutes + "m";
    }

    public record PlantingResult(String status, String plantType) {
        static PlantingResult success(String plantType) {
            return new PlantingResult("success", plantType);
        }

        static PlantingResult invalidLocation() {
            return new PlantingResult("invalid_location", null);
        }

        static PlantingResult locked() {
            return new PlantingResult("locked", null);
        }

        static PlantingResult occupied() {
            return new PlantingResult("occupied", null);
        }
    }

    public record CollectResult(String status, String reward) {
        static CollectResult success(String reward) {
            return new CollectResult("success", reward);
        }

        static CollectResult invalidLocation() {
            return new CollectResult("invalid_location", null);
        }

        static CollectResult noPlant() {
            return new CollectResult("no_plant", null);
        }

        static CollectResult notReady() {
            return new CollectResult("not_ready", null);
        }
    }

    public record GrowResult(String status, int diamondsSpent) {
        static GrowResult success(int diamondsSpent) {
            return new GrowResult("success", diamondsSpent);
        }

        static GrowResult invalidLocation() {
            return new GrowResult("invalid_location", 0);
        }

        static GrowResult noPlant() {
            return new GrowResult("no_plant", 0);
        }

        static GrowResult alreadyReady() {
            return new GrowResult("already_ready", 0);
        }

        static GrowResult notEnoughDiamonds() {
            return new GrowResult("not_enough_diamonds", 0);
        }
    }

    public record UnlockResult(String status, int x, int y) {
        static UnlockResult success() {
            return success(0, 0);
        }

        static UnlockResult success(int x, int y) {
            return new UnlockResult("success", x, y);
        }

        static UnlockResult invalidLocation() {
            return new UnlockResult("invalid_location", 0, 0);
        }

        static UnlockResult alreadyUnlocked() {
            return new UnlockResult("already_unlocked", 0, 0);
        }

        static UnlockResult notEnoughCoins() {
            return new UnlockResult("not_enough_coins", 0, 0);
        }
    }
}
