package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Represents a set bonus effect that activates when a player completes a set.
 * Similar to WeaponEffect but for armor set bonuses.
 */
public interface ArmorSetBonus {
    
    /**
     * Applies the set bonus to the player.
     * @param player The player who completed the set
     * @param config The configuration section for this bonus
     * @param pieces The number of pieces equipped (2 or 4)
     * @param setName The name of the armor set (for composite key generation)
     */
    void apply(Player player, ConfigurationSection config, int pieces, String setName);
    
    /**
     * Removes the set bonus from the player.
     * @param player The player who lost the set
     * @param config The configuration section for this bonus
     * @param pieces The number of pieces that were equipped (2 or 4)
     * @param setName The name of the armor set (for composite key generation)
     */
    void remove(Player player, ConfigurationSection config, int pieces, String setName);
    
    /**
     * Gets the unique type identifier for this set bonus.
     */
    String getType();
}
