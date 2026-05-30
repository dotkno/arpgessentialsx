package com.ahren.arpgessentialsx.weapons.passives;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface WeaponPassive {
    Trigger getTrigger();
    void apply(WeaponPassiveContext ctx);

    /**
     * Applies the passive effect when the weapon is equipped.
     * Used for state-tracking similar to set bonuses.
     */
    default void onEquip(Player player, ConfigurationSection config) {}

    /**
     * Removes the passive effect when the weapon is unequipped.
     * Used for state-tracking similar to set bonuses.
     */
    default void onUnequip(Player player, ConfigurationSection config) {}

    enum Trigger {
        ON_HIT,
        ON_KILL,
        ON_EQUIP,
        ON_DAMAGE_TAKEN
    }
}