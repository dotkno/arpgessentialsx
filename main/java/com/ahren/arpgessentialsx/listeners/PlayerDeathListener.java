package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Handles player death and respawn events to ensure set bonuses are properly cleared.
 * This prevents phantom buffs from persisting after death.
 */
public class PlayerDeathListener implements Listener {

    private final ARPGEssentialsX plugin;

    public PlayerDeathListener(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Strip all arpgessentialsx modifiers from the player
        plugin.getArmorEquipListener().getSetBonusManager().stripAllModifiers(player);

        plugin.getLogger().info("[PlayerDeathListener] Stripped set bonuses from player " + player.getName() + " on death");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Strip all arpgessentialsx modifiers from the player (double-check)
        plugin.getArmorEquipListener().getSetBonusManager().stripAllModifiers(player);
        
        // Re-apply set bonuses based on currently equipped armor
        plugin.getArmorEquipListener().getSetBonusManager().restorePlayerSetBonuses(player);
        
        plugin.getLogger().info("[PlayerDeathListener] Re-applied set bonuses for player " + player.getName() + " on respawn");
    }
}
