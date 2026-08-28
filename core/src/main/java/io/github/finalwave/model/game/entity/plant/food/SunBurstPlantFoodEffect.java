package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.item.SunType;

public final class SunBurstPlantFoodEffect implements PlantFoodEffect {

    private static final int[] COL_OFFSETS = {0, 1, -1, 0, 0, 1, -1, 1, -1};
    private static final int[] ROW_OFFSETS = {0, 0, 0, 1, -1, 1, -1, -1, 1};

    private final int sunAmount;
    private int tickTimer;
    private int setupTicks;
    private boolean spawned;

    public SunBurstPlantFoodEffect(double sunAmount) {
        this.sunAmount = Math.max(0, (int) Math.round(sunAmount));
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        spawned = false;
        int[] timings = timings(plant);
        setupTicks = timings[0];
        plant.beginPlantFood(timings[1], setupTicks);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (spawned || tickTimer <= setupTicks) {
            return;
        }
        if ("Sun-shroom".equals(plant.getName())) {
            plant.growToMaxStage(context.getTicksPerSecond());
        }
        spawnBurst(plant, context);
        spawned = true;
    }

    @Override
    public void end(Plant plant, GameContext context) {
        spawned = false;
        tickTimer = 0;
    }

    private void spawnBurst(Plant plant, GameContext context) {
        SunDrop[] drops = dropsFor(plant);
        for (int index = 0; index < drops.length; index++) {
            SunDrop drop = drops[index];
            int col = plant.getCol() + COL_OFFSETS[index % COL_OFFSETS.length];
            int row = plant.getRow() + ROW_OFFSETS[index % ROW_OFFSETS.length];
            context.spawnSunAt(col, row, drop.value(), drop.type());
        }
        plant.beginSunProduce(context.getTicksPerSecond());
    }

    private SunDrop[] dropsFor(Plant plant) {
        return switch (plant.getName()) {
            case "Sunflower" -> new SunDrop[]{
                    new SunDrop(50, SunType.NORMAL),
                    new SunDrop(50, SunType.NORMAL),
                    new SunDrop(50, SunType.NORMAL)
            };
            case "Twin Sunflower" -> new SunDrop[]{
                    new SunDrop(100, SunType.SPECIAL),
                    new SunDrop(100, SunType.SPECIAL),
                    new SunDrop(50, SunType.NORMAL)
            };
            case "Sun-shroom", "Primal Sunflower" -> new SunDrop[]{
                    new SunDrop(75, SunType.SPECIAL),
                    new SunDrop(75, SunType.SPECIAL),
                    new SunDrop(75, SunType.SPECIAL)
            };
            default -> genericDrops(sunAmount);
        };
    }

    private static SunDrop[] genericDrops(int total) {
        int remaining = total;
        int capacity = Math.max(1, (total + 49) / 50);
        SunDrop[] drops = new SunDrop[capacity];
        int index = 0;
        while (remaining > 0) {
            int chunk = Math.min(50, remaining);
            drops[index++] = new SunDrop(chunk, SunType.NORMAL);
            remaining -= chunk;
        }
        if (index == drops.length) {
            return drops;
        }
        SunDrop[] trimmed = new SunDrop[index];
        System.arraycopy(drops, 0, trimmed, 0, index);
        return trimmed;
    }

    private static int[] timings(Plant plant) {
        return switch (plant.getName()) {
            case "Sunflower" -> new int[]{5, 15};
            case "Twin Sunflower" -> new int[]{5, 17};
            case "Primal Sunflower" -> new int[]{6, 20};
            case "Sun-shroom" -> new int[]{0, 25};
            default -> new int[]{0, 15};
        };
    }

    private record SunDrop(int value, SunType type) {
    }
}
