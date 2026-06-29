package model.game.entity.zombie;

import model.definition.ZombieRegistry;
import model.definition.armor.ArmorDefinition;
import model.definition.zombie.ZombieDefinition;
import model.game.entity.zombie.behavior.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ZombieFactory {

    @FunctionalInterface
    private interface BehaviorConfigurer extends BiConsumer<Zombie.Builder, ZombieDefinition> {
    }

    private final ZombieRegistry registry;

    private static final Map<String, BehaviorConfigurer> BEHAVIOR_MAP = Map.ofEntries(
            Map.entry("ZombiePropertySheet", ZombieFactory::configureBasic),
            Map.entry("ZombieGargantuarProps", ZombieFactory::configureGargantuar),
            Map.entry("ZombieNewspaperProps", ZombieFactory::configureNewspaper),
            Map.entry("ZombieModernAllStarProps", ZombieFactory::configureAllStar),
            Map.entry("ZombieProspectorProps", ZombieFactory::configureProspector),
            Map.entry("ZombieCrystalSkullProps", ZombieFactory::configureCrystalSkull),
            Map.entry("ZombieIceAgeHunterProps", ZombieFactory::configureHunter)
    );

    public ZombieFactory(ZombieRegistry registry) {
        this.registry = registry;
    }

    public Zombie createZombie(String alias, int difficulty) {
        ZombieDefinition def = registry.getDefinition(alias);
        if (def == null) {
            throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        }
        return createFromDefinition(def, difficulty);
    }

    public Zombie createZombie(String alias) {
        return createZombie(alias, 1);
    }

    private Zombie createFromDefinition(ZombieDefinition def, int difficulty) {
        double diffMult = 1.0 + (difficulty - 1) * 0.1;
        int scaledHp = (int) Math.round(def.getHitpoints() * diffMult);
        int scaledDmg = (int) Math.round(def.getEatDps() * diffMult);

        List<Armor> armors = buildArmors(def);

        Zombie.Builder builder = new Zombie.Builder(def.getAlias())
                .maxHealth(scaledHp)
                .speed(def.getSpeed())
                .damage(scaledDmg)
                .waveCost(def.getWavePointCost())
                .glowing(false);

        for (Armor a : armors) {
            builder.armor(a);
        }

        // Every zombie gets movement
        builder.addBehavior(new MovementBehavior());

        // Attach type-specific behaviors based on JSON "objclass"
        BehaviorConfigurer configurer = BEHAVIOR_MAP.get(def.getObjClass());
        if (configurer != null) {
            configurer.accept(builder, def);
        } else {
            // Default to basic if objclass is unknown
            configureBasic(builder, def);
        }

        return builder.build();
    }

    private List<Armor> buildArmors(ZombieDefinition def) {
        if (!def.hasArmor()) {
            return List.of();
        }
        List<Armor> armors = new ArrayList<>();
        for (ArmorDefinition armorDef : registry.resolveArmorFor(def)) {
            armors.add(Armor.fromDefinition(armorDef));
        }
        return armors;
    }

    private static void configureBasic(Zombie.Builder b, ZombieDefinition d) {
        // Basic zombie: just movement (already added)
    }

    private static void configureGargantuar(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new TransformBehavior(
                TransformBehavior.TransformType.SMASH, 20));

        double threshold = extractGargantuarImpThreshold(d);
        b.addBehavior(new SummonBehavior("ZombieImp", threshold, -3.0, 0));
    }

    private static void configureNewspaper(Zombie.Builder b, ZombieDefinition d) {
        Double speedMult = d.getExtraAsDouble("EnragedSpeedScale");
        b.addBehavior(new DamageReactionBehavior(
                DamageReactionBehavior.Trigger.ARMOR_DESTROYED,
                0.0,
                speedMult != null ? speedMult : 4.0));
        b.addBehavior(new ArmorBehavior(1.2));
    }

    private static void configureAllStar(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new TransformBehavior(
                TransformBehavior.TransformType.SMASH, 0));
    }

    private static void configureProspector(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new TransformBehavior(
                TransformBehavior.TransformType.VAULT_OVER, 0));
    }

    private static void configureCrystalSkull(Zombie.Builder b, ZombieDefinition d) {
        Double dmg = d.getExtraAsDouble("LaserBeamDamage");
        b.addBehavior(new RangedAttackBehavior(
                dmg != null ? dmg.intValue() : 4000,
                50, "laser", 9.0));
    }

    private static void configureHunter(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new RangedAttackBehavior(
                20, 50, "snowball", 4.0));
    }

    private static void configurePeashooter(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new RangedAttackBehavior(
                20, 15, "pea", 9.0));
    }

    private static void configureJalapeno(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new WorldEffectBehavior(
                WorldEffectBehavior.EffectTrigger.ON_TIMER,
                0.0, 30, "fire", 60));
    }

    private static double extractGargantuarImpThreshold(ZombieDefinition d) {
        Object layers = d.getExtra("HealthThresholdToImpAmmoLayers");
        if (layers instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> map) {
                Object val = map.get("HealthPercentThrowImp");
                if (val instanceof Number n) {
                    return n.doubleValue();
                }
            }
        }
        return 0.5; // Default fallback
    }
}