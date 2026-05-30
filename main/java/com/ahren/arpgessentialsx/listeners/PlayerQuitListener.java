package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.SetBonusEventListener;
import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.party.PartyManager;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles players disconnecting from the server.
 *
 * Two scenarios:
 *
 * 1. Non-leader disconnects:
 *    - Remove them from the party silently
 *    - Notify remaining members
 *    - If party is now empty, disband it
 *
 * 2. Leader disconnects:
 *    - Transfer leadership to the next oldest member
 *    - Notify remaining members of the new leader
 *    - If no members remain, disband
 *
 * The player's party membership is intentionally KEPT in PartyManager
 * so that if they rejoin mid-session, PlayerJoinListener can restore
 * their HUD. Their UUID stays in the party until either:
 *   - They explicitly leave (/arpg party leave)
 *   - The party disbands
 *   - The server restarts (parties are session-only)
 */
public final class PlayerQuitListener implements Listener {

    private final ARPGEssentialsX plugin;

    public PlayerQuitListener(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Save set bonuses to database
        plugin.getArmorEquipListener().getSetBonusManager().savePlayerSetBonuses(uuid);

        // Cleanup set bonus state
        plugin.getSetBonusEventListener().clearPlayer(uuid);

        Party party = plugin.getPartyManager().getPartyOf(uuid);
        if (party == null) return; // Not in a party — nothing to do

        // Hide HUD for the disconnecting player
        // (They're going offline so this is mainly cleanup)
        plugin.getPartyHUDManager().hide(player);

        if (party.isLeader(uuid)) {
            handleLeaderQuit(player, party);
        } else {
            handleMemberQuit(player, party);
        }
    }

    // ── Leader quit ───────────────────────────────────────────────────────────

    private void handleLeaderQuit(Player leader, Party party) {
        // Transfer leadership to next oldest member
        UUID newLeaderUUID = party.transferLeadershipToNext();

        if (newLeaderUUID == null) {
            // No members left — disband
            plugin.getPartyManager().disbandParty(party.getPartyId());
            return;
        }

        // Also remove from the PartyManager's reverse lookup so they
        // can't accidentally create a new party while "still" in this one
        // NOTE: We do NOT call leaveParty() because that would remove them
        // from the party entirely. We only want to remove the leader's
        // playerToParty entry so they can rejoin cleanly later.
        plugin.getPartyManager().removePlayerLookup(leader.getUniqueId());

        // Notify remaining members
        Player newLeader = Bukkit.getPlayer(newLeaderUUID);
        String newLeaderName = newLeader != null ? newLeader.getName()
                : Bukkit.getOfflinePlayer(newLeaderUUID).getName();
        if (newLeaderName == null) newLeaderName = "a member";

        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;

            member.sendMessage(ColorUtil.translate(
                    "&e" + leader.getName() + " &7has left. "
                            + "&f" + newLeaderName + " &7is now the party leader."));
        }
    }

    // ── Member quit ───────────────────────────────────────────────────────────

    private void handleMemberQuit(Player member, Party party) {
        // Keep them in the party (they may rejoin) but remove the active
        // lookup so they can't create a new party while offline
        plugin.getPartyManager().removePlayerLookup(member.getUniqueId());

        // Notify remaining online members
        for (UUID memberUUID : party.getMembers()) {
            if (memberUUID.equals(member.getUniqueId())) continue;
            Player other = Bukkit.getPlayer(memberUUID);
            if (other != null) {
                other.sendMessage(ColorUtil.translate(
                        "&e" + member.getName() + " &7has disconnected."));
            }
        }
    }
}