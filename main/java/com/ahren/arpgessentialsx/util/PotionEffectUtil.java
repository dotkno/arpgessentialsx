package com.ahren.arpgessentialsx.util;

import org.bukkit.potion.PotionEffectType;

/**
 * Utility class for potion effect operations.
 */
public final class PotionEffectUtil {

    private PotionEffectUtil() {}

    /**
     * Determines if a potion effect type is positive (beneficial) or negative (harmful).
     *
     * @param type The potion effect type to check
     * @return true if the effect is beneficial, false if harmful
     */
    public static boolean isPositiveEffect(PotionEffectType type) {
        return type == PotionEffectType.REGENERATION ||
               type == PotionEffectType.SPEED ||
               type == PotionEffectType.HASTE ||
               type == PotionEffectType.STRENGTH ||
               type == PotionEffectType.HEALTH_BOOST ||
               type == PotionEffectType.ABSORPTION ||
               type == PotionEffectType.RESISTANCE ||
               type == PotionEffectType.FIRE_RESISTANCE ||
               type == PotionEffectType.WATER_BREATHING ||
               type == PotionEffectType.INVISIBILITY ||
               type == PotionEffectType.NIGHT_VISION ||
               type == PotionEffectType.JUMP_BOOST ||
               type == PotionEffectType.SATURATION ||
               type == PotionEffectType.GLOWING ||
               type == PotionEffectType.LUCK ||
               type == PotionEffectType.CONDUIT_POWER ||
               type == PotionEffectType.DOLPHINS_GRACE ||
               type == PotionEffectType.HERO_OF_THE_VILLAGE;
    }
}
