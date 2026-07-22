package model.minigame.bowling;

import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.board.BoardGameContext;
import model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class BowlingNutSystem {

    public static final double SPEED = 0.2;
    public static final double GIANT_PUSH_DISTANCE = 1.0;
    public static final int CHERRY_BOMB_DAMAGE = 1800;
    public static final double CHERRY_BOMB_RADIUS = 1.5;
    public static final String NORMAL_ZOMBIE_ALIAS = "ZombieDefault";

    private final List<BowlingNut> nuts = new ArrayList<>();
    private final Random random;
    private int standardNutDamage = 190;

    public BowlingNutSystem(Random random) {
        this.random = random == null ? new Random() : random;
    }

    public List<BowlingNut> getNuts() {
        return List.copyOf(nuts);
    }

    public void configureDamageFromRegistry(ZombieRegistry registry) {
        if (registry == null) {
            return;
        }
        var definition = registry.getDefinition(NORMAL_ZOMBIE_ALIAS);
        if (definition != null && definition.getHitpoints() > 0) {
            standardNutDamage = definition.getHitpoints();
        }
    }

    public void spawn(BowlingNut nut) {
        if (nut != null) {
            nuts.add(nut);
        }
    }

    public void tick(GameSession session) {
        if (nuts.isEmpty()) {
            return;
        }
        BoardGameContext context = session.getContext();
        MatchListener listener = session.getMatchListener();
        int rows = session.getBoard().getRows();
        int cols = session.getBoard().getCols();

        Iterator<BowlingNut> iterator = nuts.iterator();
        while (iterator.hasNext()) {
            BowlingNut nut = iterator.next();
            moveNut(nut, rows);

            if (nut.getX() < 0 || nut.getX() >= cols) {
                iterator.remove();
                continue;
            }

            reflectRowBoundary(nut, rows);

            Zombie hit = findCollidingZombie(session, nut);
            if (hit != null) {
                handleZombieCollision(session, context, listener, nut, hit);
            }

            if (nut.getX() < 0 || nut.getX() >= cols) {
                iterator.remove();
            } else if (nut.getType() == BowlingNutType.EXPLOSIVE && nut.getZombieHitCount() > 0) {
                iterator.remove();
            }
        }
    }

    private void moveNut(BowlingNut nut, int rows) {
        double dx = Math.cos(nut.getAngleRadians()) * SPEED;
        double dy = Math.sin(nut.getAngleRadians()) * SPEED;
        nut.setX(nut.getX() + dx);
        nut.setRow(nut.getRow() + dy);
        if (nut.getRow() < 0) {
            nut.setRow(0);
        } else if (nut.getRow() > rows - 1) {
            nut.setRow(rows - 1);
        }
    }

    private void reflectRowBoundary(BowlingNut nut, int rows) {
        if (nut.getRow() <= 0 && Math.sin(nut.getAngleRadians()) < 0) {
            nut.setRow(0);
            nut.setAngleRadians(-nut.getAngleRadians());
        } else if (nut.getRow() >= rows - 1 && Math.sin(nut.getAngleRadians()) > 0) {
            nut.setRow(rows - 1);
            nut.setAngleRadians(-nut.getAngleRadians());
        }
    }

    private Zombie findCollidingZombie(GameSession session, BowlingNut nut) {
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            if (Math.abs(zombie.getRow() - nut.getRow()) > 0.5) {
                continue;
            }
            if (Math.abs(zombie.getX() - nut.getX()) <= 0.5) {
                return zombie;
            }
        }
        return null;
    }

    private void handleZombieCollision(GameSession session,
                                       BoardGameContext context,
                                       MatchListener listener,
                                       BowlingNut nut,
                                       Zombie zombie) {
        switch (nut.getType()) {
            case STANDARD -> handleStandardHit(session, listener, nut, zombie);
            case EXPLOSIVE -> handleExplosiveHit(session, context, listener, nut, zombie);
            case GIANT -> handleGiantHit(listener, nut, zombie);
        }
    }

    private void handleStandardHit(GameSession session,
                                   MatchListener listener,
                                   BowlingNut nut,
                                   Zombie zombie) {
        zombie.takeDirectDamage(standardNutDamage);
        if (zombie.isDead()) {
            session.handleZombieKilled(zombie);
        }
        if (listener != null) {
            listener.onBowlingNutHit(nut.getType(), zombie.getType(), nut.getX(), nut.getRow());
        }
        if (nut.getZombieHitCount() == 0) {
            double sign = random.nextBoolean() ? 1.0 : -1.0;
            nut.setAngleRadians(nut.getAngleRadians() + sign * Math.PI / 4.0);
        } else {
            nut.setAngleRadians(-nut.getAngleRadians());
        }
        nut.incrementZombieHitCount();
        nudgePastCollision(nut);
    }

    private void nudgePastCollision(BowlingNut nut) {
        nut.setX(nut.getX() + Math.cos(nut.getAngleRadians()) * 0.3);
        nut.setRow(nut.getRow() + Math.sin(nut.getAngleRadians()) * 0.3);
    }

    private void handleExplosiveHit(GameSession session,
                                      BoardGameContext context,
                                      MatchListener listener,
                                      BowlingNut nut,
                                      Zombie zombie) {
        int col = (int) Math.round(nut.getX());
        int row = (int) Math.round(nut.getRow());
        context.explodeAt(col, row, CHERRY_BOMB_DAMAGE, CHERRY_BOMB_RADIUS);
        nut.incrementZombieHitCount();
        if (listener != null) {
            listener.onBowlingNutExploded(col, row);
        }
    }

    private void handleGiantHit(MatchListener listener, BowlingNut nut, Zombie zombie) {
        zombie.moveRight(GIANT_PUSH_DISTANCE);
        nut.incrementZombieHitCount();
        nudgePastCollision(nut);
        if (listener != null) {
            listener.onBowlingNutHit(nut.getType(), zombie.getType(), nut.getX(), nut.getRow());
        }
    }
}
