package model.game.entity.projectile;

public record ProjectileProfile(Trajectory trajectory, boolean piercing, boolean homing) {

    public enum Trajectory {
        STRAIGHT, ARCING
    }

    public static ProjectileProfile straight() {
        return new ProjectileProfile(Trajectory.STRAIGHT, false, false);
    }

    public static ProjectileProfile piercingProfile() {
        return new ProjectileProfile(Trajectory.STRAIGHT, true, false);
    }

    public static ProjectileProfile arcing() {
        return new ProjectileProfile(Trajectory.ARCING, false, false);
    }

    public static ProjectileProfile homingProfile() {
        return new ProjectileProfile(Trajectory.STRAIGHT, false, true);
    }
}
