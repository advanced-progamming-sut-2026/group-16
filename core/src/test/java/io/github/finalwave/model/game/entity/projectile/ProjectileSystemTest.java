package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantFactory;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.definition.PlantRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileSystemTest {

    private PlantRegistry registry;
    private PlantFactory plantFactory;
    private ProjectileSystem system;
    private GameBoard board;

    @BeforeEach
    void setUp() throws IOException {
        registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        plantFactory = new PlantFactory();
        system = new ProjectileSystem();
        board = new GameBoard();
    }

    @Test
    void piercingProjectileHitsMultipleZombies() {
        var def = registry.getDefinition("Peashooter");
        Plant plant = plantFactory.createBaseLevel(def, 1, 2);
        system.spawnFromPlant(plant, 100, 1, ProjectileProfile.piercingProfile());

        List<Zombie> zombies = new ArrayList<>();
        zombies.add(new Zombie.Builder("z1").maxHealth(50).position(3, 2).build());
        zombies.add(new Zombie.Builder("z2").maxHealth(50).position(4, 2).build());

        for (int i = 0; i < 20; i++) {
            system.tick(board, zombies, z -> {});
        }
        assertTrue(zombies.stream().allMatch(Zombie::isDead));
    }

    @Test
    void poisonBypassesArmor() {
        var def = registry.getDefinition("Goo Peashooter");
        Plant plant = plantFactory.createBaseLevel(def, 1, 2);
        system.spawnFromPlant(plant, 30, 1, ProjectileProfile.straight());

        Armor cone = new Armor("cone", "CONE", 100, false, false);
        Zombie armored = new Zombie.Builder("z")
                .maxHealth(200)
                .armor(cone)
                .position(2, 2)
                .build();
        List<Zombie> zombies = List.of(armored);

        for (int i = 0; i < 10; i++) {
            system.tick(board, zombies, z -> {});
        }
        assertTrue(armored.getHealth() < 200);
        assertEquals(100, cone.getHealth());
    }

    @Test
    void lobArcIgnoresGraveObstacle() {
        var def = registry.getDefinition("Cabbage-pult");
        assertNotNull(def);
        Plant plant = plantFactory.createBaseLevel(def, 2, 1);
        system.spawnFromPlant(plant, 50, 1, ProjectileProfile.arcing());
        board.setTile(3, 1, new GraveTile());
        List<Zombie> zombies = new ArrayList<>();
        zombies.add(new Zombie.Builder("z").maxHealth(100).position(5, 1).build());
        for (int i = 0; i < 30; i++) {
            system.tick(board, zombies, z -> {});
        }
        assertTrue(zombies.getFirst().getHealth() < 100 || zombies.getFirst().isDead());
    }

    @Test
    void homingProjectileChangesRowsTowardTarget() {
        var def = registry.getDefinition("Cat-tail");
        Plant plant = plantFactory.createBaseLevel(def, 1, 0);
        system.spawnFromPlant(plant, 15, 1, ProjectileProfile.homingProfile());
        List<Zombie> zombies = List.of(
                new Zombie.Builder("z").maxHealth(100).position(5, 3).build());

        system.tick(board, zombies, z -> {});

        assertEquals(3, system.getProjectiles().getFirst().getRow());
    }

    @Test
    void zombieProjectileDamagesPlant() {
        Plant plant = plantFactory.createBaseLevel(
                registry.getDefinition("Peashooter"), 2, 1);
        board.placePlant(plant);
        int healthBefore = plant.getHealth();
        system.spawn(Projectile.fromZombie(1, 3.0, 25, "snowball"));

        for (int i = 0; i < 5; i++) {
            system.tick(board, List.of(), z -> {});
        }

        assertEquals(healthBefore - 25, plant.getHealth());
    }

    @Test
    void piercingProjectileHitsEachZombieOnlyOnce() {
        Plant plant = plantFactory.createBaseLevel(
                registry.getDefinition("Peashooter"), 1, 2);
        system.spawnFromPlant(plant, 10, 1, ProjectileProfile.piercingProfile());
        Zombie first = new Zombie.Builder("first").maxHealth(100).position(3, 2).build();
        Zombie second = new Zombie.Builder("second").maxHealth(100).position(4, 2).build();

        for (int i = 0; i < 20; i++) {
            system.tick(board, List.of(first, second), z -> {});
        }

        assertEquals(90, first.getHealth());
        assertEquals(90, second.getHealth());
    }

    @Test
    void fireProjectileClearsColdStatus() {
        Plant firePlant = plantFactory.createBaseLevel(
                registry.getDefinition("Fire Peashooter"), 1, 2);
        Zombie zombie = new Zombie.Builder("cold").maxHealth(100).position(2, 2).build();
        zombie.applyFreeze(100);
        system.spawnFromPlant(firePlant, 10, 1, ProjectileProfile.straight());

        for (int i = 0; i < 5; i++) {
            system.tick(board, List.of(zombie), z -> {});
        }

        assertEquals(0, zombie.getFreezeTicksRemaining());
        assertEquals(zombie.getBaseSpeed(), zombie.getCurrentSpeed(), 0.0001);
    }

    @Test
    void straightProjectileIsBlockedByGrave() {
        Plant plant = plantFactory.createBaseLevel(
                registry.getDefinition("Peashooter"), 1, 1);
        board.setTile(3, 1, new GraveTile());
        Zombie zombie = new Zombie.Builder("behind-grave")
                .maxHealth(100).position(5, 1).build();
        system.spawnFromPlant(plant, 20, 1, ProjectileProfile.straight());

        for (int i = 0; i < 20; i++) {
            system.tick(board, List.of(zombie), z -> {});
        }

        assertEquals(100, zombie.getHealth());
        assertTrue(system.getProjectiles().isEmpty());
    }

    @Test
    void namedProjectileEffectsAreDistinct() {
        Plant pepper = plantFactory.createBaseLevel(
                registry.getDefinition("Pepper-pult"), 1, 2);
        Plant melon = plantFactory.createBaseLevel(
                registry.getDefinition("Melon-pult"), 1, 2);
        Plant winterMelon = plantFactory.createBaseLevel(
                registry.getDefinition("Winter Melon"), 1, 2);
        Plant cactus = plantFactory.createBaseLevel(
                registry.getDefinition("Cactus"), 1, 2);
        Plant puff = plantFactory.createBaseLevel(
                registry.getDefinition("Puff-shroom"), 1, 2);

        assertEquals(ProjectileEffect.PEPPER, pepper.projectileEffect());
        assertNotEquals(ProjectileEffect.FIRE, pepper.projectileEffect());
        assertEquals(ProjectileEffect.MELON, melon.projectileEffect());
        assertNotEquals(ProjectileEffect.GENERIC, melon.projectileEffect());
        assertEquals(ProjectileEffect.WINTER_MELON, winterMelon.projectileEffect());
        assertEquals(ProjectileEffect.SPIKE, cactus.projectileEffect());
        assertEquals(ProjectileEffect.PUFF, puff.projectileEffect());
    }

    @Test
    void puffPassesGraveWithoutDestroyingIt() {
        Plant puff = plantFactory.createBaseLevel(
                registry.getDefinition("Puff-shroom"), 1, 1);
        GraveTile grave = new GraveTile();
        board.setTile(3, 1, grave);
        Zombie zombie = new Zombie.Builder("behind-grave")
                .maxHealth(100).position(5, 1).build();
        system.spawnFromPlant(puff, 20, 1, ProjectileProfile.straight());

        for (int i = 0; i < 20; i++) {
            system.tick(board, List.of(zombie), z -> {});
        }

        assertTrue(board.getTile(3, 1).isGrave());
        assertEquals(GraveTile.MAX_HEALTH, grave.getHealth());
        assertTrue(zombie.getHealth() < 100);
    }

    @Test
    void piercingProjectileDoesNotDestroyGrave() {
        Plant cactus = plantFactory.createBaseLevel(
                registry.getDefinition("Cactus"), 1, 1);
        GraveTile grave = new GraveTile();
        board.setTile(3, 1, grave);
        Zombie zombie = new Zombie.Builder("behind-grave")
                .maxHealth(100).position(5, 1).build();
        system.spawnFromPlant(cactus, 30, 1, ProjectileProfile.piercingProfile());

        for (int i = 0; i < 20; i++) {
            system.tick(board, List.of(zombie), z -> {});
        }

        assertTrue(board.getTile(3, 1).isGrave());
        assertEquals(GraveTile.MAX_HEALTH, grave.getHealth());
        assertTrue(zombie.getHealth() < 100);
    }

    @Test
    void melonSplashDamagesNeighbor() {
        Plant melon = plantFactory.createBaseLevel(
                registry.getDefinition("Melon-pult"), 1, 2);
        Zombie primary = new Zombie.Builder("primary")
                .maxHealth(500).position(2.0, 2).build();
        Zombie neighbor = new Zombie.Builder("neighbor")
                .maxHealth(500).position(2.0, 3).build();
        system.spawnFromPlant(melon, 80, 1, ProjectileProfile.arcing());

        for (int i = 0; i < 10; i++) {
            system.tick(board, List.of(primary, neighbor), z -> {});
        }

        assertTrue(primary.getHealth() < 500);
        assertTrue(neighbor.getHealth() < 500);
        assertEquals(primary.getHealth(), neighbor.getHealth());
    }

    @Test
    void pepperSplashDamagesNeighborWithoutFireMultiplier() {
        Plant pepper = plantFactory.createBaseLevel(
                registry.getDefinition("Pepper-pult"), 1, 2);
        Zombie primary = new Zombie.Builder("primary")
                .maxHealth(500).position(2.0, 2).build();
        Zombie neighbor = new Zombie.Builder("neighbor")
                .maxHealth(500).position(2.0, 3).build();
        system.spawnFromPlant(pepper, 50, 1, ProjectileProfile.arcing());

        for (int i = 0; i < 10; i++) {
            system.tick(board, List.of(primary, neighbor), z -> {});
        }

        assertEquals(450, primary.getHealth());
        assertEquals(450, neighbor.getHealth());
    }

    @Test
    void butterProjectileImmobilizesZombie() {
        Plant plant = plantFactory.createBaseLevel(
                registry.getDefinition("Kernel-pult"), 1, 2);
        Zombie zombie = new Zombie.Builder("buttered")
                .maxHealth(100).position(2, 2).build();
        system.spawn(new Projectile(2, 1.5, 10, ProjectileProfile.straight(),
                ProjectileEffect.BUTTER, plant, 0));

        system.tick(board, List.of(zombie), z -> {});

        assertTrue(zombie.getFreezeTicksRemaining() > 0);
        assertEquals(0.0, zombie.getCurrentSpeed(), 0.0001);
    }

    @Test
    void peaHitsBossOnOccupiedRowBeforeCenter() {
        Plant plant = plantFactory.createBaseLevel(
                registry.getDefinition("Peashooter"), 1, 1);
        Zombie boss = new Zombie.Builder("ZombieEgyptZomboss")
                .maxHealth(3600)
                .position(8, 1)
                .build();
        boss.configureAsBoss(2);
        int health = boss.getHealth();
        system.spawn(new Projectile(2, 6.8, 20, ProjectileProfile.straight(),
                ProjectileEffect.PEA, plant, 0));

        system.tick(board, List.of(boss), z -> {});

        assertTrue(boss.getHealth() < health);
        assertTrue(system.getProjectiles().isEmpty());
    }
}
