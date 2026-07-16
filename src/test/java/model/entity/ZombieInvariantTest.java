package model.entity;

import model.definition.PlantRegistry;
import model.game.GameSession;
import model.game.entity.GameContext;
import model.game.entity.zombie.Armor;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieState;
import model.game.entity.zombie.behavior.MovementBehavior;
import model.quest.event.GameEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZombieInvariantTest {

    @Test
    void instancesHaveUniqueIdsButKeepTheirTypeAlias() {
        Zombie first = new Zombie.Builder("basic").build();
        Zombie second = new Zombie.Builder("basic").build();

        assertNotEquals(first.getId(), second.getId());
        assertEquals("basic", first.getType());
        assertEquals("basic", second.getType());
    }

    @Test
    void armorCanBeGrantedInDamageOrderAndDirectDamageBypassesIt() {
        Armor outer = new Armor("outer", "shield", 10, false, false);
        Armor inner = new Armor("inner", "helm", 20, false, true);
        Zombie zombie = new Zombie.Builder("armored").maxHealth(100).armor(outer).build();
        zombie.addArmor(inner);

        zombie.takeDamage(15);
        assertEquals(0, outer.getHealth());
        assertEquals(15, inner.getHealth());
        assertEquals(100, zombie.getHealth());

        zombie.takeDirectDamage(25);
        assertEquals(75, zombie.getHealth());
        assertEquals(15, inner.getHealth());
        assertThrows(UnsupportedOperationException.class,
                () -> zombie.getArmorLayers().add(outer));
    }

    @Test
    void permanentMultipliersComposeAndSurviveColdStatusChanges() {
        Zombie zombie = new Zombie.Builder("boosted")
                .speed(2.0)
                .damage(10)
                .build();

        zombie.multiplySpeed(1.5);
        zombie.multiplySpeed(2.0);
        zombie.multiplyEatingDamage(1.5);
        zombie.multiplyEatingDamage(2.0);
        assertEquals(6.0, zombie.getCurrentSpeed(), 0.0001);
        assertEquals(30, zombie.getDamage());

        zombie.applyChill(2);
        assertEquals(3.0, zombie.getCurrentSpeed(), 0.0001);
        zombie.applyFreeze(1);
        assertEquals(0.0, zombie.getCurrentSpeed(), 0.0001);
        zombie.tickStatuses();
        assertEquals(3.0, zombie.getCurrentSpeed(), 0.0001);
        zombie.clearColdStatuses();
        assertEquals(6.0, zombie.getCurrentSpeed(), 0.0001);
        assertEquals(30, zombie.getDamage());
    }

    @Test
    void abilityActionPreventsMovementInTheSameTick() {
        Zombie zombie = new Zombie.Builder("caster")
                .position(5.0, 1)
                .addBehavior((z, context) -> z.tryBeginAbilityAction())
                .addBehavior(new MovementBehavior())
                .build();

        zombie.onTickUpdate(emptyContext());

        assertEquals(5.0, zombie.getX(), 0.0001);
        assertEquals(ZombieState.ABILITY, zombie.getState());
    }

    @Test
    void sessionPublishesAndCleansUpEveryDeathExactlyOnce() {
        GameSession session = new GameSession(new PlantRegistry());
        List<GameEvent> events = new ArrayList<>();
        session.getEventBus().subscribe(events::add);
        Zombie zombie = new Zombie.Builder("doomed").maxHealth(10).build();
        session.addZombie(zombie);
        zombie.applyPoison(1, 10);

        session.start();
        session.tick();
        session.handleZombieKilled(zombie);
        session.tick();

        assertTrue(session.getZombies().isEmpty());
        assertEquals(1, events.stream().filter(GameEvent.ZombieKilled.class::isInstance).count());
    }

    private static GameContext emptyContext() {
        return (GameContext) Proxy.newProxyInstance(
                GameContext.class.getClassLoader(),
                new Class<?>[]{GameContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTicksPerSecond" -> 10;
                    case "getRowCount" -> 5;
                    case "getColCount" -> 9;
                    case "getZombiesInRow", "getAllZombies" -> List.of();
                    default -> defaultValue(method.getReturnType());
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
}
