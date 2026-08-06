package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.definition.armor.ArmorDefinition;
import io.github.finalwave.model.definition.zombie.ZombieDefinition;
import io.github.finalwave.model.game.entity.zombie.behavior.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
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
            Map.entry("ZombieIceAgeHunterProps", ZombieFactory::configureHunter),
            Map.entry("ZombieRaProps", ZombieFactory::configureRa),
            Map.entry("ZombieExplorerProps", ZombieFactory::configureExplorer),
            Map.entry("ZombieTombRaiserProps", ZombieFactory::configureTombRaiser),
            Map.entry("ZombieIceAgeDodoProps", ZombieFactory::configureDodo),
            Map.entry("ZombieIceAgeTroglobiteProps", ZombieFactory::configureTroglobite),
            Map.entry("ZombieBeachFishermanProps", ZombieFactory::configureFisherman),
            Map.entry("ZombieBeachOctopusProps", ZombieFactory::configureOctopus),
            Map.entry("ZombieBeachSnorkelProps", ZombieFactory::configureSnorkel),
            Map.entry("ZombieDarkJugglerProps", ZombieFactory::configureJester),
            Map.entry("ZombieDarkWizardProps", ZombieFactory::configureWizard),
            Map.entry("ZombieDarkKingProps", ZombieFactory::configureKing),
            Map.entry("ZombieLostCityJaneProps", ZombieFactory::configureJane),
            Map.entry("ZombiePianoProps", ZombieFactory::configurePiano),
            Map.entry("ZombieArcadeProps", ZombieFactory::configureArcade),
            Map.entry("ZombieZombotanyPeaProps", ZombieFactory::configureZombotanyPea),
            Map.entry("ZombieZombotanyJalapenoProps", ZombieFactory::configureZombotanyJalapeno),
            Map.entry("ZombieZombotanySquashProps", ZombieFactory::configureZombotanySquash)
    );

    public ZombieFactory(ZombieRegistry registry) {
        this.registry = registry;
    }

    public Zombie createZombie(String alias, int difficulty) {
        return createZombie(alias, 0.0, 0, difficulty);
    }

    public Zombie createZombie(String alias, double spawnX, int row) {
        return createZombie(alias, spawnX, row, 1);
    }

    public Zombie createZombie(String alias, double spawnX, int row, int difficulty) {
        if (difficulty < 1) {
            throw new IllegalArgumentException("difficulty must be at least 1");
        }
        if (!Double.isFinite(spawnX)) {
            throw new IllegalArgumentException("spawnX must be finite");
        }
        if (row < 0) {
            throw new IllegalArgumentException("row must be non-negative");
        }
        ZombieDefinition def = registry.getDefinition(alias);
        if (def == null) {
            throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        }
        return createFromDefinition(def, spawnX, row, difficulty);
    }

    public Zombie createZombie(String alias) {
        return createZombie(alias, 1);
    }

    private Zombie createFromDefinition(ZombieDefinition def, double spawnX, int row, int difficulty) {
        double diffMult = 1.0 + (difficulty - 1) * 0.1;
        int scaledHp = (int) Math.round(def.getHitpoints() * diffMult);
        int scaledDmg = (int) Math.round(def.getEatDps() * diffMult);

        List<Armor> armors = buildArmors(def);

        Zombie.Builder builder = new Zombie.Builder(def.getAlias())
                .maxHealth(scaledHp)
                .speed(def.getSpeed())
                .damage(scaledDmg)
                .waveCost(def.getWavePointCost())
                .position(spawnX, row)
                .glowing(false)
                .knightTarget("ZombiePropertySheet".equals(def.getObjClass()),
                        knightTargetKey(def.getAlias()));

        for (Armor a : armors) {
            builder.armor(a);
        }

        // Every zombie gets movement
        builder.addBehavior(new MovementBehavior());

        // Attach type-specific behaviors based on JSON "objclass"
        BehaviorConfigurer configurer = BEHAVIOR_MAP.get(def.getObjClass());
        if (configurer == null) {
            throw new IllegalArgumentException("Unsupported zombie objclass: " + def.getObjClass()
                    + " (alias " + def.getAlias() + ")");
        }
        configurer.accept(builder, def);

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
        // The documented third column is index 2 on the zero-based board.
        b.addBehavior(SummonBehavior.atFixedColumn("ZombieImp", threshold, 2, 0));
    }

    private static void configureNewspaper(Zombie.Builder b, ZombieDefinition d) {
        Double speedMult = d.getExtraAsDouble("EnragedSpeedScale");
        b.addBehavior(new DamageReactionBehavior(
                DamageReactionBehavior.Trigger.ARMOR_DESTROYED,
                0.0,
                speedMult != null ? speedMult : 4.0,
                ZombieBehaviorDefaults.number(d, "EnragedDamageScale", 4.0)));
        b.addBehavior(new ArmorBehavior(1.0));
    }

    private static void configureAllStar(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new TransformBehavior(
                TransformBehavior.TransformType.SMASH, 1, false));
        b.addBehavior(new ContactAttackBehavior(true,
                ZombieBehaviorDefaults.number(d, "RunningSpeedScale", 0.5)));
    }

    private static void configureProspector(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new TimedDirectionBehavior(
                ZombieBehaviorDefaults.ticks(d, "LaunchCountdown", 10.0)));
    }

    private static void configureCrystalSkull(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new RangedAttackBehavior(0, Integer.MAX_VALUE, "laser", 0));
        b.addBehavior(new SunStealBehavior(
                SunStealBehavior.Mode.CHARGE_AND_LASER,
                ZombieBehaviorDefaults.ticks(d, "ChargingTime", 5.0),
                ZombieBehaviorDefaults.TICKS_PER_SECOND,
                25,
                ZombieBehaviorDefaults.STANDARD_RANGE,
                4));
    }

    private static void configureHunter(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new PlantControlBehavior(
                PlantControlBehavior.Mode.HUNTER_ICE,
                ZombieBehaviorDefaults.STANDARD_COOLDOWN_TICKS,
                ZombieBehaviorDefaults.number(d, "FarAttackRange", 4.0)));
    }

    private static void configureRa(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new SunStealBehavior(SunStealBehavior.Mode.GROUND,
                0, ZombieBehaviorDefaults.TICKS_PER_SECOND,
                ZombieBehaviorDefaults.integer(d, "MaxClaimedSunCurrency", 250),
                0, 0));
    }

    private static void configureExplorer(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new TorchBehavior(
                ZombieBehaviorDefaults.number(d, "MaxTorchReach", 37.0) / 37.0));
    }

    private static void configureTombRaiser(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new GraveRaisingBehavior(
                ZombieBehaviorDefaults.integer(d, "NumberOfTombsToSpawn", 2),
                ZombieBehaviorDefaults.ticks(d, "TimeBetweenRaisings", 6.0)));
    }

    private static void configureDodo(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new MobilityTraitBehavior(MobilityTraitBehavior.Trait.DODO_BYPASS));
    }

    private static void configureTroglobite(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new IcePushBehavior(ZombieBehaviorDefaults.TICKS_PER_SECOND,
                ZombieBehaviorDefaults.integer(d, "NumberOfIceblocksToSpawnWith", 3)));
        b.addBehavior(new ContactAttackBehavior(false, 1.0));
    }

    private static void configureFisherman(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new FishermanBehavior(
                ZombieBehaviorDefaults.ticks(d, "DelayBetweenCasting", 2.5),
                ZombieBehaviorDefaults.number(d, "CastingAreaMinRange", 2.0),
                ZombieBehaviorDefaults.number(d, "CastingAreaMaxRange", 8.0)));
    }

    private static void configureOctopus(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new PlantControlBehavior(
                PlantControlBehavior.Mode.OCTOPUS,
                ZombieBehaviorDefaults.LONG_COOLDOWN_TICKS,
                contextRange(d)));
    }

    private static void configureSnorkel(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new ProjectileDefenseBehavior(
                ProjectileDefenseBehavior.Mode.SNORKEL, 1.0));
    }

    private static void configureJester(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new ProjectileDefenseBehavior(
                ProjectileDefenseBehavior.Mode.JESTER,
                ZombieBehaviorDefaults.number(d, "MoveSpeedMultiplierWhileJuggling", 1.1)));
    }

    private static void configureWizard(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new MobilityTraitBehavior(MobilityTraitBehavior.Trait.BYPASS_DISABLED));
        b.addBehavior(new PlantControlBehavior(
                PlantControlBehavior.Mode.WIZARD,
                ZombieBehaviorDefaults.STANDARD_COOLDOWN_TICKS,
                contextRange(d)));
    }

    private static void configureKing(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new KnightingBehavior(
                ZombieBehaviorDefaults.ticks(d, "DelayBetweenKnightings", 2.5),
                ZombieBehaviorDefaults.number(d, "KnightingAreaX", 4.0),
                ZombieBehaviorDefaults.integer(d, "KnightingAreaY", 3),
                validKnightTargets(d)));
    }

    private static void configureJane(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new ProjectileDefenseBehavior(
                ProjectileDefenseBehavior.Mode.BOUNCE_LOBBERS, 1.0));
    }

    private static void configurePiano(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new ContactAttackBehavior(false, 1.0));
        b.addBehavior(new LaneShiftBehavior(
                ZombieBehaviorDefaults.STANDARD_COOLDOWN_TICKS,
                ZombieBehaviorDefaults.number(d, "StreetCriticalSize", 3.0)));
    }

    private static void configureArcade(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new ArcadePushBehavior());
    }

    private static void configureZombotanyPea(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new RangedAttackBehavior(
                ZombieBehaviorDefaults.integer(d, "AttackDamage", 20),
                ZombieBehaviorDefaults.ticks(d, "AttackCooldownSeconds", 1.5),
                "pea",
                ZombieBehaviorDefaults.number(d, "AttackRange", 9.0)));
    }

    private static void configureZombotanyJalapeno(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new RowBurnBehavior(
                ZombieBehaviorDefaults.ticks(d, "BurnCountdownSeconds", 10.0)));
    }

    private static void configureZombotanySquash(Zombie.Builder b, ZombieDefinition d) {
        b.addBehavior(new ContactAttackBehavior(true, 1.0, true));
    }

    private static double contextRange(ZombieDefinition definition) {
        return ZombieBehaviorDefaults.number(definition, "FarAttackRange",
                ZombieBehaviorDefaults.STANDARD_RANGE);
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

    private static Set<String> validKnightTargets(ZombieDefinition definition) {
        Object raw = definition.getExtra("ValidKnightTargets");
        Set<String> targets = new LinkedHashSet<>();
        if (raw instanceof List<?> values) {
            for (Object value : values) {
                if (value instanceof String text && !text.isBlank()) {
                    targets.add(text.toLowerCase());
                }
            }
        }
        if (targets.isEmpty()) {
            targets.addAll(Set.of("default", "armor1", "armor2", "armor3", "armor4"));
        }
        return Set.copyOf(targets);
    }

    private static String knightTargetKey(String alias) {
        if (alias == null) {
            return null;
        }
        String key = alias.startsWith("Zombie") ? alias.substring("Zombie".length()) : alias;
        key = key.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return key.isBlank() ? "default" : key;
    }
}