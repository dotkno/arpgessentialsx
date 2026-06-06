package com.ahren.arpgessentialsx.util;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages boss bar cooldown indicators for spells, weapon skills, and relics.
 *
 * Each player can have multiple stacked boss bars showing cooldown progress for
 * different abilities. The bars progress sync with the remaining cooldown time.
 */
public final class BossBarCooldownManager {

    private final ARPGEssentialsX plugin;

    /** Maps "uuid-abilityName" to the boss bar */
    private final Map<String, BossBar> activeBossBars = new ConcurrentHashMap<>();

    /** Maps "uuid-abilityName" to the task that updates the boss bar */
    private final Map<String, BukkitRunnable> updateTasks = new ConcurrentHashMap<>();

    /** Maps "uuid-abilityName" to the cooldown end time (in milliseconds) */
    private final Map<String, Long> cooldownEndTimes = new ConcurrentHashMap<>();

    /** Maps "uuid-abilityName" to the total duration (in milliseconds) */
    private final Map<String, Long> totalDurations = new ConcurrentHashMap<>();

    public BossBarCooldownManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    private String getKey(UUID uuid, String abilityName) {
        return uuid + "-" + abilityName;
    }

    /**
     * Starts a cooldown boss bar for a player.
     *
     * @param player The player to show the boss bar to
     * @param abilityName The name of the ability on cooldown
     * @param cooldownSeconds The cooldown duration in seconds
     */
    public void startCooldown(Player player, String abilityName, double cooldownSeconds) {
        UUID uuid = player.getUniqueId();
        String key = getKey(uuid, abilityName);

        // Remove any existing boss bar for this specific ability
        removeCooldown(uuid, abilityName);

        // Calculate end time
        long endTime = System.currentTimeMillis() + (long) (cooldownSeconds * 1000);
        long totalDuration = (long) (cooldownSeconds * 1000);
        cooldownEndTimes.put(key, endTime);
        totalDurations.put(key, totalDuration);

        // Create boss bar
        BossBar bossBar = Bukkit.createBossBar(
            ColorUtil.translate(abilityName + " Cooldown"),
            BarColor.PURPLE,
            BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);
        activeBossBars.put(key, bossBar);

        // Start update task
        BukkitRunnable updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    removeCooldown(uuid, abilityName);
                    cancel();
                    return;
                }

                Long endTime = cooldownEndTimes.get(key);
                if (endTime == null) {
                    removeCooldown(uuid, abilityName);
                    cancel();
                    return;
                }

                Long totalDuration = totalDurations.get(key);
                if (totalDuration == null) {
                    removeCooldown(uuid, abilityName);
                    cancel();
                    return;
                }

                long remaining = endTime - System.currentTimeMillis();

                if (remaining <= 0) {
                    // Cooldown finished
                    removeCooldown(uuid, abilityName);
                    cancel();
                    return;
                }

                // Update progress
                double progress = Math.max(0.0, Math.min(1.0, (double) remaining / totalDuration));
                bossBar.setProgress(progress);

                // Update color based on remaining time
                double remainingSeconds = remaining / 1000.0;
                double cooldownSeconds = totalDuration / 1000.0;
                if (remainingSeconds <= 1.0) {
                    bossBar.setColor(BarColor.GREEN);
                } else if (remainingSeconds <= cooldownSeconds * 0.3) {
                    bossBar.setColor(BarColor.YELLOW);
                } else {
                    bossBar.setColor(BarColor.PURPLE);
                }
            }
        };

        updateTasks.put(key, updateTask);
        updateTask.runTaskTimer(plugin, 0L, 1L); // Update every tick
    }

    /**
     * Removes the cooldown boss bar for a specific ability.
     *
     * @param uuid The player's UUID
     * @param abilityName The name of the ability
     */
    public void removeCooldown(UUID uuid, String abilityName) {
        String key = getKey(uuid, abilityName);

        // Cancel update task
        BukkitRunnable task = updateTasks.remove(key);
        if (task != null) {
            task.cancel();
        }

        // Remove boss bar
        BossBar bossBar = activeBossBars.remove(key);
        if (bossBar != null) {
            bossBar.removeAll();
        }

        // Clean up maps
        cooldownEndTimes.remove(key);
        totalDurations.remove(key);
    }

    /**
     * Cleans up all data for a player (called on disconnect).
     *
     * @param uuid The player's UUID
     */
    public void cleanupPlayer(UUID uuid) {
        // Remove all cooldowns for this player
        activeBossBars.keySet().removeIf(key -> key.startsWith(uuid.toString()));
        updateTasks.keySet().removeIf(key -> {
            if (key.startsWith(uuid.toString())) {
                BukkitRunnable task = updateTasks.get(key);
                if (task != null) task.cancel();
                return true;
            }
            return false;
        });
        cooldownEndTimes.keySet().removeIf(key -> key.startsWith(uuid.toString()));
        totalDurations.keySet().removeIf(key -> key.startsWith(uuid.toString()));
    }

    /**
     * Checks if a player currently has an active cooldown boss bar for a specific ability.
     *
     * @param uuid The player's UUID
     * @param abilityName The name of the ability
     * @return true if the player has an active cooldown boss bar for this ability
     */
    public boolean hasActiveCooldown(UUID uuid, String abilityName) {
        return activeBossBars.containsKey(getKey(uuid, abilityName));
    }
}
