package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that consumes mana to deal bonus damage on charged shots.
 *
 * yml params:
 *   mana_cost_percentage: 0.15   (15% of max mana)
 *   bonus_damage_percentage: 0.35   (35% bonus damage)
 *
 * Trigger: 4-piece set completion, enhances charged shots
 */
public final class ChargedShotBonus implements ArmorSetBonus {

    private static final Map<UUID, Boolean> activePlayers = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        activePlayers.put(player.getUniqueId(), true);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public String getType() {
        return "charged_shot";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getManaCostPercentage(ConfigurationSection config) {
        return config.getDouble("mana_cost_percentage", 0.15);
    }

    public static double getBonusDamagePercentage(ConfigurationSection config) {
        return config.getDouble("bonus_damage_percentage", 0.35);
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
}
