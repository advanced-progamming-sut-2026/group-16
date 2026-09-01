package io.github.finalwave.view.gui.render.clip;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ZombieLimbLooksTest {

    @Test
    void egyptBoneArmPreferredOverBoneFlagPart() {
        Set<String> names = Set.of(
                "zombie_egypt_arm_outer_upper",
                "zombie_egypt_arm_outer_upper_bone",
                "zombie_egypt_arms_outer_upper",
                "zombie_egypt_arm_outer_lower",
                "zombie_egypt_hand_outer_01",
                "zombie_egypt_hand_inner_01",
                "zombie_egypt_skull"
        );
        assertEquals("zombie_egypt_arms_outer_upper", ZombieLimbLooks.boneArmPart(names));
    }

    @Test
    void hidesOnlyOuterHandKeepsInnerArmsAndArmsGroup() {
        assertTrue(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_hand_outer_01"));
        assertTrue(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_hand_outer_02"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_hand_inner_01"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_arm_inner_lower"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_arm_inner_upper"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_arm_outer_lower"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_arm_outer_upper"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_arms_outer_upper"));
        assertFalse(ZombieLimbLooks.isHiddenWhenArmDropped("zombie_egypt_arm_outer_upper_bone"));
    }

    @Test
    void midFightArmDropOnlyForAllowlistedFamilies() {
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieDefault"));
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieArmor1"));
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieArmor2"));
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieArmor4"));
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieNewspaper"));
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieDarkJuggler"));
        assertTrue(ZombieLimbLooks.losesArmMidFight("ZombieModernAllStar"));
        assertFalse(ZombieLimbLooks.losesArmMidFight("ZombieRa"));
        assertFalse(ZombieLimbLooks.losesArmMidFight("ZombieImp"));
    }

    @Test
    void headPartsExcludeParticles() {
        assertTrue(ZombieLimbLooks.isHeadPart("zombie_egypt_skull"));
        assertTrue(ZombieLimbLooks.isHeadPart("zombie_jaw"));
        assertFalse(ZombieLimbLooks.isHeadPart("particle_head"));
        assertFalse(ZombieLimbLooks.isHeadPart("zombie_egypt_arm_outer_upper"));
    }

    @Test
    void armDropRatioIsTwentyPercentHealthRemaining() {
        assertEquals(0.2f, ZombieLimbLooks.ARM_DROP_HEALTH_RATIO);
    }
}
