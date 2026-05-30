package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Listens for players joining the server.
 *
 * On join:
 *   - Applies class attributes (via AuthMe or directly)
 *   - Restores party HUD and reverse lookup if still in an active party
 */
public final class PlayerJoinListener implements Listener {

    private final ARPGEssentialsX plugin;

    public PlayerJoinListener(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean authMeEnabled = Bukkit.getPluginManager().getPlugin("AuthMe") != null;

        if (authMeEnabled) {
            plugin.getLogger().fine("Player " + player.getName()
                    + " joined. AuthMe detected — waiting for login.");
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.applyClassToPlayer(player);

                restorePartyState(player);
                restoreSetBonuses(player);
            }, 1L);
        }
    }

    /**
     * Restores set bonuses for a rejoining player by recalculating
     * based on their currently equipped armor.
     */
    private void restoreSetBonuses(Player player) {
        plugin.getArmorEquipListener().getSetBonusManager().restorePlayerSetBonuses(player);
    }

    /**
     * Restores party state for a rejoining player:
     *   1. Finds their party via the slow-path scan in PartyManager
     *   2. Restores their reverse lookup so inSameParty() works again
     *   3. Shows the HUD
     *   4. Notifies remaining members they're back
     */
    private void restorePartyState(Player player) {
        UUID uuid = player.getUniqueId();
        Party party = plugin.getPartyManager().getPartyOf(uuid);
        if (party == null) return;

        // Restore reverse lookup (removed on disconnect)
        plugin.getPartyManager().restorePlayerLookup(uuid, party.getPartyId());

        // Restore HUD
        plugin.getPartyHUDManager().show(player);

        // Notify remaining online members
        for (UUID memberUUID : party.getMembers()) {
            if (memberUUID.equals(uuid)) continue;
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(ColorUtil.translate(
                        "&e" + player.getName() + " &7has rejoined the party."));
            }
        }
    }
}