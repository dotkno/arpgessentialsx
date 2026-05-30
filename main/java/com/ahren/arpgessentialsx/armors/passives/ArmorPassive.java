package com.ahren.arpgessentialsx.armors.passives;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface ArmorPassive {
    Trigger getTrigger();
    void apply(ArmorPassiveContext ctx);

    /**
     * Applies the passive effect when the armor is equipped.
     * Used for state-tracking similar to set bonuses.
     */
    default void onEquip(Player player, ConfigurationSection config) {}

    /**
     * Removes the passive effect when the armor is unequipped.
     * Used for state-tracking similar to set bonuses.
     */
    default void onUnequip(Player player, ConfigurationSection config) {}

    enum Trigger {
        ON_EQUIP,
        ON_UNEQUIP,
        ON_DAMAGE_TAKEN,
        ON_HIT,
        ON_KILL,
        ON_TICK
    }
}
