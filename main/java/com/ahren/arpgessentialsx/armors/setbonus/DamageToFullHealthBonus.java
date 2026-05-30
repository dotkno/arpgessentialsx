package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that increases damage to enemies at full health.
 *
 * yml params:
 *   damage_bonus: 0.20   (20% bonus damage)
 *
 * Trigger: 4-piece set completion, bonus damage to full health enemies
 */
public final class DamageToFullHealthBonus implements ArmorSetBonus {

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
        return "damage_to_full_health";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getDamageBonus(ConfigurationSection config) {
        return config.getDouble("damage_bonus", 0.20);
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
}
