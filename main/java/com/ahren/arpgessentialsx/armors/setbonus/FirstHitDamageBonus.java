package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that increases damage against enemies at full health.
 *
 * yml params:
 *   damage_bonus: 0.20   (20% increased damage)
 *
 * Trigger: 4-piece set completion, bonus damage on first hit vs full HP enemies
 */
public final class FirstHitDamageBonus implements ArmorSetBonus {

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
        return "first_hit_damage";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getDamageBonus(UUID uuid) {
        return 0.20; // Default 20% bonus
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
}
