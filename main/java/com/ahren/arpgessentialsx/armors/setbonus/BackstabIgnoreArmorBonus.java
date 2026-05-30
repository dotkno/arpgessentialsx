package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that causes attacks from behind or while invisible to ignore armor and deal true damage.
 *
 * yml params:
 *   armor_ignore_percentage: 0.30   (30% armor ignore)
 *   true_damage_percentage: 0.50   (50% of damage as true damage)
 *
 * Trigger: 4-piece set completion, enhances backstab/invisible attacks
 */
public final class BackstabIgnoreArmorBonus implements ArmorSetBonus {

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
        return "backstab_ignore_armor";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getArmorIgnorePercentage(ConfigurationSection config) {
        return config.getDouble("armor_ignore_percentage", 0.30);
    }

    public static double getTrueDamagePercentage(ConfigurationSection config) {
        return config.getDouble("true_damage_percentage", 0.50);
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
}
