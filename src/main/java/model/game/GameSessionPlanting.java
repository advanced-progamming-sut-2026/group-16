package model.game;

import model.definition.plant.PlantDefinition;
import model.game.board.PlantPlacementResult;
import model.game.entity.Vase;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantStatsCalculator;
import model.game.entity.plant.PlantTag;
import model.item.Sun;
import model.item.SunType;
import model.minigame.GroundSeedPacket;
import model.quest.event.GameEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


final class GameSessionPlanting {

    private final GameSession session;
    private final List<Sun> sunItems = new ArrayList<>();
    private final List<Vase> vases = new ArrayList<>();
    private final List<GroundSeedPacket> groundSeedPackets = new ArrayList<>();
    private int seedPacketExpiryTicks = 100;

    GameSessionPlanting(GameSession session) {
        this.session = session;
    }

    List<Sun> getSunItems() {
        return List.copyOf(sunItems);
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

    PlantPlacementResult plantFromSeedPacket(int col, int row) {
        GroundSeedPacket packet = getGroundSeedPacketAt(col, row);
        if (packet == null) {
            return PlantPlacementResult.NO_SEED_PACKET;
        }
        PlantDefinition definition = session.getPlantRegistry().getDefinition(packet.plantName());
        if (definition == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (getVaseAt(col, row) != null) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        PlantPlacementResult placement = session.getBoard().canPlace(definition, col, row);
        if (placement != PlantPlacementResult.SUCCESS) {
            return placement;
        }
        Plant plant = session.getPlantFactory().create(definition, 1, col, row);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
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
        if (!special.isPrepPhaseActive() && !session.getCooldownTracker().isReady(plantName)) {
            return PlantPlacementResult.ON_COOLDOWN;
        }
        if (session.getSunBalance() < cost) {
            return PlantPlacementResult.INSUFFICIENT_SUN;
        }
        PlantPlacementResult placement = session.getBoard().canPlace(definition, col, row);
        if (placement != PlantPlacementResult.SUCCESS) {
            return placement;
        }
        Plant plant = session.getPlantFactory().create(definition, level, col, row);
        session.withdrawSun(cost);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        if (!special.isPrepPhaseActive()) {
            session.getCooldownTracker().startCooldown(
                    plantName, plant.getStats().recharge(), GameSession.TICKS_PER_SECOND);
        }
        session.getEventBus().publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getCategory().name(),
                col,
                row,
                plant.hasTag(PlantTag.NIGHT) || plant.hasTag(PlantTag.SHROOM)));
        session.getEventBus().publish(new GameEvent.SunSpent(cost));
        if (special.isConveyorBeltActive()) {
            special.removeConveyorBeltPlant(plantName);
        }
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
        session.getSpecialLevelState().registerProtectedSeed(plant.getId(), plantName, col, row);
        return plant;
    }

    boolean collectSun(Sun sun) {
        if (sun == null || !sunItems.contains(sun)) {
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

    int stealGroundSun(int maximum) {
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

    void tickSunItems() {
        Iterator<Sun> iterator = sunItems.iterator();
        MatchListener matchListener = session.getMatchListener();
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
}
