package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.model.minigame.izombie.IZombieHandler;
import io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler;
import io.github.finalwave.model.minigame.mode.NetworkedIZombieMode;
import io.github.finalwave.network.match.MatchSnapshotApplier;
import io.github.finalwave.network.match.MatchSnapshotBuilder;
import io.github.finalwave.view.gui.render.sync.ZombieVisualState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkedIZombieModeTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry zombieRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
    }

    @Test
    void hostStartsEmptyThenAcceptsPicksAndPlants() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(stage, plantRegistry, zombieRegistry, new Random(7L));

        GameSession host = mode.createHostSession();
        host.start();
        assertTrue(host.getZombies().isEmpty());
        assertEquals(IZombieDuelCatalog.PLACEMENT_COLUMN, host.getIZombiePlacementColumn());

        mode.applyPicks(host, IZombieDuelCatalog.DEFAULT_PLANTS, IZombieDuelCatalog.DEFAULT_ZOMBIES);
        assertTrue(host.getActiveMiniGameHandler() instanceof NetworkedIZombieHandler handler && handler.isPlaying());
        assertEquals(IZombieDuelCatalog.ZOMBIE_START_SUN, host.getIZombieSunBalance());
        assertEquals(IZombieDuelCatalog.DEFAULT_ZOMBIES.size(), host.getIZombieZombiePool().size());
        assertEquals(host.getBoard().getRows(), stationaryProducerCount(host));

        PlantPlacementResult planted = host.tryPlant("Peashooter", 2, 2, 1);
        assertEquals(PlantPlacementResult.SUCCESS, planted);

        var payload = MatchSnapshotBuilder.build(host, "match-test");
        assertEquals(IZombieDuelCatalog.PHASE_PLAYING, payload.getPhase());
        GameSession guest = mode.createGuestSession();
        guest.start();
        MatchSnapshotApplier.apply(guest, payload);

        assertEquals(host.getBoard().getAllPlants().size(), guest.getBoard().getAllPlants().size());
        assertEquals(MatchResult.IN_PROGRESS, guest.getMatchResult());
        assertEquals(host.getIZombieZombiePool(), guest.getIZombieZombiePool());
        assertEquals(host.getBoard().getRows(), stationaryProducerCount(guest));
    }

    @Test
    void networkedHandlerProducesSunAndPlantWinOnTimer() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(11L));
        GameSession host = mode.createHostSession();
        host.start();
        mode.applyPicks(host, IZombieDuelCatalog.DEFAULT_PLANTS, IZombieDuelCatalog.DEFAULT_ZOMBIES);
        NetworkedIZombieHandler handler = (NetworkedIZombieHandler) host.getActiveMiniGameHandler();
        assertTrue(handler.isPlaying());
        assertEquals(host.getBoard().getRows(), stationaryProducerCount(host));
        assertEquals(host.getBoard().getRows(), handler.getSunProducerSystem().getProducerCount());

        int before = host.getIZombieSunBalance();
        host.advanceTicks(GameSession.TICKS_PER_SECOND);
        assertTrue(host.getIZombieSunBalance() > before);

        host.advanceTicks(IZombieDuelCatalog.ROUND_SECONDS * GameSession.TICKS_PER_SECOND);
        assertEquals(MatchResult.WON, host.getMatchResult());
    }

    @Test
    void sanitizeAcceptsRegistryPlantsAndZombiesOutsideOldPools() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(5L));
        GameSession host = mode.createHostSession();
        host.start();

        List<String> plants = List.of("Kernel-pult", "Peashooter", "Wall-nut", "Sunflower",
                "Potato Mine", "Chomper", "Snow Pea", "Repeater");
        List<String> zombies = List.of("ZombieDefault", "ZombieGargantuar", "ZombieImp",
                "ZombieArmor1", "ZombieArmor2");
        assertTrue(plantRegistry.getDefinition("Kernel-pult") != null
                || plants.stream().allMatch(name -> plantRegistry.getDefinition(name) != null));

        String extraPlant = plantRegistry.getAllDefinitions().stream()
                .map(def -> def.getName())
                .filter(name -> !IZombieDuelCatalog.PLANT_POOL.contains(name))
                .findFirst()
                .orElse("Peashooter");
        String extraZombie = zombieRegistry.getAllDefinitions().stream()
                .map(def -> def.getAlias())
                .filter(alias -> !IZombieDuelCatalog.ZOMBIE_POOL.contains(alias))
                .findFirst()
                .orElse("ZombieDefault");

        mode.applyPicks(host,
                List.of(extraPlant, "Peashooter", "Wall-nut", "Sunflower",
                        "Potato Mine", "Chomper", "Snow Pea", "Repeater"),
                List.of("ZombieDefault", extraZombie, "ZombieImp", "ZombieArmor1", "ZombieArmor2"));

        assertTrue(host.getSelectedLoadout().contains(extraPlant));
        assertTrue(host.getIZombieZombiePool().contains(extraZombie));
    }

    @Test
    void zombiePlacementRespectsRedLine() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(3L));
        GameSession host = mode.createHostSession();
        host.start();
        mode.applyPicks(host, IZombieDuelCatalog.DEFAULT_PLANTS, IZombieDuelCatalog.DEFAULT_ZOMBIES);

        assertEquals(PlantPlacementResult.BEYOND_PLANTING_LINE,
                host.tryPlaceZombie("ZombieDefault", IZombieDuelCatalog.PLACEMENT_COLUMN, 1));
        assertEquals(PlantPlacementResult.SUCCESS,
                host.tryPlaceZombie("ZombieDefault", IZombieDuelCatalog.FIRST_ZOMBIE_COLUMN, 1));
        assertTrue(host.getZombies().stream().anyMatch(zombie -> !zombie.isStationary()));
    }

    @Test
    void guestSnapshotZombiesLeaveSpawningSoTheyDraw() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(19L));
        GameSession host = mode.createHostSession();
        host.start();
        mode.applyPicks(host, IZombieDuelCatalog.DEFAULT_PLANTS, IZombieDuelCatalog.DEFAULT_ZOMBIES);
        assertEquals(PlantPlacementResult.SUCCESS,
                host.tryPlaceZombie("ZombieDefault", IZombieDuelCatalog.FIRST_ZOMBIE_COLUMN, 2));

        var payload = MatchSnapshotBuilder.build(host, "match-guest-zombies");
        assertFalse(payload.getZombies() == null || payload.getZombies().isEmpty());

        GameSession guest = mode.createGuestSession();
        guest.start();
        MatchSnapshotApplier.apply(guest, payload);

        assertFalse(guest.getZombies().isEmpty());
        for (Zombie zombie : guest.getZombies()) {
            assertNotEquals(ZombieState.SPAWNING, zombie.getState());
            assertTrue(ZombieVisualState.shouldDraw(zombie));
        }
        assertEquals(host.getBoard().getRows(), stationaryProducerCount(guest));
    }

    @Test
    void projectileEffectFromStringParsesEnumNames() {
        assertEquals(ProjectileEffect.GOO, ProjectileEffect.fromString("GOO"));
        assertEquals(ProjectileEffect.GOO_PF, ProjectileEffect.fromString("GOO_PF"));
        assertEquals(ProjectileEffect.ICE, ProjectileEffect.fromString("ice"));
    }

    @Test
    void guestSnapshotRestoresGooProjectileEffectAndVisualClip() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(31L));
        GameSession host = mode.createHostSession();
        host.start();
        List<String> plants = List.of(
                "Goo Peashooter", "Peashooter", "Wall-nut", "Sunflower",
                "Potato Mine", "Chomper", "Snow Pea", "Repeater");
        mode.applyPicks(host, plants, IZombieDuelCatalog.DEFAULT_ZOMBIES);

        assertEquals(PlantPlacementResult.SUCCESS, host.tryPlant("Goo Peashooter", 2, 2, 1));
        assertEquals(PlantPlacementResult.SUCCESS,
                host.tryPlaceZombie("ZombieDefault", IZombieDuelCatalog.FIRST_ZOMBIE_COLUMN, 2));

        io.github.finalwave.network.match.MatchStatePayload payload = null;
        for (int i = 0; i < GameSession.TICKS_PER_SECOND * 12; i++) {
            host.advanceTicks(1);
            Projectile hostProjectile = host.getProjectileSystem().getProjectiles().stream()
                    .filter(projectile -> projectile.getEffect() == ProjectileEffect.GOO)
                    .findFirst()
                    .orElse(null);
            if (hostProjectile == null) {
                continue;
            }
            payload = MatchSnapshotBuilder.build(host, "match-goo-sync");
            break;
        }
        assertTrue(payload != null, "host should fire goo projectiles");
        assertFalse(payload.getProjectiles().isEmpty());
        assertEquals("GOO", payload.getProjectiles().getFirst().getType());
        assertEquals("projectile_t1", payload.getProjectiles().getFirst().getVisualClip());

        GameSession guest = mode.createGuestSession();
        guest.start();
        MatchSnapshotApplier.apply(guest, payload);

        Projectile guestProjectile = guest.getProjectileSystem().getProjectiles().getFirst();
        assertEquals(ProjectileEffect.GOO, guestProjectile.getEffect());
        assertEquals("projectile_t1", guestProjectile.getVisualClip());
    }

    @Test
    void guestSnapshotReceivesProjectilesStateArmorAndChill() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(23L));
        GameSession host = mode.createHostSession();
        host.start();
        mode.applyPicks(host, IZombieDuelCatalog.DEFAULT_PLANTS, IZombieDuelCatalog.DEFAULT_ZOMBIES);

        assertEquals(PlantPlacementResult.SUCCESS, host.tryPlant("Snow Pea", 2, 2, 1));
        assertEquals(PlantPlacementResult.SUCCESS,
                host.tryPlaceZombie("ZombieDefault", IZombieDuelCatalog.FIRST_ZOMBIE_COLUMN, 2));

        io.github.finalwave.network.match.MatchStatePayload payload = null;
        for (int i = 0; i < GameSession.TICKS_PER_SECOND * 12; i++) {
            host.advanceTicks(1);
            if (host.getProjectileSystem().getProjectiles().isEmpty()) {
                continue;
            }
            Zombie hostZombie = firstCombatZombie(host);
            assertTrue(hostZombie != null);
            hostZombie.setState(ZombieState.EATING);
            if (hostZombie.getChillTicksRemaining() <= 0 && hostZombie.getFreezeTicksRemaining() <= 0) {
                hostZombie.applyChill(40);
            }
            payload = MatchSnapshotBuilder.build(host, "match-visual-sync");
            break;
        }
        assertTrue(payload != null, "host should fire projectiles");
        assertFalse(payload.getProjectiles().isEmpty());

        var combatDto = payload.getZombies().stream()
                .filter(dto -> !dto.isStationary())
                .findFirst()
                .orElseThrow();
        assertEquals(ZombieState.EATING.name(), combatDto.getState());
        assertTrue(combatDto.getChillTicks() > 0 || combatDto.getFreezeTicks() > 0);

        GameSession guest = mode.createGuestSession();
        guest.start();
        MatchSnapshotApplier.apply(guest, payload);

        assertFalse(guest.getProjectileSystem().getProjectiles().isEmpty());
        assertEquals(payload.getProjectiles().size(), guest.getProjectileSystem().getProjectiles().size());
        assertEquals(payload.getProjectiles().getFirst().getId(),
                guest.getProjectileSystem().getProjectiles().getFirst().getId());

        Zombie guestZombie = firstCombatZombie(guest);
        assertTrue(guestZombie != null);
        assertEquals(ZombieState.EATING, guestZombie.getState());
        assertEquals(combatDto.getChillTicks(), guestZombie.getChillTicksRemaining());
        assertEquals(combatDto.getFreezeTicks(), guestZombie.getFreezeTicksRemaining());
        assertEquals(combatDto.getArmorLayers().size(), guestZombie.getArmorLayers().size());
        for (int i = 0; i < combatDto.getArmorLayers().size(); i++) {
            assertEquals(combatDto.getArmorLayers().get(i).getHealth(),
                    guestZombie.getArmorLayers().get(i).getHealth());
        }
    }

    private static int stationaryProducerCount(GameSession session) {
        int count = 0;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isStationary() && IZombieHandler.SUN_PRODUCER_ALIAS.equals(zombie.getType())) {
                count++;
            }
        }
        return count;
    }

    private static Zombie firstCombatZombie(GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isStationary()) {
                return zombie;
            }
        }
        return null;
    }
}
