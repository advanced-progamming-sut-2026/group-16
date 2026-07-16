package model.entity;

import model.definition.PlantRegistry;
import model.game.entity.GameContext;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantFactory;
import model.game.entity.projectile.Projectile;
import model.game.entity.projectile.ProjectileEffect;
import model.game.entity.projectile.ProjectileProfile;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.behavior.ContactAttackBehavior;
import model.game.entity.zombie.behavior.PlantControlBehavior;
import model.game.entity.zombie.behavior.ProjectileDefenseBehavior;
import model.game.entity.zombie.behavior.TimedDirectionBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ZombieSpecialBehaviorTest {

    private Plant plant;

    @BeforeEach
    void createPlant() throws Exception {
        PlantRegistry registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        plant = new PlantFactory().createBaseLevel(registry.getDefinition("Peashooter"), 3, 1);
    }

    @Test
    void contactAttackerInstantlyDestroysItsFirstPlantAndSlows() {
        AtomicBoolean destroyed = new AtomicBoolean();
        GameContext context = contextWithPlant(plant, destroyed);
        Zombie zombie = new Zombie.Builder("all-star")
                .maxHealth(100)
                .speed(2.0)
                .position(3.4, 1)
                .addBehavior(new ContactAttackBehavior(true, 0.5))
                .build();

        zombie.onTickUpdate(context);

        assertTrue(plant.isDead());
        assertTrue(destroyed.get());
        assertEquals(1.0, zombie.getCurrentSpeed(), 0.0001);

        Plant second = plant;
        int healthAfterCharge = second.getHealth();
        zombie.onTickUpdate(context);
        assertEquals(healthAfterCharge, second.getHealth());
        assertEquals(1.0, zombie.getCurrentSpeed(), 0.0001);
    }

    @Test
    void allStarChargeCanBeConsumedByHypnotizedZombie() {
        Zombie hypnotized = new Zombie.Builder("ally").maxHealth(50)
                .position(3.1, 1).build();
        hypnotized.setHypnotized(true);
        GameContext context = proxy((method, args) -> switch (method) {
            case "getZombiesInRow", "getAllZombies" -> List.of(hypnotized);
            default -> null;
        });
        Zombie allStar = new Zombie.Builder("all-star").speed(2).position(3.4, 1)
                .addBehavior(new ContactAttackBehavior(true, 0.5)).build();

        allStar.onTickUpdate(context);

        assertTrue(hypnotized.isDead());
        assertEquals(1.0, allStar.getCurrentSpeed(), 0.0001);
    }

    @Test
    void prospectorRelocatesAfterTenSecondsAndThenMovesRight() {
        Zombie zombie = new Zombie.Builder("prospector")
                .speed(1.0)
                .position(8.0, 1)
                .addBehavior(new TimedDirectionBehavior(100))
                .addBehavior(new model.game.entity.zombie.behavior.MovementBehavior())
                .build();
        GameContext context = emptyContext();

        for (int i = 0; i < 100; i++) {
            zombie.onTickUpdate(context);
        }
        assertEquals(0.0, zombie.getX(), 0.0001);
        assertTrue(zombie.isMovingRight());

        zombie.onTickUpdate(context);
        assertEquals(0.1, zombie.getX(), 0.0001);
    }

    @Test
    void wizardRestoresOnlyItsOwnTransformationsOnDeath() {
        GameContext context = contextWithPlant(plant, new AtomicBoolean());
        Zombie wizard = new Zombie.Builder("wizard")
                .maxHealth(10)
                .position(4.0, 1)
                .addBehavior(new PlantControlBehavior(PlantControlBehavior.Mode.WIZARD, 1, 4))
                .build();

        wizard.onTickUpdate(context);
        assertTrue(plant.isCatTransformedBy(wizard.getId()));
        assertFalse(plant.canBeTargetedByZombie());

        wizard.takeDirectDamage(10);
        assertFalse(plant.isCatTransformed());
        assertTrue(plant.canBeTargetedByZombie());
    }

    @Test
    void snorkelRejectsStraightShotsButAllowsLobbersWhileSubmerged() {
        ProjectileDefenseBehavior defense = new ProjectileDefenseBehavior(
                ProjectileDefenseBehavior.Mode.SNORKEL, 1.0);
        Zombie snorkel = new Zombie.Builder("snorkel").addBehavior(defense).build();
        GameContext context = proxy((method, args) ->
                "isWaterAt".equals(method) ? true : null);
        snorkel.onTickUpdate(context);
        Projectile straight = new Projectile(1, 2, 10, ProjectileProfile.straight(),
                ProjectileEffect.PEA, plant, 0);
        Projectile lobbed = new Projectile(1, 2, 10, ProjectileProfile.arcing(),
                ProjectileEffect.GENERIC, plant, 0);

        assertTrue(snorkel.interceptProjectile(straight, context));
        assertFalse(snorkel.interceptProjectile(lobbed, context));
    }

    private static GameContext contextWithPlant(Plant target, AtomicBoolean destroyed) {
        return proxy((method, args) -> switch (method) {
            case "getPlantAt", "getPlantInFront" -> target;
            case "getAllPlants" -> List.of(target);
            case "onPlantDestroyed" -> {
                destroyed.set(true);
                yield null;
            }
            default -> null;
        });
    }

    private static GameContext emptyContext() {
        return proxy((method, args) -> null);
    }

    private static GameContext proxy(Invocation invocation) {
        return (GameContext) Proxy.newProxyInstance(
                GameContext.class.getClassLoader(),
                new Class<?>[]{GameContext.class},
                (proxy, method, args) -> {
                    Object result = invocation.invoke(method.getName(), args);
                    if (result != null) {
                        return result;
                    }
                    return switch (method.getName()) {
                        case "getTicksPerSecond" -> 10;
                        case "getRowCount" -> 5;
                        case "getColCount" -> 9;
                        case "getZombiesInRow", "getAllZombies", "getAllPlants" -> List.of();
                        default -> defaultValue(method.getReturnType());
                    };
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == double.class) {
            return 0.0;
        }
        return 0;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String method, Object[] args);
    }
}
