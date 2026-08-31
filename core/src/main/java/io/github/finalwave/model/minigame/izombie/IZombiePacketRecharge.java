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
            case "ZombieProspector" -> IZombieDuelCatalog.rechargeSeconds(alias);
            case "ZombieArmor2" -> IZombieDuelCatalog.rechargeSeconds(alias);
            case "ZombieIceAgeDodo" -> IZombieDuelCatalog.rechargeSeconds(alias);
            default -> 7.5;
        };
    }

    public static double secondsFor(String alias, int cost) {
        Integer duelCost = IZombieDuelCatalog.zombieCosts().get(alias);
        if (duelCost != null && duelCost == cost) {
            return IZombieDuelCatalog.rechargeSeconds(alias);
        }
        return secondsFor(alias);
    }
}
