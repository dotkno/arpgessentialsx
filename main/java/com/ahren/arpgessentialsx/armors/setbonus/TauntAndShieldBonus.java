package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that taunts enemies and shields party members on damage taken.
 *
 * yml params:
 *   taunt_radius: 10.0   (blocks)
 *   taunt_duration: 3.0   (seconds)
 *   shield_radius: 12.0   (blocks)
 *   shield_percentage: 0.08   (8% of tank's max HP as shield)
 *   shield_duration: 5.0   (seconds)
 *
 * Trigger: 4-piece set completion, taunts and shields on damage taken
 */
public final class TauntAndShieldBonus implements ArmorSetBonus {

    private static final Map<UUID, Boolean> activePlayers = new HashMap<>();
    private static final Map<UUID, Long> lastTriggerTime = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        activePlayers.put(player.getUniqueId(), true);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        lastTriggerTime.remove(uuid);
    }

    @Override
    public String getType() {
        return "taunt_and_shield";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static boolean canTrigger(UUID uuid, ConfigurationSection config) {
        if (!activePlayers.containsKey(uuid)) return false;
        
        double cooldown = config.getDouble("cooldown", 5.0) * 1000;
        Long lastTime = lastTriggerTime.get(uuid);
        
        if (lastTime == null) return true;
        return System.currentTimeMillis() - lastTime >= cooldown;
    }

    public static void recordTrigger(UUID uuid) {
        lastTriggerTime.put(uuid, System.currentTimeMillis());
    }

    public static double getTauntRadius(ConfigurationSection config) {
        return config.getDouble("taunt_radius", 10.0);
    }

    public static double getTauntDuration(ConfigurationSection config) {
        return config.getDouble("taunt_duration", 3.0);
    }

    public static double getShieldRadius(ConfigurationSection config) {
        return config.getDouble("shield_radius", 12.0);
    }

    public static double getShieldPercentage(ConfigurationSection config) {
        return config.getDouble("shield_percentage", 0.08);
    }

    public static double getShieldDuration(ConfigurationSection config) {
        return config.getDouble("shield_duration", 5.0);
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
        lastTriggerTime.remove(uuid);
    }
}
