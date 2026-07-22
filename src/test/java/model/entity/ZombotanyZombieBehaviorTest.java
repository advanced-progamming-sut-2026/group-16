package model.entity;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.entity.GameContext;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantFactory;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieFactory;
import model.game.entity.zombie.behavior.ContactAttackBehavior;
import model.game.entity.zombie.behavior.RangedAttackBehavior;
import model.game.entity.zombie.behavior.RowBurnBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZombotanyZombieBehaviorTest {

    private PlantFactory plantFactory;
    private PlantRegistry plantRegistry;
    private ZombieFactory zombieFactory;

    @BeforeEach
    void setUp() throws Exception {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        plantFactory = new PlantFactory();

        ZombieRegistry registry = new ZombieRegistry();
        registry.loadFromJson("src/main/resources/zombotany-zombies.json");
        zombieFactory = new ZombieFactory(registry);
    }

    @Test
    void peaShooterZombieSpawnsLeftwardProjectileWhenPlantInRange() {
        Plant plant = plantFactory.createBaseLevel(
                plantRegistry.getDefinition("Peashooter"), 2, 1);
        AtomicInteger spawnCalls = new AtomicInteger();
        AtomicReference<Integer> damage = new AtomicReference<>();
        AtomicReference<String> projectileType = new AtomicReference<>();
        GameContext context = proxy((method, args) -> switch (method) {
            case "getPlantAt", "getPlantInFront" -> plant;
            case "spawnProjectile" -> {
                spawnCalls.incrementAndGet();
                if (args.length >= 4 && args[3] instanceof Integer dmg) {
                    damage.set(dmg);
                } else if (args.length >= 5 && args[3] instanceof Integer dmg) {
                    damage.set(dmg);
                }
                if (args.length >= 4 && args[args.length - 1] instanceof String type) {
                    projectileType.set(type);
                }
                yield null;
            }
            default -> null;
        });

        Zombie zombie = zombieFactory.createZombie("ZombiePeaShooter", 6.0, 1);
        assertTrue(zombie.getBehaviors().stream().anyMatch(b -> b instanceof RangedAttackBehavior));

        for (int i = 0; i < 20; i++) {
            zombie.onTickUpdate(context);
        }
        assertTrue(spawnCalls.get() >= 1);
        assertEquals(20, damage.get());
        assertEquals("pea", projectileType.get());
    }

    @Test
    void wallNutZombieHasHighHealthAndNoSpecialBehavior() {
        Zombie zombie = zombieFactory.createZombie("ZombieWallNut");
        assertEquals(1200, zombie.getMaxHealth());
        assertEquals(0.15, zombie.getBaseSpeed(), 0.0001);
        assertFalse(zombie.getBehaviors().stream().anyMatch(b -> b instanceof RangedAttackBehavior));
        assertFalse(zombie.getBehaviors().stream().anyMatch(b -> b instanceof RowBurnBehavior));
        assertFalse(zombie.getBehaviors().stream().anyMatch(b -> b instanceof ContactAttackBehavior));
    }

    @Test
    void jalapenoZombieBurnsEntireRowAfterTenSecondsThenDies() {
        Plant left = plantFactory.createBaseLevel(plantRegistry.getDefinition("Peashooter"), 1, 1);
        Plant right = plantFactory.createBaseLevel(plantRegistry.getDefinition("Sunflower"), 5, 1);
        Map<Integer, Plant> plants = new HashMap<>();
        plants.put(1, left);
        plants.put(5, right);
        AtomicInteger destroyed = new AtomicInteger();
        AtomicBoolean zombieKilled = new AtomicBoolean();

        GameContext context = proxy((method, args) -> switch (method) {
            case "getPlantAt" -> plants.get((Integer) args[0]);
            case "onPlantDestroyed" -> {
                destroyed.incrementAndGet();
                yield null;
            }
            case "onZombieKilled" -> {
                zombieKilled.set(true);
                yield null;
            }
            default -> null;
        });

        Zombie jalapeno = zombieFactory.createZombie("ZombieJalapeno", 7.0, 1);
        assertTrue(jalapeno.getBehaviors().stream().anyMatch(b -> b instanceof RowBurnBehavior));

        for (int i = 0; i < 99; i++) {
            jalapeno.onTickUpdate(context);
        }
        assertFalse(left.isDead());
        assertFalse(right.isDead());
        assertFalse(jalapeno.isDead());

        jalapeno.onTickUpdate(context);
        assertTrue(left.isDead());
        assertTrue(right.isDead());
        assertTrue(jalapeno.isDead());
        assertEquals(2, destroyed.get());
        assertTrue(zombieKilled.get());
    }

    @Test
    void squashZombieDestroysPlantAndItselfOnContact() {
        Plant plant = plantFactory.createBaseLevel(
                plantRegistry.getDefinition("Peashooter"), 3, 1);
        AtomicBoolean plantDestroyed = new AtomicBoolean();
        AtomicBoolean zombieKilled = new AtomicBoolean();
        GameContext context = proxy((method, args) -> switch (method) {
            case "getPlantAt", "getPlantInFront" -> plant;
            case "onPlantDestroyed" -> {
                plantDestroyed.set(true);
                yield null;
            }
            case "onZombieKilled" -> {
                zombieKilled.set(true);
                yield null;
            }
            default -> null;
        });

        Zombie squash = zombieFactory.createZombie("ZombieSquash", 3.4, 1);
        assertTrue(squash.getBehaviors().stream().anyMatch(b -> b instanceof ContactAttackBehavior));
        assertEquals(0.55, squash.getBaseSpeed(), 0.0001);

        squash.onTickUpdate(context);

        assertTrue(plant.isDead());
        assertTrue(plantDestroyed.get());
        assertTrue(squash.isDead());
        assertTrue(zombieKilled.get());
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
