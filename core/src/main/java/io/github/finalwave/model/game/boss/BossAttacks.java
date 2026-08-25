package io.github.finalwave.model.game.boss;

import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public final class BossAttacks {

    private BossAttacks() {
    }

    public static final class Idle implements BossAttack {
        private int remaining;

        @Override
        public void start(BossArena arena) {
            remaining = arena.randomIdleTicks();
            arena.setClip("idle");
        }

        @Override
        public boolean tick(BossArena arena) {
            arena.setClip("idle");
            remaining--;
            return remaining <= 0;
        }
    }

    public static final class Missile implements BossAttack {
        private final boolean graves;
        private int elapsed;
        private int startTicks;
        private int impactTicks;
        private boolean launched;
        private boolean struck;
        private final int[] cell = new int[2];

        public Missile(boolean graves) {
            this.graves = graves;
        }

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            launched = false;
            struck = false;
            startTicks = graves ? BossCatalog.MISSILE_START_TICKS : BossCatalog.MISSILE_ICE_START_TICKS;
            impactTicks = startTicks + BossCatalog.MISSILE_DELAY_TICKS + BossCatalog.MISSILE_FLIGHT_TICKS;
            arena.pickPlantCell(cell);
            arena.setClip(graves ? "missile" : "slingshot");
            arena.emit(graves ? BossVfx.Kind.LOCK_RETICLE : BossVfx.Kind.LOCK_RETICLE_ICE, cell[0], cell[1]);
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            if (!launched && elapsed >= startTicks) {
                launched = true;
                if (graves) {
                    arena.setClip("missile_launch");
                    arena.emit(BossVfx.Kind.MISSILE_FLIGHT, cell[0], cell[1]);
                } else {
                    arena.emit(BossVfx.Kind.ICE_MISSILE_FLIGHT, cell[0], cell[1]);
                }
            }
            if (!struck && elapsed >= impactTicks) {
                struck = true;
                arena.destroyPlantAt(cell[0], cell[1]);
                if (graves) {
                    arena.placeGraves(2);
                    arena.emit(BossVfx.Kind.MISSILE_EGYPT, cell[0], cell[1]);
                } else {
                    arena.emit(BossVfx.Kind.MISSILE_ICE, cell[0], cell[1]);
                }
            }
            return elapsed >= impactTicks && elapsed >= startTicks + BossCatalog.MISSILE_LAUNCH_TICKS;
        }
    }

    public static final class Charge implements BossAttack {
        private boolean returning;

        @Override
        public void start(BossArena arena) {
            returning = false;
            arena.setClip("walk_forward");
        }

        @Override
        public boolean tick(BossArena arena) {
            Zombie boss = arena.boss();
            if (!returning) {
                boss.moveLeft(BossArena.CHARGE_STEP);
                crushColumn(arena, (int) Math.floor(boss.getX()));
                if (boss.getX() <= BossArena.CHARGE_END_X) {
                    boss.setPosition(BossArena.CHARGE_END_X, boss.getRow());
                    arena.destroyPlantsOnOccupiedRows();
                    returning = true;
                    arena.setClip("walk_backwards");
                }
                return false;
            }
            boss.moveRight(BossArena.CHARGE_STEP);
            if (boss.getX() >= arena.homeX()) {
                boss.setPosition(arena.homeX(), boss.getRow());
                arena.setClip("idle");
                return true;
            }
            return false;
        }

        private static void crushColumn(BossArena arena, int col) {
            for (int row : arena.occupiedRows()) {
                arena.destroyPlantAt(col, row);
                arena.destroyPlantAt(col + 1, row);
            }
        }
    }

    public static final class Summon implements BossAttack {
        private int elapsed;
        private boolean spawned;
        private boolean closing;

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            spawned = false;
            closing = false;
            arena.setClip("portal");
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            if (!spawned && elapsed >= BossCatalog.PORTAL_START_TICKS) {
                spawned = true;
                List<String> pool = BossCatalog.summonPool(arena.chapter());
                if (!pool.isEmpty()) {
                    int count = 2 + arena.random().nextInt(3);
                    int[] rows = arena.occupiedRows();
                    for (int i = 0; i < count; i++) {
                        String alias = pool.get(arena.random().nextInt(pool.size()));
                        int row = rows[arena.random().nextInt(rows.length)];
                        arena.spawnMinion(alias, row, arena.spawnX());
                    }
                }
            }
            if (!closing && elapsed >= BossCatalog.PORTAL_START_TICKS + BossCatalog.PORTAL_LOOP_TICKS) {
                closing = true;
                arena.setClip("portal_end");
            }
            return elapsed >= BossCatalog.PORTAL_START_TICKS
                    + BossCatalog.PORTAL_LOOP_TICKS
                    + BossCatalog.PORTAL_END_TICKS;
        }
    }

    public static final class LaneSwitch implements BossAttack {
        private int elapsed;
        private int duration;
        private double startY;
        private int targetRow;

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            duration = BossCatalog.LANE_SWITCH_TICKS;
            startY = arena.boss().getY();
            int current = arena.primaryRow();
            int max = arena.maxPrimaryRow();
            if (max <= 0) {
                targetRow = current;
                duration = 1;
                return;
            }
            do {
                targetRow = arena.random().nextInt(max + 1);
            } while (targetRow == current && max > 0);
            arena.setClip(targetRow < current ? "walk_up" : "walk_down");
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            double t = Math.min(1.0, elapsed / (double) Math.max(1, duration));
            arena.boss().setVisualY(startY + (targetRow - startY) * t);
            if (elapsed < duration) {
                return false;
            }
            arena.boss().setRow(targetRow);
            arena.setClip("idle");
            return true;
        }
    }

    public static final class Fireball implements BossAttack {
        private int elapsed;
        private boolean launched;
        private boolean struck;
        private boolean closing;
        private final List<int[]> cells = new ArrayList<>();

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            launched = false;
            struck = false;
            closing = false;
            cells.clear();
            int shots = 2 + arena.random().nextInt(3);
            arena.pickUniqueCells(cells, shots);
            arena.setClip("fire_bomb");
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            if (!launched && elapsed >= BossCatalog.FIRE_BOMB_TICKS) {
                launched = true;
                arena.setClip("fire_bomb_loop");
                for (int[] cell : cells) {
                    arena.emit(BossVfx.Kind.FIREBALL_FLIGHT, cell[0], cell[1]);
                }
            }
            if (!closing && elapsed >= BossCatalog.FIRE_BOMB_TICKS + BossCatalog.FIRE_BOMB_LOOP_TICKS) {
                closing = true;
                arena.setClip("fire_bomb_end");
            }
            if (!struck && elapsed >= BossCatalog.FIRE_BOMB_TICKS + BossCatalog.FIREBALL_FLIGHT_TICKS) {
                struck = true;
                for (int[] cell : cells) {
                    arena.strikeFireball(cell[0], cell[1]);
                }
            }
            return elapsed >= BossCatalog.FIRE_BOMB_TICKS + BossCatalog.FIREBALL_FLIGHT_TICKS
                    && elapsed >= BossCatalog.FIRE_BOMB_TICKS
                    + BossCatalog.FIRE_BOMB_LOOP_TICKS
                    + BossCatalog.FIRE_BOMB_END_TICKS;
        }
    }

    public static final class DragonFire implements BossAttack {
        private int elapsed;
        private boolean struck;
        private boolean closing;

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            struck = false;
            closing = false;
            arena.setClip("fire");
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            if (!struck && elapsed >= BossCatalog.FIRE_ATTACK_TICKS) {
                struck = true;
                arena.scorchOccupiedRows();
            }
            if (!closing && elapsed >= BossCatalog.FIRE_ATTACK_TICKS + BossCatalog.FIRE_ATTACK_LOOP_TICKS) {
                closing = true;
                arena.setClip("fire_end");
            }
            return elapsed >= BossCatalog.FIRE_ATTACK_TICKS
                    + BossCatalog.FIRE_ATTACK_LOOP_TICKS
                    + BossCatalog.FIRE_ATTACK_END_TICKS;
        }
    }

    public static final class IceWind implements BossAttack {
        private int elapsed;
        private boolean struck;
        private int[] rows = new int[0];

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            struck = false;
            rows = arena.pickAdjacentRows(2);
            arena.setClip("wind");
            int originCol = Math.max(0, arena.board().getCols() - 1);
            for (int row : rows) {
                arena.emit(BossVfx.Kind.ICE_WIND, originCol, row);
            }
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            if (!struck && elapsed >= BossCatalog.WIND_START_TICKS) {
                struck = true;
                arena.applyIceWindOnRows(rows, BossCatalog.ICE_WIND_FROST_STACKS);
            }
            return elapsed >= BossCatalog.WIND_TICKS;
        }
    }

    public static final class FreezeColumn implements BossAttack {
        private int elapsed;
        private boolean struck;
        private int column;

        @Override
        public void start(BossArena arena) {
            elapsed = 0;
            struck = false;
            int cols = arena.board().getCols();
            column = 1 + arena.random().nextInt(Math.max(1, cols - 3));
            int clip = Math.min(6, Math.max(1, column));
            arena.setClip("glacier_" + clip);
        }

        @Override
        public boolean tick(BossArena arena) {
            elapsed++;
            if (!struck && elapsed >= BossCatalog.GLACIER_STRIKE_TICKS) {
                struck = true;
                arena.freezeColumn(column);
            }
            return elapsed >= BossCatalog.GLACIER_TICKS;
        }
    }

    public static final class SharkBite implements BossAttack {
        private int remaining;
        private boolean struck;

        @Override
        public void start(BossArena arena) {
            remaining = 14;
            struck = false;
            arena.setClip("idle");
        }

        @Override
        public boolean tick(BossArena arena) {
            remaining--;
            if (!struck && remaining <= 6) {
                struck = true;
                int[] cell = new int[2];
                if (arena.swallowWaterPlant(cell)) {
                    arena.emit(BossVfx.Kind.SHARK, cell[0], cell[1]);
                }
            }
            return remaining <= 0;
        }
    }

    public static final class Vacuum implements BossAttack {
        private int remaining;
        private boolean struck;

        @Override
        public void start(BossArena arena) {
            remaining = 16;
            struck = false;
            arena.setClip("suction");
        }

        @Override
        public boolean tick(BossArena arena) {
            arena.setClip("suction");
            remaining--;
            if (!struck && remaining <= 6) {
                struck = true;
                int[] rows = arena.occupiedRows();
                arena.emit(BossVfx.Kind.VACUUM, (int) arena.boss().getX(), rows[0]);
                arena.vacuumOccupied();
            }
            return remaining <= 0;
        }
    }

    public static BossAttack randomSpecial(BossArena arena) {
        ChapterId chapter = arena.chapter();
        List<BossAttack> options = switch (chapter) {
            case DARK_AGES -> List.of(
                    new Fireball(),
                    new DragonFire(),
                    new Summon(),
                    new LaneSwitch());
            case FROSTBITE_CAVES -> List.of(
                    new Missile(false),
                    new IceWind(),
                    new FreezeColumn());
            case BIG_WAVE_BEACH -> List.of(
                    new SharkBite(),
                    new Vacuum(),
                    new Summon(),
                    new LaneSwitch());
            default -> List.of(
                    new Missile(true),
                    new Charge(),
                    new Summon(),
                    new LaneSwitch());
        };
        if (!BossCatalog.allowsSummon(chapter)) {
            options = options.stream().filter(attack -> !(attack instanceof Summon)).toList();
        }
        if (!BossCatalog.allowsLaneSwitch(chapter)) {
            options = options.stream().filter(attack -> !(attack instanceof LaneSwitch)).toList();
        }
        if (options.isEmpty()) {
            return new Idle();
        }
        return options.get(arena.random().nextInt(options.size()));
    }
}
