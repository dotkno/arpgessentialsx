package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that increases the damage of your next spell after casting.
 *
 * yml params:
 *   damage_increase: 0.15   (15% increase per stack)
 *   max_stacks: 3
 *   mana_refund_percentage: 0.10   (10% mana refund on consume)
 *   cooldown_reduction_percentage: 0.05   (5% cooldown reduction on consume)
 *
 * Trigger: 4-piece set completion, amplifies spell damage
 */
public final class SpellDamageAmplificationBonus implements ArmorSetBonus {

    private static final Map<UUID, Integer> playerStacks = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        playerStacks.put(player.getUniqueId(), 0);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        playerStacks.remove(player.getUniqueId());
    }

    @Override
    public String getType() {
        return "spell_damage_amplification";
    }

    public static void onSpellCast(Player player, ConfigurationSection config) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        int maxStacks = config.getInt("max_stacks", 3);
        int currentStacks = playerStacks.get(uuid);
        
        if (currentStacks < maxStacks) {
            playerStacks.put(uuid, currentStacks + 1);
        }
    }

    public static double getDamageMultiplier(Player player, ConfigurationSection config) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return 1.0;

        int stacks = playerStacks.get(uuid);
        double increase = config.getDouble("damage_increase", 0.15);
        
        return 1.0 + (stacks * increase);
    }

    public static void consumeStacks(Player player, ConfigurationSection config) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        playerStacks.put(uuid, 0);
        
        // Apply mana refund and cooldown reduction
        double manaRefund = config.getDouble("mana_refund_percentage", 0.10);
        double cdReduction = config.getDouble("cooldown_reduction_percentage", 0.05);
        
        // Note: These would need integration with a mana/cooldown system
        // For now, this is a placeholder for when such systems exist
    }

    public static void clearPlayer(UUID uuid) {
        playerStacks.remove(uuid);
    }
}
