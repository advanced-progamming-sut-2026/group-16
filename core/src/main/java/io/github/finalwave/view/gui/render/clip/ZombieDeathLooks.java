package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class ZombieDeathLooks {
    public static final String ASH_PATH = "768/INITIAL/EFFECTS/ZOMBIE_ASH/ZOMBIE_ASH.PAM";
    public static final String BIG_ASH_PATH = "768/INITIAL/EFFECTS/ZOMBIE_BIG_ASH/ZOMBIE_BIG_ASH.PAM";
    public static final String IMP_ASH_PATH = "768/INITIAL/EFFECTS/ZOMBIE_IMP_ASH/ZOMBIE_IMP_ASH.PAM";
    public static final String GARG_ASH_PATH =
            "768/INITIAL/EFFECTS/ZOMBIE_GARGANTUAR_ASH/ZOMBIE_GARGANTUAR_ASH.PAM";
    public static final String CLIP = "animation";

    private ZombieDeathLooks() {
    }

    public static String ashPath(Zombie zombie) {
        if (zombie == null || zombie.getType() == null) {
            return ASH_PATH;
        }
        String type = zombie.getType();
        if (type.contains("Gargantuar")) {
            return GARG_ASH_PATH;
        }
        if (type.contains("Imp") || type.contains("imp")) {
            return IMP_ASH_PATH;
        }
        if (zombie.isBoss() || zombie.getMaxHealth() >= 600) {
            return BIG_ASH_PATH;
        }
        return ASH_PATH;
    }
}
