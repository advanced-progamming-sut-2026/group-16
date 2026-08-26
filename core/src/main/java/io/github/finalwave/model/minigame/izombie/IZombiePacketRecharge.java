package io.github.finalwave.model.minigame.izombie;


public final class IZombiePacketRecharge {
    private IZombiePacketRecharge() {
    }

    public static double secondsFor(String alias) {
        if (alias == null) {
            return 7.5;
        }
        return switch (alias) {
            case "ZombieImp" -> 0d;
            case "ZombieDefault" -> 7.5;
            case "ZombieArmor1", "ZombieNewspaper" -> 10d;
            case "ZombieBeachFisherman", "ZombieArmor4" -> 15d;
            case "ZombieDarkArmor3", "ZombiePiano", "ZombieBeachOctopus" -> 20d;
            case "ZombieGargantuar" -> 30d;
            default -> 7.5;
        };
    }
}
