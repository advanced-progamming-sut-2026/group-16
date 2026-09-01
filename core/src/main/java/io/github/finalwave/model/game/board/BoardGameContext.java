package io.github.finalwave.model.game.board;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.GooPuddle;
import io.github.finalwave.model.game.LawnBurst;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.PendingGraveLanding;
import io.github.finalwave.model.game.board.tile.CraterTile;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.plant.ability.BonkChoyAbility;
import io.github.finalwave.model.game.entity.plant.ability.WasabiWhipAbility;
import io.github.finalwave.model.game.entity.plant.ability.PhatBeetPulseMark;
import io.github.finalwave.model.game.entity.plant.ability.KiwibeastPulseMark;
import io.github.finalwave.model.game.entity.projectile.EndurianMuzzles;
import io.github.finalwave.model.game.entity.projectile.PhatBeetMuzzles;
import io.github.finalwave.model.game.entity.projectile.WasabiWhipMuzzles;
import io.github.finalwave.model.game.entity.plant.ability.DoomShroomAbility;
import io.github.finalwave.model.game.entity.plant.ability.IcebergLettuceAbility;
import io.github.finalwave.model.game.entity.plant.ability.ExplosiveAbility;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;
import io.github.finalwave.model.game.entity.plant.ability.ProjectileAttackAbility;
import io.github.finalwave.model.game.entity.plant.support.PlantLaneSupport;
import io.github.finalwave.model.game.entity.projectile.IcebergLettuceMuzzles;
import io.github.finalwave.model.game.entity.projectile.DoomShroomMuzzles;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
    public boolean lockZombieLanes() {
        return session.isIZombieActive();
    }

    @Override
    public Plant getPlantAt(int col, int row) {
        return session.getBoard().getPlantAt(col, row);
    }

    @Override
    public PlantDefinition findPlantDefinition(String plantName) {
        return session.getPlantRegistry().getDefinition(plantName);
    }

    @Override
    public Plant getPlantInFront(double zombieX, int row) {
        return session.getBoard().getPlantInFront(zombieX, row);
    }

    @Override
    public List<Zombie> getZombiesInRow(int row) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.occupiesRow(row)) {
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
    public Zombie spawnZombieOfType(String alias, int row, double x) {
        return session.spawnZombieOfType(alias, row, x);
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
    public boolean zombiesWalkOffLawn() {
        return session.isSandboxPractice();
    }

    @Override
    public void despawnWalkOffZombie(Zombie zombie) {
        session.despawnWalkOffZombie(zombie);
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
                plant, resolvedDamage, shots, profile, effect, getRowCount());
    }

    @Override
    public void spawnReverseProjectile(Plant plant, int damage, int shots, ProjectileProfile profile) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        ProjectileEffect effect = hasFirePlantAhead(plant)
                ? ProjectileEffect.FIRE
                : plant.projectileEffect();
        session.getProjectileSystem().spawnFromPlant(
                plant, resolvedDamage, shots, profile, effect, true);
    }

    @Override
    public void spawnProjectile(Plant plant, int damage, ProjectileProfile profile, int muzzleIndex) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        ProjectileEffect effect = hasFirePlantAhead(plant)
                ? ProjectileEffect.FIRE
                : plant.projectileEffect();
        session.getProjectileSystem().spawnPeaPodHead(
                plant, resolvedDamage, profile, effect, muzzleIndex);
    }

    @Override
    public void spawnDirectedProjectile(Plant plant, int damage, double vx, double vy, float visualScale) {
        spawnDirectedProjectile(plant, damage, vx, vy, visualScale, 0, 0);
    }

    @Override
    public void spawnDirectedProjectile(Plant plant, int damage, double vx, double vy, float visualScale,
                                        double laneOffset, double extraX) {
        spawnDirectedProjectile(plant, damage, vx, vy, visualScale, laneOffset, extraX, null);
    }

    @Override
    public void spawnDirectedProjectile(Plant plant, int damage, double vx, double vy, float visualScale,
                                        double laneOffset, double extraX, ProjectileEffect effect) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        ProjectileProfile profile = plant.getAbility() instanceof ProjectileAttackAbility attack
                ? attack.getProfile()
                : ProjectileProfile.straight();
        ProjectileEffect resolvedEffect = effect != null ? effect : resolveDirectedEffect(plant, visualScale);
        session.getProjectileSystem().spawnDirectedFromPlant(
                plant, resolvedDamage, vx, vy, profile, resolvedEffect, visualScale, laneOffset, extraX);
    }

    @Override
    public void spawnPoisonLaneBall(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnPoisonLaneBallFromPlant(plant, resolvedDamage);
    }

    @Override
    public void addGooLaneTrail(Plant plant, int durationTicks) {
        session.addGooLaneTrail(plant, durationTicks);
    }

    @Override
    public void addGooPuddle(int col, int row, int durationTicks) {
        session.addGooPuddle(col, row, durationTicks);
    }

    @Override
    public List<GooPuddle> getGooPuddles() {
        return session.getGooPuddles();
    }

    private static ProjectileEffect resolveDirectedEffect(Plant plant, float visualScale) {
        if (visualScale >= PlantShotPatterns.GIANT_PEA_SCALE) {
            return ProjectileEffect.GIANT_PEA;
        }
        return plant.projectileEffect();
    }

    @Override
    public void spawnLaneClearProjectile(Plant plant, int damage, ProjectileEffect effect) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnLaneClearFromPlant(plant, resolvedDamage, effect);
    }

    @Override
    public void spawnBowlingProjectile(Plant plant, int damage, ProjectileEffect effect,
                                       ProjectileProfile profile) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnBowlingFromPlant(plant, resolvedDamage, effect);
    }

    @Override
    public void spawnPiercingProjectile(Plant plant, int damage, ProjectileEffect effect, int pierce) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnPiercingFromPlant(plant, resolvedDamage, effect, pierce);
    }

    @Override
    public void spawnPeaPodGiant(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        ProjectileEffect effect = hasFirePlantAhead(plant)
                ? ProjectileEffect.FIRE
                : plant.projectileEffect();
        session.getProjectileSystem().spawnPeaPodGiant(plant, resolvedDamage, effect);
    }

    @Override
    public void spawnCabbagePlantFood(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnCabbagePlantFood(plant, resolvedDamage);
    }

    @Override
    public void spawnKernelPlantFood(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnKernelPlantFood(
                plant, resolvedDamage, session.getZombies());
    }

    @Override
    public void spawnMelonPlantFood(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnMelonPlantFood(plant, resolvedDamage);
    }

    @Override
    public void spawnWinterMelonPlantFood(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnWinterMelonPlantFood(plant, resolvedDamage);
    }

    @Override
    public void spawnPepperPlantFood(Plant plant, int damage, int muzzleIndex) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getProjectileSystem().spawnPepperPlantFood(plant, resolvedDamage, muzzleIndex);
    }

    @Override
    public void spawnGrapeshotGrapes(Plant plant, int count, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        int bonus = (int) plant.getStats().specialModifier(PlantSpecialModifiers.EXPLODE_DAMAGE_BUFF);
        session.getProjectileSystem().spawnGrapeshotGrapes(
                plant, count, resolvedDamage + bonus, getRowCount(), getColCount());
    }

    @Override
    public void startJalapenoRowFire(Plant plant, int damage) {
        int resolvedDamage = applyPlantDamageModifiers(plant, damage);
        session.getJalapenoFireSystem().scheduleRowFire(
                plant, resolvedDamage, session.getCurrentTick(), getColCount());
    }

    @Override
    public void explodeSquare(int centerCol, int centerRow, int damage, int tileRadius, Plant source) {
        int totalDamage = source == null ? damage : applyPlantDamageModifiers(source, damage);
        if (source != null) {
            totalDamage += (int) source.getStats().specialModifier(PlantSpecialModifiers.EXPLODE_DAMAGE_BUFF);
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isDead() || zombie.isHypnotized()) {
                continue;
            }
            int zombieCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zombieCol - centerCol) <= tileRadius
                    && Math.abs(zombie.getRow() - centerRow) <= tileRadius) {
                damageZombieByExplosion(zombie, totalDamage);
            }
        }
    }

    @Override
    public void spawnDoomShroomSeedling(int centerCol, int centerRow, int tileRadius, Plant source) {
        if (source == null) {
            return;
        }
        List<int[]> candidates = new ArrayList<>();
        for (int row = centerRow - tileRadius; row <= centerRow + tileRadius; row++) {
            for (int col = centerCol - tileRadius; col <= centerCol + tileRadius; col++) {
                if (col == centerCol && row == centerRow) {
                    continue;
                }
                if (session.getBoard().canPlace(source.getDefinition(), col, row)
                        == PlantPlacementResult.SUCCESS) {
                    candidates.add(new int[] {col, row});
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int[] tile = candidates.get(random.nextInt(candidates.size()));
        session.createDoomShroomSeedling(source, tile[0], tile[1]);
    }

    @Override
    public void spawnDoomShroomSeedlings(int centerCol, int centerRow, int tileRadius, Plant source) {
        spawnDoomShroomSeedling(centerCol, centerRow, tileRadius, source);
    }

    @Override
    public void triggerDoomShroomPlantFood(Plant plant) {
        for (Plant candidate : session.getBoard().getAllPlants()) {
            if (candidate == null || !candidate.isAlive() || !candidate.isDoomShroom()) {
                continue;
            }
            if (candidate.getAbility() instanceof DoomShroomAbility ability) {
                ability.startPlantFoodAdvance(candidate, this);
            }
        }
    }

    @Override
    public void enqueueTangleKelpGrabMark(int col, int row) {
        session.getTangleKelpGrabSystem().enqueue(col, row);
    }

    @Override
    public <T> void shuffle(List<T> list) {
        Collections.shuffle(list, random);
    }

    @Override
    public void placeTimedCrater(int col, int row, float durationSeconds) {
        if (!session.getBoard().inBounds(col, row)) {
            return;
        }
        session.getBoard().setTile(col, row, new CraterTile());
        session.getCraterSystem().schedule(
                col, row, session.getCurrentTick(), DoomShroomMuzzles.craterDurationTicks());
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
        spawnSun(plant, total, SunType.NORMAL);
    }

    @Override
    public void spawnSun(Plant plant, double total, SunType type) {
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
        SunType resolved = type == null ? SunType.NORMAL : type;
        session.spawnSunItem(new Sun(plant.getCol(), plant.getRow(), value, resolved, true));
        plant.beginSunProduce(GameSession.TICKS_PER_SECOND);
        MatchListener listener = session.getMatchListener();
        if (listener != null) {
            listener.onSunProduced(plant, plant.getCol(), plant.getRow());
        }
    }

    @Override
    public void spawnSunAt(int col, int row, int value, SunType type) {
        int clampedCol = Math.max(0, Math.min(session.getBoard().getCols() - 1, col));
        int clampedRow = Math.max(0, Math.min(session.getBoard().getRows() - 1, row));
        session.spawnSunItem(new Sun(clampedCol, clampedRow, Math.max(1, value),
                type == null ? SunType.NORMAL : type, true));
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
            if (zombieInBlast(zombie, centerCol, centerRow, radius)) {
                damageZombieByExplosion(zombie, damage);
            }
        }
        session.queueLawnBurst(new LawnBurst(LawnBurst.Kind.CHERRY, centerCol, centerRow));
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
            if (zombieInExplosion(plant, zombie, centerCol, centerRow, radius)) {
                damageZombieByExplosion(zombie, totalDamage);
            }
        }
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering.isAlive() && covering.getCoveredPlant() != plant
                    && inBlast(covering.getCol(), covering.getRow(), centerCol, centerRow, radius)) {
                covering.takeDamage(totalDamage);
            }
        }
        for (ArcadeObstacle obstacle : session.getArcadeObstacles()) {
            if (obstacle.isAlive() && inBlast(
                    (int) Math.floor(obstacle.getX()), obstacle.getRow(),
                    centerCol, centerRow, radius)) {
                obstacle.takeDamage(totalDamage);
            }
        }
        for (PianoObstacle obstacle : session.getPianoObstacles()) {
            if (obstacle.isAlive() && inBlast(
                    (int) Math.floor(obstacle.getX()), obstacle.getRow(),
                    centerCol, centerRow, radius)) {
                obstacle.takeDamage(totalDamage);
            }
        }
        damageTilesInRadius(centerCol, centerRow, radius, totalDamage,
                plant.hasTag(PlantTag.FIRE)
                        || plant.getStats().hasSpecialModifier(PlantSpecialModifiers.MELT_AREA_3X3));
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
        session.queueLawnBurst(new LawnBurst(LawnBurst.kindForPlant(plant.getName()), centerCol, centerRow));
    }

    @Override
    public void clearGraveAt(int col, int row) {
        session.clearGraveAt(col, row);
    }

    @Override
    public void queueLawnBurst(LawnBurst burst) {
        session.queueLawnBurst(burst);
    }

    private static boolean inExplosionRange(Plant plant,
                                            double radius,
                                            int centerCol,
                                            int centerRow,
                                            double targetX,
                                            int targetRow) {
        double abilityValue = plant.getDefinition().getAbilityValue();
        if (abilityValue > 1.0) {
            int sideLength = Math.max(1, (int) Math.ceil(Math.sqrt(abilityValue)));
            int tileRadius = Math.max(1, sideLength / 2);
            int targetCol = (int) Math.floor(targetX);
            return Math.abs(targetCol - centerCol) <= tileRadius
                    && Math.abs(targetRow - centerRow) <= tileRadius;
        }
        double horizontalDistance = targetX - centerCol;
        double verticalDistance = targetRow - centerRow;
        return Math.hypot(horizontalDistance, verticalDistance) <= radius;
    }

    @Override
    public void applyFieldModifier(Plant plant, double magnitude) {
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
            dealMeleeAreaDamage(plant, damage, range);
            return;
        }
        dealMeleeSingleDamage(plant, damage, range);
    }

    private void dealMeleeAreaDamage(Plant plant, int damage, double range) {
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
        for (PianoObstacle obstacle : session.getPianoObstacles()) {
            if (obstacle.getRow() == plant.getRow()
                    && Math.abs(obstacle.getX() - plant.getCol()) <= range) {
                obstacle.takeDamage(damage);
            }
        }
        for (int col = 0; col < getColCount(); col++) {
            if (Math.abs(col - plant.getCol()) > range) {
                continue;
            }
            damageGraveAt(col, plant.getRow(), damage);
            damageIceAt(col, plant.getRow(), damage);
        }
        for (Zombie zombie : getZombiesInRow(plant.getRow())) {
            if (Math.abs(zombie.getX() - plant.getCol()) <= range) {
                zombie.takeDamage(damage);
                if (zombie.isDead()) {
                    onZombieKilled(zombie);
                }
            }
        }
    }

    private void dealMeleeSingleDamage(Plant plant, int damage, double range) {
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
        PianoObstacle piano = nearestPianoAhead(plant, range);
        if (piano != null) {
            piano.takeDamage(damage);
            return;
        }
        int tileCol = nearestDamageableTileColAhead(plant.getRow(), plant.getCol(), range);
        if (tileCol >= 0) {
            Tile tile = session.getBoard().getTile(tileCol, plant.getRow());
            if (tile != null && tile.isGrave()) {
                damageGraveAt(tileCol, plant.getRow(), damage);
                return;
            }
            if (tile != null && tile.isIce()) {
                damageIceAt(tileCol, plant.getRow(), damage);
                return;
            }
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
    public void dealBonkChoyPunch(Plant plant, BonkChoyAbility.PunchStyle style, int damage) {
        damage = applyPlantDamageModifiers(plant, damage);
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        switch (style) {
            case RIGHT -> {
                damageTile(plantCol + 1, plantRow, damage);
                damageTile(plantCol, plantRow, damage);
                damageZombiesOnTile(plantRow, plantCol, plantCol + 1, damage);
            }
            case LEFT -> {
                damageTile(plantCol - 1, plantRow, damage);
                damageZombiesOnTile(plantRow, plantCol - 1, plantCol, damage);
            }
            case BOTH -> {
                damageTile(plantCol + 1, plantRow, damage);
                damageTile(plantCol - 1, plantRow, damage);
                damageTile(plantCol, plantRow, damage);
                damageZombiesOnTile(plantRow, plantCol - 1, plantCol + 1, damage);
            }
            case UP_RIGHT -> {
                damageTile(plantCol + 1, plantRow - 1, damage);
                damageZombiesOnTile(plantRow - 1, plantCol + 1, plantCol + 1, damage);
            }
            case UP_LEFT -> {
                damageTile(plantCol - 1, plantRow - 1, damage);
                damageZombiesOnTile(plantRow - 1, plantCol - 1, plantCol - 1, damage);
            }
        }
    }

    @Override
    public void dealWasabiWhipPunch(Plant plant, WasabiWhipAbility.WhipStyle style, int damage) {
        damage = applyPlantDamageModifiers(plant, damage);
        int splash = damage / 2;
        int range = WasabiWhipMuzzles.rangeTiles(plant);
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int minCol;
        int maxCol;
        int targetRow;
        switch (style) {
            case LEFT -> {
                minCol = plantCol - range;
                maxCol = plantCol - 1;
                whipLane(plantRow, minCol, maxCol, damage, splash);
            }
            case RIGHT -> {
                minCol = plantCol;
                maxCol = plantCol + range;
                whipLane(plantRow, minCol, maxCol, damage, splash);
            }
            case UP_LEFT -> {
                minCol = plantCol - range;
                maxCol = plantCol - 1;
                targetRow = plantRow - 1;
                whipDiagonal(targetRow, minCol, maxCol, damage);
            }
            case DOWN_LEFT -> {
                minCol = plantCol - range;
                maxCol = plantCol - 1;
                targetRow = plantRow + 1;
                whipDiagonal(targetRow, minCol, maxCol, damage);
            }
            case UP_RIGHT -> {
                minCol = plantCol + 1;
                maxCol = plantCol + range;
                targetRow = plantRow - 1;
                whipDiagonal(targetRow, minCol, maxCol, damage);
            }
            case DOWN_RIGHT -> {
                minCol = plantCol + 1;
                maxCol = plantCol + range;
                targetRow = plantRow + 1;
                whipDiagonal(targetRow, minCol, maxCol, damage);
            }
        }
    }

    private void whipLane(int plantRow, int minCol, int maxCol, int damage, int splash) {
        for (int col = minCol; col <= maxCol; col++) {
            damageTile(col, plantRow, damage);
            damageTile(col, plantRow - 1, splash);
            damageTile(col, plantRow + 1, splash);
        }
        damageZombiesOnTile(plantRow, minCol, maxCol, damage);
        damageZombiesOnTile(plantRow - 1, minCol, maxCol, splash);
        damageZombiesOnTile(plantRow + 1, minCol, maxCol, splash);
    }

    private void whipDiagonal(int row, int minCol, int maxCol, int damage) {
        for (int col = minCol; col <= maxCol; col++) {
            damageTile(col, row, damage);
        }
        damageZombiesOnTile(row, minCol, maxCol, damage);
    }

    @Override
    public void dealBonkChoyAreaPunch(Plant plant, int radius, int damage) {
        damage = applyPlantDamageModifiers(plant, damage);
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        for (int row = centerRow - radius; row <= centerRow + radius; row++) {
            for (int col = centerCol - radius; col <= centerCol + radius; col++) {
                damageTile(col, row, damage);
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
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
    public void dealPhatBeetShockwave(Plant plant, int damage) {
        if (plant == null) {
            return;
        }
        damage = applyPlantDamageModifiers(plant, damage);
        LinkedHashSet<PhatBeetPulseMark.HitTile> hits = new LinkedHashSet<>();
        slamPhatBeetRing(plant, 0, PhatBeetMuzzles.INNER_RADIUS, damage, hits);
        session.getPhatBeetPulseSystem().enqueue(new PhatBeetPulseMark(
                plant.getCol(), plant.getRow(), false, List.copyOf(hits)));
    }

    @Override
    public void dealPhatBeetPlantFood(Plant plant) {
        if (plant == null) {
            return;
        }
        int inner = applyPlantDamageModifiers(plant, PhatBeetMuzzles.INNER_PLANT_FOOD_DAMAGE);
        int outer = applyPlantDamageModifiers(plant, PhatBeetMuzzles.OUTER_PLANT_FOOD_DAMAGE);
        LinkedHashSet<PhatBeetPulseMark.HitTile> hits = new LinkedHashSet<>();
        slamPhatBeetRing(plant, 0, PhatBeetMuzzles.INNER_RADIUS, inner, hits);
        slamPhatBeetRing(plant, PhatBeetMuzzles.INNER_RADIUS + 1, PhatBeetMuzzles.OUTER_RADIUS, outer, hits);
        session.getPhatBeetPulseSystem().enqueue(new PhatBeetPulseMark(
                plant.getCol(), plant.getRow(), true, List.copyOf(hits)));
    }

    @Override
    public void dealKiwibeastShockwave(Plant plant, int damage, int radius, boolean plantFood) {
        if (plant == null) {
            return;
        }
        damage = applyPlantDamageModifiers(plant, damage);
        LinkedHashSet<KiwibeastPulseMark.HitTile> hits = new LinkedHashSet<>();
        slamKiwibeastRing(plant, 0, Math.max(0, radius), damage, hits);
        session.getKiwibeastPulseSystem().enqueue(new KiwibeastPulseMark(
                plant.getCol(), plant.getRow(), plantFood, List.copyOf(hits)));
    }

    @Override
    public void dealEndurianSpikes(Plant plant, int damage) {
        if (plant == null) {
            return;
        }
        damage = applyPlantDamageModifiers(plant, damage);
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        int radius = EndurianMuzzles.RADIUS;
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (!covering.isAlive() || covering.getCoveredPlant() == plant) {
                continue;
            }
            if (Math.max(Math.abs(covering.getCol() - centerCol),
                    Math.abs(covering.getRow() - centerRow)) <= radius) {
                covering.takeDamage(damage);
            }
        }
        for (ArcadeObstacle obstacle : session.getArcadeObstacles()) {
            if (!obstacle.isAlive()) {
                continue;
            }
            int obstacleCol = (int) Math.floor(obstacle.getX());
            if (Math.max(Math.abs(obstacleCol - centerCol),
                    Math.abs(obstacle.getRow() - centerRow)) <= radius) {
                obstacle.takeDamage(damage);
            }
        }
        for (int row = centerRow - radius; row <= centerRow + radius; row++) {
            for (int col = centerCol - radius; col <= centerCol + radius; col++) {
                if (Math.max(Math.abs(col - centerCol), Math.abs(row - centerRow)) > radius) {
                    continue;
                }
                damageTile(col, row, damage);
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            if (Math.max(Math.abs(zCol - centerCol), Math.abs(zombie.getRow() - centerRow)) > radius) {
                continue;
            }
            zombie.takeDamage(damage);
            if (zombie.isDead()) {
                onZombieKilled(zombie);
            }
        }
    }

    @Override
    public void knockbackEatingZombies(Plant plant) {
        if (plant == null) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive() || zombie.getState() != ZombieState.EATING) {
                continue;
            }
            Plant target = getPlantInFront(zombie.getX(), zombie.getRow());
            if (target != plant) {
                continue;
            }
            zombie.moveRight(1.0);
            zombie.setState(ZombieState.MOVING);
        }
    }

    @Override
    public void knockbackNearbyZombies(Plant plant, int radius) {
        if (plant == null) {
            return;
        }
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int chebyshevRadius = Math.max(0, radius);
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            if (Math.max(Math.abs(zCol - plantCol), Math.abs(zombie.getRow() - plantRow)) > chebyshevRadius) {
                continue;
            }
            zombie.moveRight(1.0);
            zombie.setState(ZombieState.MOVING);
        }
    }

    private void slamKiwibeastRing(Plant plant,
                                   int minRadius,
                                   int maxRadius,
                                   int damage,
                                   Set<KiwibeastPulseMark.HitTile> hits) {
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        for (int row = centerRow - maxRadius; row <= centerRow + maxRadius; row++) {
            for (int col = centerCol - maxRadius; col <= centerCol + maxRadius; col++) {
                int chebyshev = Math.max(Math.abs(col - centerCol), Math.abs(row - centerRow));
                if (chebyshev < minRadius || chebyshev > maxRadius) {
                    continue;
                }
                damageTile(col, row, damage);
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            int chebyshev = Math.max(Math.abs(zCol - centerCol), Math.abs(zombie.getRow() - centerRow));
            if (chebyshev < minRadius || chebyshev > maxRadius) {
                continue;
            }
            zombie.takeDamage(damage);
            hits.add(new KiwibeastPulseMark.HitTile(zCol, zombie.getRow()));
            if (zombie.isDead()) {
                onZombieKilled(zombie);
            }
        }
    }

    private void slamPhatBeetRing(Plant plant,
                                  int minRadius,
                                  int maxRadius,
                                  int damage,
                                  Set<PhatBeetPulseMark.HitTile> hits) {
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        for (int row = centerRow - maxRadius; row <= centerRow + maxRadius; row++) {
            for (int col = centerCol - maxRadius; col <= centerCol + maxRadius; col++) {
                int chebyshev = Math.max(Math.abs(col - centerCol), Math.abs(row - centerRow));
                if (chebyshev < minRadius || chebyshev > maxRadius) {
                    continue;
                }
                damageTile(col, row, damage);
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            int chebyshev = Math.max(Math.abs(zCol - centerCol), Math.abs(zombie.getRow() - centerRow));
            if (chebyshev < minRadius || chebyshev > maxRadius) {
                continue;
            }
            zombie.takeDamage(damage);
            hits.add(new PhatBeetPulseMark.HitTile(zCol, zombie.getRow()));
            if (zombie.isDead()) {
                onZombieKilled(zombie);
            }
        }
    }

    private void damageTile(int col, int row, int damage) {
        if (col < 0 || col >= getColCount() || row < 0 || row >= getRowCount()) {
            return;
        }
        damageGraveAt(col, row, damage);
        damageIceAt(col, row, damage);
    }

    private void damageZombiesOnTile(int row, int minCol, int maxCol, int damage) {
        for (Zombie zombie : getZombiesInRow(row)) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            if (zCol >= minCol && zCol <= maxCol) {
                zombie.takeDamage(damage);
                if (zombie.isDead()) {
                    onZombieKilled(zombie);
                }
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
    public void completeImitaterMorph(Plant imitater) {
        session.morphImitater(imitater);
    }

    @Override
    public void freezeGroundedZombiesForIceberg(Plant plant, double baseSeconds, double extensionSeconds) {
        int ticks = IcebergLettuceMuzzles.mapFreezeTicks(baseSeconds, extensionSeconds);
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            if (IcebergLettuceAbility.isAirborne(zombie, plant)) {
                continue;
            }
            if (IcebergLettuceAbility.shouldChillInsteadOfFreeze(zombie, this)) {
                zombie.applyChill(ticks);
            } else {
                zombie.applyFreeze(ticks);
            }
        }
    }

    @Override
    public void enqueueIcebergFlash() {
        session.getIcebergFlashSystem().enqueue();
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
    public void spawnForwardClones(Plant plant, int count) {
        List<int[]> candidates = new ArrayList<>();
        int sourceCol = plant.getCol();
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = sourceCol + 1; col < session.getBoard().getCols(); col++) {
                if (session.getBoard().canPlace(plant.getDefinition(), col, row)
                        == PlantPlacementResult.SUCCESS) {
                    candidates.add(new int[] {col, row});
                }
            }
        }
        Collections.shuffle(candidates, session.getRandom());
        int spawned = 0;
        for (int[] tile : candidates) {
            if (spawned >= count) {
                break;
            }
            session.createPlantFoodClone(plant, tile[0], tile[1]);
            spawned++;
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
    public int stealGroundSun(Zombie thief, int maximum) {
        return session.stealGroundSun(thief, maximum);
    }

    @Override
    public boolean isWaterAt(int col, int row) {
        return session.getBoard().inBounds(col, row)
                && session.getBoard().getTile(col, row).isWater();
    }

    @Override
    public void pushIceInRow(int row) {
        pushIceStack(row, Double.POSITIVE_INFINITY);
    }

    @Override
    public void pushIceAhead(Zombie zombie) {
        if (zombie == null) {
            return;
        }
        pushIceStack(zombie.getRow(), zombie.getX());
    }

    private void pushIceStack(int row, double pusherX) {
        if (row < 0 || row >= getRowCount()) {
            return;
        }
        int right = -1;
        for (int col = 0; col < getColCount(); col++) {
            if (session.getBoard().getTile(col, row).isIce() && col < pusherX) {
                right = col;
            }
        }
        if (right < 0 || pusherX >= right + 0.85) {
            return;
        }
        int left = right;
        while (left > 0 && session.getBoard().getTile(left - 1, row).isIce()) {
            left--;
        }
        int dest = left - 1;
        if (dest < 0) {
            return;
        }
        var destination = session.getBoard().getTile(dest, row);
        if (destination == null || destination.isWater()
                || destination.isGrave() || destination.isIce()
                || destination.blocksPlanting()) {
            return;
        }
        Plant target = getPlantAt(dest, row);
        if (target != null) {
            if (!target.canBeTargetedByZombie()) {
                return;
            }
            target.takeDamage(target.getHealth());
            onPlantDestroyed(target);
        }
        session.getBoard().setTile(dest, row, new IceTile());
        session.getBoard().setTile(right, row, new NormalTile());
    }

    @Override
    public void createIceBlocks(int row, int startCol, int count) {
        if (row < 0 || row >= getRowCount()) {
            return;
        }
        int remaining = Math.max(0, count);
        int start = Math.min(getColCount() - 1, startCol);
        for (int col = start; col >= 0 && remaining > 0; col--) {
            var tile = session.getBoard().getTile(col, row);
            if (tile == null || tile.isWater() || tile.isGrave()) {
                break;
            }
            Plant occupant = getPlantAt(col, row);
            if (occupant != null) {
                if (!occupant.canBeTargetedByZombie()) {
                    break;
                }
                occupant.takeDamage(occupant.getHealth());
                onPlantDestroyed(occupant);
            }
            session.getBoard().setTile(col, row, new IceTile());
            remaining--;
        }
    }

    @Override
    public boolean damageGraveAt(int col, int row, int amount) {
        return session.damageGraveAt(col, row, amount);
    }

    @Override
    public boolean damageIceAt(int col, int row, int amount) {
        return session.damageIceAt(col, row, amount);
    }

    @Override
    public PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        return session.coverPlant(plant, type, health);
    }

    @Override
    public PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health, Zombie source) {
        return session.coverPlant(plant, type, health, source);
    }

    @Override
    public boolean canThrowTombBones(Zombie source, int count, int maxGraves) {
        if (source == null || count <= 0) {
            return false;
        }
        int remainingSlots = Math.max(0, maxGraves - countGraves() - session.pendingGraveCount());
        return remainingSlots > 0 && !emptyGraveTiles().isEmpty();
    }

    @Override
    public int throwTombBones(Zombie source, int count, int maxGraves) {
        if (source == null || count <= 0) {
            return 0;
        }
        int remainingSlots = Math.max(0, maxGraves - countGraves() - session.pendingGraveCount());
        int toThrow = Math.min(count, remainingSlots);
        if (toThrow <= 0) {
            return 0;
        }
        List<int[]> candidates = emptyGraveTiles();
        if (candidates.isEmpty()) {
            return 0;
        }
        Collections.shuffle(candidates, random);
        int zombieRow = source.getRow();
        int zombieCol = (int) Math.floor(source.getX());
        candidates.sort((a, b) -> {
            boolean aAhead = a[0] <= zombieCol - 2;
            boolean bAhead = b[0] <= zombieCol - 2;
            if (aAhead != bAhead) {
                return aAhead ? -1 : 1;
            }
            int da = Math.abs(a[1] - zombieRow) * 4 + Math.abs(a[0] - (zombieCol - 4));
            int db = Math.abs(b[1] - zombieRow) * 4 + Math.abs(b[0] - (zombieCol - 4));
            return Integer.compare(da, db);
        });
        int hold = 16;
        int flight = 14;
        int total = hold + flight;
        int thrown = 0;
        for (int i = 0; i < Math.min(toThrow, candidates.size()); i++) {
            int[] cell = candidates.get(i);
            session.queueGraveLanding(new PendingGraveLanding(
                    session.nextBoneId(), cell[0], cell[1], total, total, hold,
                    source.getX() - 0.35, source.getY()));
            thrown++;
        }
        return thrown;
    }

    private List<int[]> emptyGraveTiles() {
        List<int[]> candidates = new ArrayList<>();
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColCount(); col++) {
                var tile = session.getBoard().getTile(col, row);
                if (getPlantAt(col, row) == null && tile != null
                        && !tile.blocksPlanting() && !tile.isWater()
                        && !tile.isIce() && !tile.isGrave()
                        && !session.hasPendingGraveAt(col, row)) {
                    candidates.add(new int[]{col, row});
                }
            }
        }
        return candidates;
    }

    @Override
    public void queuePlantBurn(int col, int row) {
        session.queueLawnBurst(new LawnBurst(LawnBurst.Kind.BURN, col, row));
    }

    @Override
    public void fireLaneLaser(int row, int fromCol, int span) {
        fireLaneLaser(row, fromCol, span, 0);
    }

    @Override
    public void fireLaneLaser(int row, int fromCol, int span, int delayTicks) {
        fireLaneLaser(row, fromCol, span, delayTicks, fromCol + 0.5);
    }

    @Override
    public void fireLaneLaser(int row, int fromCol, int span, int delayTicks, double originX) {
        session.queueLaneLaser(row, fromCol, Math.max(1, span), Math.max(0, delayTicks), originX);
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

    @Override
    public void pushPianoObstacle(Zombie pusher) {
        session.pushPianoObstacle(pusher);
    }

    @Override
    public void releasePianoObstacle(String pusherId) {
        session.releasePianoObstacle(pusherId);
    }

    @Override
    public List<PianoObstacle> getPianoObstacles() {
        return session.getPianoObstacles();
    }

    private void handleDeathTags(Plant plant) {
        if (plant.hasTag(PlantTag.EXPLOSIVE) && plant.getStats().damage() > 0) {
            explode(plant, plant.getStats().damage(), 1.0);
        }
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
        if (!"Garlic".equals(plant.getName()) || session.isIZombieActive()) {
            return;
        }
        for (Zombie zombie : getZombiesInRow(plant.getRow())) {
            if (Math.abs(zombie.getX() - plant.getCol()) <= 0.5) {
                PlantLaneSupport.divertBiter(zombie, plant, this);
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
                    damageIceAt(col, row, IceTile.MAX_HEALTH);
                }
            }
        }
    }

    private void damageTilesInRadius(int centerCol, int centerRow, double radius,
                                     int damage, boolean fireMeltsIce) {
        int searchRadius = (int) Math.ceil(radius);
        for (int row = centerRow - searchRadius; row <= centerRow + searchRadius; row++) {
            for (int col = centerCol - searchRadius; col <= centerCol + searchRadius; col++) {
                if (!session.getBoard().inBounds(col, row)) {
                    continue;
                }
                if (!inBlast(col, row, centerCol, centerRow, radius)) {
                    continue;
                }
                Tile tile = session.getBoard().getTile(col, row);
                if (tile != null && tile.isGrave()) {
                    damageGraveAt(col, row, damage);
                }
                if (tile != null && tile.isIce()) {
                    damageIceAt(col, row, fireMeltsIce ? IceTile.MAX_HEALTH : damage);
                }
            }
        }
    }

    private int nearestDamageableTileColAhead(int row, int fromCol, double range) {
        int nearest = -1;
        double distance = Double.MAX_VALUE;
        for (int col = fromCol; col < getColCount(); col++) {
            double current = col - fromCol;
            if (current < 0 || current > range || current >= distance) {
                continue;
            }
            Tile tile = session.getBoard().getTile(col, row);
            if (tile != null && (tile.isGrave() || tile.isIce())) {
                nearest = col;
                distance = current;
            }
        }
        return nearest;
    }

    private static boolean inBlast(int col, int row, int centerCol, int centerRow, double radius) {
        return Math.max(Math.abs(col - centerCol), Math.abs(row - centerRow)) <= radius;
    }

    private static boolean zombieInBlast(Zombie zombie, int centerCol, int centerRow, double radius) {
        int col = (int) Math.floor(zombie.getX());
        for (int row : zombie.occupiedRows()) {
            if (inBlast(col, row, centerCol, centerRow, radius)) {
                return true;
            }
        }
        return false;
    }

    private static boolean zombieInExplosion(Plant plant, Zombie zombie,
                                             int centerCol, int centerRow, double radius) {
        if (plant.getDefinition().getAbilityValue() > 1.0) {
            for (int row : zombie.occupiedRows()) {
                if (inExplosionRange(plant, radius, centerCol, centerRow, zombie.getX(), row)) {
                    return true;
                }
            }
            return false;
        }
        return zombieInBlast(zombie, centerCol, centerRow, radius);
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

    private PianoObstacle nearestPianoAhead(Plant source, double range) {
        PianoObstacle nearest = null;
        double distance = Double.MAX_VALUE;
        for (PianoObstacle obstacle : session.getPianoObstacles()) {
            double current = obstacle.getX() - source.getCol();
            if (obstacle.isAlive() && obstacle.getRow() == source.getRow()
                    && current >= 0 && current <= range && current < distance) {
                nearest = obstacle;
                distance = current;
            }
        }
        return nearest;
    }

    private int countGraves() {
        int result = 0;
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColCount(); col++) {
                var tile = session.getBoard().getTile(col, row);
                if (tile != null && tile.isGrave()) {
                    result++;
                }
            }
        }
        return result;
    }

    private void damageZombieByExplosion(Zombie zombie, int damage) {
        if (zombie == null || zombie.isDead()) {
            return;
        }
        zombie.markPowderDeath();
        zombie.takeDamage(damage);
        if (zombie.isDead()) {
            onZombieKilled(zombie);
        }
    }
}
