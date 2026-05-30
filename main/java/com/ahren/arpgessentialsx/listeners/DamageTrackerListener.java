package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Simply records the last time each player took damage.
 * This data is used by the Fighter's out-of-combat regeneration passive.
 *
 * We use LOW priority so this runs before any damage reduction/healing plugins.
 * We only care THAT damage happened, not the final amount.
 */
public final class DamageTrackerListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final Map<UUID, Long> lastDamageTime;

    public DamageTrackerListener(ARPGEssentialsX plugin, Map<UUID, Long> lastDamageTime) {
        this.plugin = plugin;
        this.lastDamageTime = lastDamageTime;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Only track players who actually have a class
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data != null && data.hasClass()) {
            lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
}