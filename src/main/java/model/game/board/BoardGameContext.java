package model.game.board;

import model.game.GameSession;
import model.game.MatchListener;
import model.game.board.tile.NormalTile;
import model.game.board.tile.GraveTile;
import model.game.board.tile.IceTile;
import model.game.board.tile.Tile;
import model.game.entity.GameContext;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCategory;
import model.game.entity.plant.PlantCovering;
import model.game.entity.plant.PlantSpecialModifiers;
import model.game.entity.plant.PlantTag;
import model.game.entity.plant.ability.ExplosiveAbility;
import model.game.entity.plant.ability.ProjectileAttackAbility;
import model.game.entity.projectile.Projectile;
import model.game.entity.projectile.ProjectileEffect;
import model.game.entity.projectile.ProjectileProfile;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ArcadeObstacle;
import model.item.Sun;
import model.item.SunType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class BoardGameContext implements GameContext {

    private final GameSession session;
    private final Random random;

    public BoardGameContext(GameSession session) {
        this.session = session;
        this.random = session.getRandom();
    }

    @Override
    public int getCurrentTick() {
        return session.getCurrentTick();
    }

    @Override
    public int getTicksPerSecond() {
        return GameSession.TICKS_PER_SECOND;
    }

    @Override
    public int getRowCount() {
        return session.getBoard().getRows();
    }

    @Override
    public int getColCount() {
        return session.getBoard().getCols();
    }

    @Override
    public Tile getTileAt(int col, int row) {
        if (!session.getBoard().inBounds(col, row)) {
            return null;
        }
        return session.getBoard().getTile(col, row);
    }

    @Override
    public boolean areZombiesImmuneToChill() {
        return session.areZombiesImmuneToChill();
    }

    @Override
    public Plant getPlantAt(int col, int row) {
        return session.getBoard().getPlantAt(col, row);
    }

    @Override
    public Plant getPlantInFront(double zombieX, int row) {
        return session.getBoard().getPlantInFront(zombieX, row);
    }

    @Override
    public List<Zombie> getZombiesInRow(int row) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getRow() == row && zombie.isAlive()) {
                result.add(zombie);
            }
        }
        return result;
    }

    @Override
    public List<Zombie> getAllZombies() {
        return session.getZombies();
    }

    @Override
    public List<Plant> getAllPlants() {
        return session.getBoard().getAllPlants();
    }

    @Override
    public void spawnProjectile(int row, double startX, int damage, String projectileType) {
        session.getProjectileSystem().spawn(Projectile.fromZombie(row, startX, damage, projectileType));
    }

    @Override
    public void spawnProjectile(Zombie source, int row, double startX,
                                int damage, String projectileType) {
        if (source == null) {
            spawnProjectile(row, startX, damage, projectileType);
            return;
        }
        session.getProjectileSystem().spawn(Projectile.fromZombie(
                row, startX, damage, projectileType, source.getId()));
    }

    @Override
    public void reflectProjectile(Zombie reflector, Projectile projectile) {
        session.getProjectileSystem().spawnReflected(reflector, projectile);
    }

    @Override
    public void spawnZombieOfType(String alias, int row, double x) {
        session.spawnZombieOfType(alias, row, x);
    }

    @Override
    public void dropSeedPacket(String plantName, int col, int row) {
        session.addGroundSeedPacket(plantName, col, row);
    }

    @Override
    public void onZombieReachedHouse(Zombie zombie) {
        session.handleZombieReachedHouse(zombie);
    }

    @Override
    public void onZombieKilled(Zombie zombie) {
        session.handleZombieKilled(zombie);
    }

    @Override
    public void onPlantDestroyed(Plant plant) {
        if (plant == null) {
            return;
        }
        if (!session.removePlantFromBoard(plant)) {
            return;
        }
        handleDeathTags(plant);
        handleMoveZombieOnEat(plant);
    }

    @Override
    public void applyRowEffect(int row, String effectType, int durationTicks) {
        session.applyRowEffect(row, effectType, durationTicks);
    }

    @Override
    public void spawnProjectile(Plant plant, int damage, int shots, ProjectileProfile profile) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        ProjectileEffect effect = hasFirePlantAhead(plant)
                ? ProjectileEffect.FIRE
                : plant.projectileEffect();
        session.getProjectileSystem().spawnFromPlant(
                plant, resolvedDamage, shots, profile, effect);
    }

    @Override
    public void boostFamily(Plant plant, PlantCategory boostedFamily, double extendedDuration) {
        session.boostFamily(boostedFamily, extendedDuration);
    }

    @Override
    public void resetFamilyCooldowns(PlantCategory boostedFamily) {
        session.resetFamilyCooldowns(boostedFamily);
    }

    @Override
    public void spawnSun(Plant plant, double total) {
        double amount = total;
        if (session.isFamilyBoosted(plant.getCategory())) {
            amount *= 2.0;
        }
        if (plant.getStats().hasSpecialModifier(PlantSpecialModifiers.SUN_DROP_INCREMENT)) {
            amount += plant.getStats().specialModifier(PlantSpecialModifiers.SUN_DROP_INCREMENT);
        }
        int value = (int) Math.round(amount);
        if (plant.getStats().hasSpecialModifier(PlantSpecialModifiers.DOUBLE_SUN_CHANCE)) {
            double chance = plant.getStats().specialModifier(PlantSpecialModifiers.DOUBLE_SUN_CHANCE);
            if (random.nextDouble() < chance) {
                value *= 2;
            }
        }
        session.spawnSunItem(new Sun(plant.getCol(), plant.getRow(), value, SunType.NORMAL, true));
        MatchListener listener = session.getMatchListener();
        if (listener != null) {
            listener.onSunProduced(plant, plant.getCol(), plant.getRow());
        }
    }

    @Override
    public void armTrap(Plant plant) {
        plant.setArmedTrap(true);
    }

    public void explodeAt(int centerCol, int centerRow, int damage, double radius) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isDead()) {
                continue;
            }
            double horizontalDistance = zombie.getX() - centerCol;
            double verticalDistance = zombie.getRow() - centerRow;
            if (Math.hypot(horizontalDistance, verticalDistance) <= radius) {
                zombie.takeDamage(damage);
                if (zombie.isDead()) {
                    onZombieKilled(zombie);
                }
            }
        }
    }

    @Override
    public void explode(Plant plant, int damage, double radius) {
        radius += plant.getStats()
                .specialModifier(PlantSpecialModifiers.GRAPE_BOUNCE_EXT);
        int bonus = (int) plant.getStats().specialModifier(PlantSpecialModifiers.EXPLODE_DAMAGE_BUFF);
        int totalDamage = damage + bonus;
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isDead()) {
                continue;
            }
            double horizontalDistance = zombie.getX() - centerCol;
            double verticalDistance = zombie.getRow() - centerRow;
            if (Math.hypot(horizontalDistance, verticalDistance) <= radius) {
                zombie.takeDamage(totalDamage);
                if (zombie.isDead()) {
                    onZombieKilled(zombie);
                }
            }
        }
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering.isAlive() && covering.getCoveredPlant() != plant
                    && Math.hypot(covering.getCol() - centerCol,
                    covering.getRow() - centerRow) <= radius) {
                covering.takeDamage(totalDamage);
            }
        }
        for (ArcadeObstacle obstacle : session.getArcadeObstacles()) {
            if (obstacle.isAlive() && Math.hypot(obstacle.getX() - centerCol,
                    obstacle.getRow() - centerRow) <= radius) {
                obstacle.takeDamage(totalDamage);
            }
        }
        if ("Grave Buster".equals(plant.getName())
                && session.getBoard().getTile(centerCol, centerRow).isGrave()) {
            session.clearGraveAt(centerCol, centerRow);
        }
        if (plant.hasTag(PlantTag.FIRE)
                || plant.getStats().hasSpecialModifier(PlantSpecialModifiers.MELT_AREA_3X3)) {
            meltIceAround(centerCol, centerRow,
                    plant.getStats().hasSpecialModifier(PlantSpecialModifiers.MELT_AREA_3X3)
                            ? 1 : 0);
        }
    }

    @Override
    public void applyFieldModifier(Plant plant, double magnitude) {
        if ("Magnet-shroom".equals(plant.getName())) {
            int range = 1 + (int) plant.getStats()
                    .specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
            Zombie target = findFrontZombie(plant.getRow(), plant.getCol());
            if (target != null && target.getX() - plant.getCol() <= range) {
                target.stripArmorViaMagnet();
            }
            return;
        }
        int radius = (int) plant.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
        double duration = 5.0
                + plant.getStats().specialModifier(PlantSpecialModifiers.DURATION_EXT);
        for (int row = Math.max(0, plant.getRow() - radius);
             row <= Math.min(getRowCount() - 1, plant.getRow() + radius);
             row++) {
            session.applyFieldModifier(row, magnitude, duration);
        }
    }

    @Override
    public void grantArmor(Plant plant, int armorValue) {
        plant.grantArmor(armorValue);
    }

    @Override
    public void dealMeleeDamage(Plant plant, int damage, boolean areaOfEffect) {
        damage = applyPlantDamageModifiers(plant, damage);
        double range = Math.max(1.0, plant.getDefinition().getAbilityValue())
                + plant.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
        if (areaOfEffect || plant.hasTag(PlantTag.AOE)) {
            for (PlantCovering covering : session.getPlantCoverings()) {
                if (covering.getCoveredPlant() != plant
                        && covering.getRow() == plant.getRow()
                        && Math.abs(covering.getCol() - plant.getCol()) <= range) {
                    covering.takeDamage(damage);
                }
            }
            for (ArcadeObstacle obstacle : session.getArcadeObstacles()) {
                if (obstacle.getRow() == plant.getRow()
                        && Math.abs(obstacle.getX() - plant.getCol()) <= range) {
                    obstacle.takeDamage(damage);
                }
            }
            for (Zombie zombie : getZombiesInRow(plant.getRow())) {
                if (Math.abs(zombie.getX() - plant.getCol()) <= range) {
                    zombie.takeDamage(damage);
                    if (zombie.isDead()) {
                        onZombieKilled(zombie);
                    }
                }
            }
            return;
        }
        PlantCovering covering = nearestCoveringAhead(plant, range);
        if (covering != null) {
            covering.takeDamage(damage);
            return;
        }
        ArcadeObstacle obstacle = nearestArcadeAhead(plant, range);
        if (obstacle != null) {
            obstacle.takeDamage(damage);
            return;
        }
        Zombie target = findFrontZombie(plant.getRow(), plant.getCol());
        if (target != null && target.getX() - plant.getCol() <= range) {
            target.takeDamage(damage);
            if (target.isDead()) {
                onZombieKilled(target);
            }
        }
    }

    @Override
    public void projectileBurst(Plant plant, double value) {
        int shots = plant.getStats().damage() > 0
                ? (int) Math.max(1, Math.ceil(value / plant.getStats().damage()))
                : (int) Math.max(1, value);
        ProjectileProfile profile = plant.getAbility() instanceof ProjectileAttackAbility attack
                ? attack.getProfile()
                : ProjectileProfile.straight();
        spawnProjectile(plant, plant.getStats().damage(), shots, profile);
    }

    @Override
    public void hypnotizeRandomZombies(Plant plant, int value) {
        List<Zombie> candidates = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && !zombie.isHypnotized()) {
                candidates.add(zombie);
            }
        }
        Collections.shuffle(candidates, random);
        int count = Math.min(value, candidates.size());
        double healthMultiplier = Math.max(1.0, plant.getStats()
                .specialModifier(PlantSpecialModifiers.ZOMBIE_HEALTH_MULTIPLIER));
        double damageMultiplier = Math.max(1.0, plant.getStats()
                .specialModifier(PlantSpecialModifiers.ZOMBIE_DAMAGE_MULTIPLIER));
        for (int i = 0; i < count; i++) {
            candidates.get(i).hypnotize(healthMultiplier, damageMultiplier);
        }
    }

    @Override
    public void freezeAllZombies(Plant plant, double value) {
        double extension = plant.getStats()
                .specialModifier(PlantSpecialModifiers.FREEZE_DURATION_EXT);
        int baseTicks = (int) Math.ceil((value + extension) * getTicksPerSecond());
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) {
                zombie.applyFreeze(baseTicks);
            }
        }
    }

    @Override
    public void knockbackBlast(Plant plant) {
        for (Zombie zombie : getZombiesInRow(plant.getRow())) {
            if (zombie.getX() >= plant.getCol()) {
                zombie.moveRight(1.5);
            }
        }
    }

    @Override
    public void spawnClones(Plant plant, int value) {
        int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int spawned = 0;
        for (int[] delta : deltas) {
            if (spawned >= value) {
                break;
            }
            int col = plant.getCol() + delta[0];
            int row = plant.getRow() + delta[1];
            if (session.getBoard().canPlace(plant.getDefinition(), col, row) == PlantPlacementResult.SUCCESS) {
                session.createClone(plant, col, row);
                spawned++;
            }
        }
    }

    @Override
    public void pullUnderwater(Plant plant, double value) {
        int row = plant.getRow();
        if (plant.hasTag(PlantTag.WATER)
                && !session.getBoard().getTile(plant.getCol(), row).isWater()) {
            return;
        }
        int targetCount = (int) Math.max(1, value)
                + (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.BONUS_GRAB_TARGETS);
        List<Zombie> targets = new ArrayList<>(getZombiesInRow(row));
        if (plant.hasTag(PlantTag.WATER)) {
            targets.removeIf(zombie -> !zombie.getType().toLowerCase()
                    .contains("snorkel"));
        }
        targets.sort(java.util.Comparator.comparingDouble(
                zombie -> Math.abs(zombie.getX() - plant.getCol())));
        for (int i = 0; i < Math.min(targetCount, targets.size()); i++) {
            Zombie zombie = targets.get(i);
            zombie.takeDirectDamage(zombie.getMaxHealth());
            if (zombie.isDead()) {
                onZombieKilled(zombie);
            }
        }
    }

    @Override
    public void localAreaAttack(Plant plant, double value) {
        int sideLength = Math.max(1, (int) Math.ceil(Math.sqrt(value)));
        int radius = Math.max(1, sideLength / 2);
        int smashCount = 1 + (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.BONUS_SMASH_CHARGES);
        int damage = plant.getStats().damage() * smashCount;
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        for (Zombie zombie : session.getZombies()) {
            int zCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zCol - centerCol) <= radius && Math.abs(zombie.getRow() - centerRow) <= radius) {
                zombie.takeDamage(damage);
                if (zombie.isDead()) {
                    onZombieKilled(zombie);
                }
            }
        }
    }

    @Override
    public boolean movePlant(Plant plant, int col, int row) {
        if (plant == null || !plant.isAlive()
                || session.getBoard().canPlace(plant.getDefinition(), col, row)
                != PlantPlacementResult.SUCCESS) {
            return false;
        }
        int oldCol = plant.getCol();
        int oldRow = plant.getRow();
        session.getBoard().removePlant(plant);
        plant.relocate(col, row);
        try {
            session.getBoard().placePlant(plant);
            return true;
        } catch (IllegalArgumentException ex) {
            plant.relocate(oldCol, oldRow);
            session.getBoard().placePlant(plant);
            return false;
        }
    }

    @Override
    public void createGraves(int count) {
        List<int[]> candidates = new ArrayList<>();
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColCount(); col++) {
                var tile = session.getBoard().getTile(col, row);
                if (getPlantAt(col, row) == null && tile != null
                        && !tile.blocksPlanting() && !tile.isWater()
                        && !tile.isIce() && !tile.isGrave()) {
                    candidates.add(new int[]{col, row});
                }
            }
        }
        Collections.shuffle(candidates, random);
        for (int i = 0; i < Math.min(Math.max(0, count), candidates.size()); i++) {
            int[] cell = candidates.get(i);
            session.getBoard().setTile(cell[0], cell[1], new GraveTile());
        }
    }

    @Override
    public int withdrawSun(int amount) {
        return session.withdrawSun(amount);
    }

    @Override
    public void returnSun(int amount) {
        session.addSunBalance(Math.max(0, amount));
    }

    @Override
    public int stealGroundSun(int maximum) {
        return session.stealGroundSun(maximum);
    }

    @Override
    public boolean isWaterAt(int col, int row) {
        return session.getBoard().inBounds(col, row)
                && session.getBoard().getTile(col, row).isWater();
    }

    @Override
    public void pushIceInRow(int row) {
        if (row < 0 || row >= getRowCount()) {
            return;
        }
        for (int col = 1; col < getColCount(); col++) {
            if (!session.getBoard().getTile(col, row).isIce()) {
                continue;
            }
            var destination = session.getBoard().getTile(col - 1, row);
            if (destination == null || destination.isWater()
                    || destination.isGrave() || destination.isIce()
                    || destination.blocksPlanting()) {
                return;
            }
            Plant target = getPlantAt(col - 1, row);
            if (target != null) {
                if (!target.canBeTargetedByZombie()) {
                    return;
                }
                target.takeDamage(target.getHealth());
                onPlantDestroyed(target);
            }
            session.getBoard().setTile(col - 1, row, new IceTile());
            session.getBoard().setTile(col, row, new NormalTile());
            break;
        }
    }

    @Override
    public void createIceBlocks(int row, int startCol, int count) {
        if (row < 0 || row >= getRowCount()) {
            return;
        }
        int remaining = Math.max(0, count);
        for (int col = Math.min(getColCount() - 1, startCol);
             col >= 0 && remaining > 0; col--) {
            var tile = session.getBoard().getTile(col, row);
            if (getPlantAt(col, row) == null && tile != null
                    && !tile.blocksPlanting() && !tile.isWater()
                    && !tile.isGrave()) {
                session.getBoard().setTile(col, row, new IceTile());
                remaining--;
            }
        }
    }

    @Override
    public PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        return session.coverPlant(plant, type, health);
    }

    @Override
    public void registerHunterIceHit(Plant plant) {
        session.registerHunterIceHit(plant);
    }

    @Override
    public List<PlantCovering> getPlantCoverings() {
        return session.getPlantCoverings();
    }

    @Override
    public void pushArcadeObstacle(Zombie pusher) {
        session.pushArcadeObstacle(pusher);
    }

    @Override
    public void releaseArcadeObstacle(String pusherId) {
        session.releaseArcadeObstacle(pusherId);
    }

    @Override
    public List<ArcadeObstacle> getArcadeObstacles() {
        return session.getArcadeObstacles();
    }

    private void handleDeathTags(Plant plant) {
        if (plant.getStats().hasSpecialModifier(PlantSpecialModifiers.DEATH_EXPLOSION_AOE)) {
            double radius = plant.getStats().specialModifier(PlantSpecialModifiers.DEATH_EXPLOSION_AOE);
            explode(plant, plant.getStats().damage(), radius);
        }
        if (plant.getStats().hasSpecialModifier(PlantSpecialModifiers.EXPLODE_ON_FINISH)) {
            if (plant.getAbility() instanceof ExplosiveAbility explosive) {
                explosive.detonate(plant, this);
            } else {
                explode(plant, plant.getStats().damage(), 1.0);
            }
        }
    }

    private void handleMoveZombieOnEat(Plant plant) {
        if (!plant.hasTag(PlantTag.MOVE_ZOMBIE)) {
            return;
        }
        for (Zombie zombie : getZombiesInRow(plant.getRow())) {
            if (Math.abs(zombie.getX() - plant.getCol()) <= 0.5) {
                int upperRow = plant.getRow() - 1;
                int lowerRow = plant.getRow() + 1;
                if (upperRow < 0 && lowerRow >= getRowCount()) {
                    continue;
                }
                int newRow = upperRow >= 0 && lowerRow < getRowCount()
                        ? (random.nextBoolean() ? upperRow : lowerRow)
                        : (upperRow >= 0 ? upperRow : lowerRow);
                zombie.setRow(newRow);
            }
        }
    }

    private boolean hasFirePlantAhead(Plant plant) {
        if (!plant.hasTag(PlantTag.PEA)) {
            return false;
        }
        for (int col = plant.getCol() + 1; col < getColCount(); col++) {
            Plant ahead = getPlantAt(col, plant.getRow());
            if (ahead != null && ahead.isAlive() && ahead.hasTag(PlantTag.FIRE)) {
                return true;
            }
        }
        return false;
    }

    private int applyPlantDamageModifiers(Plant plant, int damage) {
        double multiplier = 1.0 + session.getFieldModifier(plant.getRow());
        if (session.isFamilyBoosted(plant.getCategory())) {
            multiplier *= 2.0;
        }
        return (int) Math.max(0, Math.round(damage * multiplier));
    }

    private void meltIceAround(int centerCol, int centerRow, int radius) {
        for (int row = centerRow - radius; row <= centerRow + radius; row++) {
            for (int col = centerCol - radius; col <= centerCol + radius; col++) {
                if (session.getBoard().inBounds(col, row)
                        && session.getBoard().getTile(col, row).isIce()) {
                    session.getBoard().setTile(col, row, new NormalTile());
                }
            }
        }
    }

    private Zombie findFrontZombie(int row, int col) {
        Zombie closest = null;
        double closestX = Double.MAX_VALUE;
        for (Zombie zombie : getZombiesInRow(row)) {
            if (zombie.getX() >= col && zombie.getX() < closestX) {
                closest = zombie;
                closestX = zombie.getX();
            }
        }
        return closest;
    }

    private PlantCovering nearestCoveringAhead(Plant source, double range) {
        PlantCovering nearest = null;
        double distance = Double.MAX_VALUE;
        for (PlantCovering covering : session.getPlantCoverings()) {
            double current = covering.getCol() - source.getCol();
            if (covering.isAlive() && covering.getCoveredPlant() != source
                    && covering.getRow() == source.getRow()
                    && current >= 0 && current <= range && current < distance) {
                nearest = covering;
                distance = current;
            }
        }
        return nearest;
    }

    private ArcadeObstacle nearestArcadeAhead(Plant source, double range) {
        ArcadeObstacle nearest = null;
        double distance = Double.MAX_VALUE;
        for (ArcadeObstacle obstacle : session.getArcadeObstacles()) {
            double current = obstacle.getX() - source.getCol();
            if (obstacle.isAlive() && obstacle.getRow() == source.getRow()
                    && current >= 0 && current <= range && current < distance) {
                nearest = obstacle;
                distance = current;
            }
        }
        return nearest;
    }
}
