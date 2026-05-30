package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that reduces ability cooldowns on killing enemies.
 *
 * yml params:
 *   cooldown_reduction: 1.5   (seconds to reduce)
 *   internal_cooldown: 4.0   (seconds between triggers)
 *
 * Trigger: 4-piece set completion, reduces cooldowns on kill
 */
public final class CooldownReductionOnKillBonus implements ArmorSetBonus {

    private static final Map<UUID, Boolean> activePlayers = new HashMap<>();
    private static final Map<UUID, Long> lastTriggerTime = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        activePlayers.put(player.getUniqueId(), true);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        activePlayers.remove(player.getUniqueId());
        lastTriggerTime.remove(player.getUniqueId());
    }

    @Override
    public String getType() {
        return "cooldown_reduction_on_kill";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getCooldownReduction(UUID uuid) {
        return 1.5; // Default 1.5 seconds
    }

    public static double getInternalCooldown(UUID uuid) {
        return 4.0; // Default 4 seconds
    }

    public static boolean canTrigger(UUID uuid) {
        Long lastTime = lastTriggerTime.get(uuid);
        if (lastTime == null) return true;
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTime) >= (getInternalCooldown(uuid) * 1000);
    }

    public static void recordTrigger(UUID uuid) {
        lastTriggerTime.put(uuid, System.currentTimeMillis());
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
        lastTriggerTime.remove(uuid);
    }
}
