package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that regenerates health over time, pausing when damaged.
 *
 * yml params:
 *   regen_percentage: 0.03   (3% of max health)
 *   regen_interval: 6.0   (seconds between regen ticks)
 *   damage_pause_duration: 3.0   (seconds to pause after taking damage)
 *
 * Trigger: 4-piece set completion, regenerates health periodically
 */
public final class HealthRegenBonus implements ArmorSetBonus {

    private static final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();
    private static final Map<UUID, Long> lastDamageTime = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        double regenPercentage = config.getDouble("regen_percentage", 0.03);
        double regenInterval = config.getDouble("regen_interval", 6.0);
        double damagePauseDuration = config.getDouble("damage_pause_duration", 3.0);

        UUID uuid = player.getUniqueId();
        
        // Cancel existing task if any
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(uuid);
                    lastDamageTime.remove(uuid);
                    return;
                }

                // Check if recently damaged
                Long lastDamage = lastDamageTime.get(uuid);
                long currentTime = System.currentTimeMillis();
                
                if (lastDamage != null && (currentTime - lastDamage) < (damagePauseDuration * 1000)) {
                    return; // Pause regen if recently damaged
                }

                // Apply regen
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double healAmount = maxHealth * regenPercentage;
                double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
                player.setHealth(newHealth);
            }
        };

        task.runTaskTimer(com.ahren.arpgessentialsx.ARPGEssentialsX.getPlugin(com.ahren.arpgessentialsx.ARPGEssentialsX.class), 
                         0L, (long)(regenInterval * 20L));
        activeTasks.put(uuid, task);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        
        // Cancel task
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }

        // Clear damage tracking
        lastDamageTime.remove(uuid);
    }

    @Override
    public String getType() {
        return "health_regen";
    }

    public static boolean isActive(UUID uuid) {
        return activeTasks.containsKey(uuid);
    }

    public static void recordDamage(UUID uuid) {
        lastDamageTime.put(uuid, System.currentTimeMillis());
    }

    public static void clearPlayer(UUID uuid) {
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }
        lastDamageTime.remove(uuid);
    }
}
