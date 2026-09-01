package io.github.finalwave.model.game;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantStatsCalculator;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.plant.support.ImitaterMorphSupport;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.PlantFoodDrop;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.GroundSeedPacket;
import io.github.finalwave.model.quest.event.GameEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


final class GameSessionPlanting {

    private final GameSession session;
    private final List<Sun> sunItems = new ArrayList<>();
    private final List<PlantFoodDrop> plantFoodDrops = new ArrayList<>();
    private final List<Vase> vases = new ArrayList<>();
    private final List<GroundSeedPacket> groundSeedPackets = new ArrayList<>();
    private int seedPacketExpiryTicks = 100;

    GameSessionPlanting(GameSession session) {
        this.session = session;
    }

    List<Sun> getSunItems() {
        return List.copyOf(sunItems);
    }

    List<PlantFoodDrop> getPlantFoodDrops() {
        return List.copyOf(plantFoodDrops);
    }

    List<Vase> getVases() {
        return List.copyOf(vases);
    }

    List<GroundSeedPacket> getGroundSeedPackets() {
        return List.copyOf(groundSeedPackets);
    }

    void setSeedPacketExpiryTicks(int seedPacketExpiryTicks) {
        this.seedPacketExpiryTicks = Math.max(1, seedPacketExpiryTicks);
    }

    int getSeedPacketExpiryTicks() {
        return seedPacketExpiryTicks;
    }

    void addVase(Vase vase) {
        if (vase != null && vase.isAlive()) {
            vases.add(vase);
        }
    }

    Vase getVaseAt(int col, int row) {
        for (Vase vase : vases) {
            if (vase.isAlive()
                    && (int) Math.floor(vase.getX()) == col
                    && (int) Math.floor(vase.getY()) == row) {
                return vase;
            }
        }
        return null;
    }

    boolean smashVase(int col, int row) {
        Vase vase = getVaseAt(col, row);
        if (vase == null) {
            return false;
        }
        Vase.Content content = vase.getContent();
        vase.smash(session.getContext());
        vases.remove(vase);
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onVaseSmashed(col, row, content);
        }
        if (session.getActiveMiniGameHandler() != null) {
            session.getActiveMiniGameHandler().onTick(session);
        }
        return true;
    }

    boolean areAllVasesSmashed() {
        return vases.isEmpty() || vases.stream().noneMatch(Vase::isAlive);
    }

    void addGroundSeedPacket(String plantName, int col, int row) {
        if (plantName == null || plantName.isBlank() || !session.getBoard().inBounds(col, row)) {
            return;
        }
        groundSeedPackets.removeIf(packet -> packet.col() == col && packet.row() == row);
        GroundSeedPacket packet = new GroundSeedPacket(
                plantName, col, row, session.getCurrentTick() + seedPacketExpiryTicks);
        groundSeedPackets.add(packet);
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onSeedPacketDropped(plantName, col, row);
        }
    }

    GroundSeedPacket getGroundSeedPacketAt(int col, int row) {
        for (GroundSeedPacket packet : groundSeedPackets) {
            if (packet.col() == col && packet.row() == row) {
                return packet;
            }
        }
        return null;
    }

    GroundSeedPacket getGroundSeedPacketByName(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return null;
        }
        for (GroundSeedPacket packet : groundSeedPackets) {
            if (plantName.equals(packet.plantName())) {
                return packet;
            }
        }
        return null;
    }

    PlantPlacementResult plantFromSeedPacket(int col, int row) {
        GroundSeedPacket packet = getGroundSeedPacketAt(col, row);
        if (packet == null) {
            return PlantPlacementResult.NO_SEED_PACKET;
        }
        return placeFromSeedPacket(packet, col, row);
    }

    PlantPlacementResult plantFromSeedPacket(String plantName, int col, int row) {
        GroundSeedPacket packet = getGroundSeedPacketByName(plantName);
        if (packet == null) {
            return PlantPlacementResult.NO_SEED_PACKET;
        }
        return placeFromSeedPacket(packet, col, row);
    }

    private PlantPlacementResult placeFromSeedPacket(GroundSeedPacket packet, int col, int row) {
        PlantDefinition definition = session.getPlantRegistry().getDefinition(packet.plantName());
        if (definition == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (getVaseAt(col, row) != null) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        PlantPlacementResult stacked = stackPeaPodIfPresent(definition, col, row);
        if (stacked != null) {
            if (stacked == PlantPlacementResult.SUCCESS) {
                groundSeedPackets.remove(packet);
                MatchListener matchListener = session.getMatchListener();
                if (matchListener != null) {
                    matchListener.onSeedPacketPlanted(packet.plantName(), col, row);
                }
            }
            return stacked;
        }
        PlantPlacementResult placement = session.getBoard().canPlace(definition, col, row);
        if (placement != PlantPlacementResult.SUCCESS) {
            return placement;
        }
        Plant plant = session.getPlantFactory().create(definition, 1, col, row);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        ImitaterMorphSupport.onPlanted(plant, session);
        groundSeedPackets.remove(packet);
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onSeedPacketPlanted(packet.plantName(), col, row);
        }
        session.getEventBus().publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getCategory().name(),
                col,
                row,
                plant.hasTag(PlantTag.NIGHT) || plant.hasTag(PlantTag.SHROOM)));
        return PlantPlacementResult.SUCCESS;
    }

    PlantPlacementResult tryPlant(String plantName, int col, int row, int level) {
        PlantDefinition definition = session.getPlantRegistry().getDefinition(plantName);
        if (definition == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (level < 1 || level > definition.getMaxLevel()) {
            return PlantPlacementResult.INVALID_LEVEL;
        }
        GameSessionSpecialLevelState special = session.getSpecialLevelState();
        if (special.isConveyorBeltActive()) {
            if (!special.hasConveyorBeltPlant(plantName)) {
                return PlantPlacementResult.NOT_ON_CONVEYOR_BELT;
            }
        } else if (!special.getLevelLockedPlants().isEmpty()
                && special.isLevelLockedPlant(plantName)) {
            return PlantPlacementResult.LEVEL_PLANT_LOCKED;
        } else if (!session.getSelectedLoadout().isEmpty()
                && !session.getSelectedLoadout().contains(plantName)) {
            return PlantPlacementResult.NOT_IN_LOADOUT;
        }
        int cost = PlantStatsCalculator.compute(definition, level).cost();
        boolean conveyor = special.isConveyorBeltActive();
        boolean sandbox = session.isSandboxPractice();
        if (!sandbox && !conveyor && !special.isPrepPhaseActive()
                && !session.getCooldownTracker().isReady(plantName)) {
            return PlantPlacementResult.ON_COOLDOWN;
        }
        if (!sandbox && !conveyor && session.getSunBalance() < cost) {
            return PlantPlacementResult.INSUFFICIENT_SUN;
        }
        PlantPlacementResult stacked = stackPeaPodIfPresent(definition, col, row);
        if (stacked != null) {
            if (stacked == PlantPlacementResult.SUCCESS) {
                if (!sandbox && !conveyor) {
                    session.withdrawSun(cost);
                    session.getEventBus().publish(new GameEvent.SunSpent(cost));
                }
                if (!sandbox && !conveyor && !special.isPrepPhaseActive()) {
                    session.getCooldownTracker().startCooldown(
                            plantName,
                            PlantStatsCalculator.compute(definition, level).recharge(),
                            GameSession.TICKS_PER_SECOND);
                }
                if (conveyor) {
                    special.removeConveyorBeltPlant(plantName);
                }
            }
            return stacked;
        }
        PlantPlacementResult placement = session.getBoard().canPlace(definition, col, row);
        if (placement != PlantPlacementResult.SUCCESS) {
            return placement;
        }
        Plant existing = session.getBoard().getGroundPlantAt(col, row);
        if (Plant.isPeaPod(plantName) && existing != null && Plant.isPeaPod(existing.getName())) {
            if (!existing.addStack()) {
                return PlantPlacementResult.GROUND_OCCUPIED;
            }
            if (!sandbox && !conveyor) {
                session.withdrawSun(cost);
            }
            if (!sandbox && !conveyor && !special.isPrepPhaseActive()) {
                session.getCooldownTracker().startCooldown(
                        plantName, existing.getStats().recharge(), GameSession.TICKS_PER_SECOND);
            }
            if (!sandbox && !conveyor) {
                session.getEventBus().publish(new GameEvent.SunSpent(cost));
            }
            if (conveyor) {
                special.removeConveyorBeltPlant(plantName);
            }
            return PlantPlacementResult.SUCCESS;
        }
        Plant plant = session.getPlantFactory().create(definition, level, col, row);
        if (!sandbox && !conveyor) {
            session.withdrawSun(cost);
        }
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        ImitaterMorphSupport.onPlanted(plant, session);
        if (!sandbox && !conveyor && !special.isPrepPhaseActive()) {
            session.getCooldownTracker().startCooldown(
                    plantName, plant.getStats().recharge(), GameSession.TICKS_PER_SECOND);
        }
        session.getEventBus().publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getCategory().name(),
                col,
                row,
                plant.hasTag(PlantTag.NIGHT) || plant.hasTag(PlantTag.SHROOM)));
        if (!sandbox && !conveyor) {
            session.getEventBus().publish(new GameEvent.SunSpent(cost));
        }
        if (conveyor) {
            special.removeConveyorBeltPlant(plantName);
        }
        if (!"Imitater".equals(plantName)) {
            session.noteImitaterTargetSeed(plantName);
        }
        return PlantPlacementResult.SUCCESS;
    }

    private PlantPlacementResult stackPeaPodIfPresent(PlantDefinition definition, int col, int row) {
        if (definition == null || !Plant.PEA_POD.equals(definition.getName())) {
            return null;
        }
        Plant existing = session.getBoard().findPeaPod(col, row);
        if (existing == null) {
            return null;
        }
        if (!existing.tryAddStack()) {
            return PlantPlacementResult.GROUND_OCCUPIED;
        }
        session.getEventBus().publish(new GameEvent.PlantPlanted(
                existing.getName(),
                existing.getCategory().name(),
                col,
                row,
                existing.hasTag(PlantTag.NIGHT) || existing.hasTag(PlantTag.SHROOM)));
        return PlantPlacementResult.SUCCESS;
    }

    Plant placeDefensePlant(String plantName, int col, int row) {
        PlantDefinition definition = session.getPlantRegistry().getDefinition(plantName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown plant: " + plantName);
        }
        Plant plant = session.getPlantFactory().create(definition, 1, col, row);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        ImitaterMorphSupport.onPlanted(plant, session);
        return plant;
    }

    Plant placeProtectedSeed(String plantName, int col, int row) {
        PlantDefinition definition = session.getPlantRegistry().getDefinition(plantName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown plant: " + plantName);
        }
        Plant plant = session.getPlantFactory().create(definition, 1, col, row);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        ImitaterMorphSupport.onPlanted(plant, session);
        session.getSpecialLevelState().registerProtectedSeed(plant.getId(), plantName, col, row);
        return plant;
    }

    boolean collectSun(Sun sun) {
        if (sun == null || !sunItems.contains(sun) || sun.isAttracted() || sun.isExpired()) {
            return false;
        }
        if (sun.getType() == SunType.RADIOACTIVE && sun.isFalling()) {
            sunItems.remove(sun);
            session.getCombat().explodeRadioactiveSun(sun.getCol(), sun.getRow());
            return true;
        }
        if (!sunItems.remove(sun)) {
            return false;
        }
        session.addSunBalance(sun.getValue());
        session.getEventBus().publish(new GameEvent.SunCollected(sun.getValue()));
        return true;
    }

    boolean collectSunAt(int col, int row) {
        Sun target = null;
        for (Sun sun : sunItems) {
            if (sun.getCol() == col && sun.getRow() == row) {
                target = sun;
                break;
            }
        }
        return collectSun(target);
    }

    void spawnPlantFoodDrop(int col, int row, double worldX) {
        if (session.getPlantFoodCount() >= GameSession.MAX_PLANT_FOOD) {
            return;
        }
        int clampedCol = Math.max(0, Math.min(session.getBoard().getCols() - 1, col));
        int clampedRow = Math.max(0, Math.min(session.getBoard().getRows() - 1, row));
        plantFoodDrops.add(new PlantFoodDrop(clampedCol, clampedRow, worldX));
    }

    boolean collectPlantFood(PlantFoodDrop drop) {
        if (drop == null || !plantFoodDrops.contains(drop) || drop.isConsumed()) {
            return false;
        }
        if (session.getPlantFoodCount() >= GameSession.MAX_PLANT_FOOD) {
            return false;
        }
        if (!plantFoodDrops.remove(drop)) {
            return false;
        }
        drop.consume();
        session.addPlantFood(1);
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onGlowingZombieDroppedFood(session.getPlantFoodCount());
        }
        return true;
    }

    boolean collectPlantFoodAt(int col, int row) {
        PlantFoodDrop target = null;
        for (PlantFoodDrop drop : plantFoodDrops) {
            if (drop.getCol() == col && drop.getRow() == row && !drop.isConsumed()) {
                target = drop;
                break;
            }
        }
        return collectPlantFood(target);
    }

    void spawnSunItem(Sun sun) {
        if (sun == null) {
            return;
        }
        sunItems.add(sun);
        session.getSpecialLevelState().addTimedWarSunProgress(sun.getValue());
    }

    void spawnSkySun(int col, int row, int value) {
        if (session.getBoard().inBounds(col, row) && value > 0) {
            sunItems.add(new Sun(col, row, value, SunType.NORMAL, false));
        }
    }

    int stealGroundSun(Zombie thief, int maximum) {
        int remaining = Math.max(0, maximum);
        int stolen = 0;
        while (remaining > 0) {
            Sun sun = nearestStealableSun(thief);
            if (sun == null) {
                break;
            }
            int value = sun.takeValue(remaining);
            stolen += value;
            remaining -= value;
            if (thief != null && value > 0) {
                sun.attractTo(thief.getId());
            } else if (sun.getValue() == 0) {
                sunItems.remove(sun);
            }
        }
        return stolen;
    }

    private Sun nearestStealableSun(Zombie thief) {
        Sun best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Sun sun : sunItems) {
            if (sun == null || sun.isExpired() || sun.isAttracted() || sun.getValue() <= 0) {
                continue;
            }
            if (thief == null) {
                return sun;
            }
            double dx = thief.getX() - sun.getCol();
            double dy = thief.getRow() - sun.getRow();
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                best = sun;
                bestDistance = distance;
            }
        }
        return best;
    }

    void tickSunItems() {
        Iterator<Sun> iterator = sunItems.iterator();
        MatchListener matchListener = session.getMatchListener();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            String thiefId = sun.attractZombieId();
            boolean justLanded = sun.tick();
            if (justLanded && matchListener != null) {
                matchListener.onSunReachedGround(sun.getCol(), sun.getRow());
            }
            if (sun.isExpired()) {
                if (thiefId != null) {
                    concealRaStaffSun(thiefId);
                }
                iterator.remove();
            }
        }
    }

    private void concealRaStaffSun(String zombieId) {
        if (zombieId == null) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombieId.equals(zombie.getId())) {
                zombie.concealStaffSun();
                return;
            }
        }
    }

    void tickSkySun() {
        Sun sun = session.getSkySunSystem().tick(
                session.getCurrentTick(),
                GameSession.TICKS_PER_SECOND,
                session.getBoard().getCols(),
                session.getBoard().getRows());
        if (sun != null) {
            sunItems.add(sun);
            MatchListener matchListener = session.getMatchListener();
            if (matchListener != null) {
                matchListener.onSunDropped(sun.getType(), sun.getCol(), sun.getRow());
            }
        }
    }

    void tickGroundSeedPackets() {
        Iterator<GroundSeedPacket> iterator = groundSeedPackets.iterator();
        MatchListener matchListener = session.getMatchListener();
        while (iterator.hasNext()) {
            GroundSeedPacket packet = iterator.next();
            if (packet.expiresAtTick() <= session.getCurrentTick()) {
                iterator.remove();
                if (matchListener != null) {
                    matchListener.onSeedPacketExpired(packet.plantName(), packet.col(), packet.row());
                }
            }
        }
    }

    boolean usePlantFood(int col, int row) {
        if (session.getPlantFoodCount() <= 0) {
            return false;
        }
        Plant plant = session.getBoard().getPlantAt(col, row);
        if (plant == null || !plant.isAlive()) {
            return false;
        }
        session.consumePlantFood();
        plant.activatePlantFoodEffect(session.getContext());
        return true;
    }

    boolean removePlantFromBoard(Plant plant, boolean countsAsLoss) {
        if (plant == null || session.destroyedPlantIds().contains(plant.getId())) {
            return false;
        }
        boolean wasProtectedSeed = session.getSpecialLevelState().isProtectedSeedId(plant.getId());
        session.destroyedPlantIds().add(plant.getId());
        session.getBoard().removePlant(plant);
        if (countsAsLoss) {
            session.incrementPlantsLost();
            if (session.getActiveSpecialLevelHandler() != null) {
                session.getActiveSpecialLevelHandler().onPlantLost(session, plant);
            }
            if (session.getActiveMiniGameHandler() != null) {
                session.getActiveMiniGameHandler().onPlantLost(session, plant);
            }
            MatchListener matchListener = session.getMatchListener();
            if (matchListener != null) {
                matchListener.onPlantDestroyed(plant, plant.getCol(), plant.getRow());
            }
        }
        session.getEventBus().publish(new GameEvent.PlantDestroyed(
                plant.getName(),
                plant.getCategory().name()));
        if (countsAsLoss && wasProtectedSeed) {
            MatchListener matchListener = session.getMatchListener();
            if (matchListener != null) {
                matchListener.onProtectedSeedDestroyed(plant, plant.getCol(), plant.getRow());
            }
            session.loseMatch();
        }
        return true;
    }

    Plant createClone(Plant source, int col, int row) {
        Plant clone = session.getPlantFactory().create(
                source.getDefinition(), source.getLevel(), col, row);
        session.getBoard().placePlant(clone);
        clone.onPlanted(session.getContext());
        return clone;
    }

    void morphImitater(Plant imitater) {
        if (imitater == null || !"Imitater".equals(imitater.getName())) {
            return;
        }
        String targetName = imitater.getImitatedPlantName();
        if (targetName == null || targetName.isBlank()) {
            return;
        }
        var definition = session.getContext().findPlantDefinition(targetName);
        if (definition == null) {
            return;
        }
        int col = imitater.getCol();
        int row = imitater.getRow();
        int level = imitater.getLevel();
        session.getBoard().removePlant(imitater);
        Plant replacement = session.getPlantFactory().create(definition, level, col, row);
        session.getBoard().placePlant(replacement);
        replacement.initializeAfterImitaterMorph(session.getContext());
    }

    Plant createDoomShroomSeedling(Plant source, int col, int row) {
        Plant seedling = session.getPlantFactory().create(
                source.getDefinition(), source.getLevel(), col, row);
        session.getBoard().placePlant(seedling);
        seedling.onPlanted(session.getContext());
        return seedling;
    }

    Plant createPlantFoodClone(Plant source, int col, int row) {
        Plant clone = session.getPlantFactory().create(
                source.getDefinition(), source.getLevel(), col, row);
        session.getBoard().placePlant(clone);
        clone.onPlanted(session.getContext());
        clone.setPlantFoodSpawned(true);
        clone.setArmedTrap(true);
        clone.setChargeTicksRemaining(0);
        return clone;
    }
}
