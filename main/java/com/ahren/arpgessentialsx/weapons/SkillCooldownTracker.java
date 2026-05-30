package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.scheduler.BukkitRunnable;
import com.ahren.arpgessentialsx.util.ColorUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared cooldown state for weapon skills.
 *
 * Extracted from WeaponSkillListener so that CooldownReductionPassive
 * can reduce active cooldowns without needing a direct reference to the listener.
 *
 * WeaponSkillListener registers cooldowns here.
 * CooldownReductionPassive calls reduceCooldown() here.
 */
public final class SkillCooldownTracker {

    private final ARPGEssentialsX plugin;

    /** "uuid-weaponId" → active cooldown task */
    private final Map<String, BukkitRunnable> cooldowns = new ConcurrentHashMap<>();

    /** "uuid-weaponId" → remaining ticks when task was started */
    private final Map<String, long[]> cooldownEndTick = new ConcurrentHashMap<>();

    public SkillCooldownTracker(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(UUID uuid, String weaponId) {
        return cooldowns.containsKey(key(uuid, weaponId));
    }

    /**
     * Starts a cooldown for a player's weapon skill.
     */
    public void startCooldown(UUID uuid, String weaponId, double seconds, Weapon weapon) {
        String k = key(uuid, weaponId);
        long endTick = plugin.getServer().getCurrentTick() + (long)(seconds * 20);
        cooldownEndTick.put(k, new long[]{endTick});

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(k);
                cooldownEndTick.remove(k);
                var player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    ColorUtil.sendActionBar(player,
                            "&a" + ColorUtil.translate(weapon.getSkillName()) + " &ais ready!");
                }
            }
        };
        cooldowns.put(k, task);
        task.runTaskLater(plugin, (long)(seconds * 20));
    }

    /**
     * Reduces an active cooldown by a flat number of seconds.
     * If the reduction would make it expire, fires it immediately.
     */
    public void reduceCooldown(UUID uuid, String weaponId, double reductionSeconds) {
        String k = key(uuid, weaponId);
        BukkitRunnable existing = cooldowns.get(k);
        if (existing == null) return;

        long[] endTick = cooldownEndTick.get(k);
        if (endTick == null) return;

        long now = plugin.getServer().getCurrentTick();
        long remaining = endTick[0] - now;
        long reduction = (long)(reductionSeconds * 20);
        long newRemaining = Math.max(1, remaining - reduction);

        // Cancel old task and start a shorter one
        existing.cancel();
        cooldowns.remove(k);

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null) return;

        startCooldown(uuid, weaponId, newRemaining / 20.0, weapon);
    }

    public void clearPlayer(UUID uuid) {
        cooldowns.entrySet().removeIf(e -> {
            if (e.getKey().startsWith(uuid.toString())) {
                e.getValue().cancel();
                return true;
            }
            return false;
        });
        cooldownEndTick.keySet().removeIf(k -> k.startsWith(uuid.toString()));
    }

    /**
     * Reduces all active cooldowns for a player by a specified amount.
     * Used by set bonuses that reduce cooldowns on kill.
     */
    public void reduceAllCooldowns(UUID uuid, double reductionSeconds) {
        cooldowns.entrySet().removeIf(e -> {
            if (e.getKey().startsWith(uuid.toString())) {
                String weaponId = e.getKey().substring(uuid.toString().length() + 1);
                reduceCooldown(uuid, weaponId, reductionSeconds);
                return false; // Don't remove, reduceCooldown handles it
            }
            return false;
        });
    }

    private String key(UUID uuid, String weaponId) {
        return uuid + "-" + weaponId;
    }
}