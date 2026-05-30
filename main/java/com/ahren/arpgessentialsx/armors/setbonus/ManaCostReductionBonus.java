package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that reduces mana cost when casting spells.
 *
 * yml params:
 *   mana_cost_reduction: 0.15   (15% mana cost reduction)
 *
 * Trigger: 4-piece set completion, reduces mana cost on spell cast
 */
public final class ManaCostReductionBonus implements ArmorSetBonus {

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
        return "mana_cost_reduction";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getManaCostReduction(UUID uuid) {
        return 0.15; // Default 15% reduction, can be made configurable
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
}
