package com.ahren.arpgessentialsx.party;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory manager for all active parties.
 *
 * Holds:
 *   - All active Party objects, keyed by party UUID
 *   - A reverse lookup: playerUUID → partyUUID
 *   - Pending invites:  invitedUUID  → leaderUUID
 *   - Pending requests: requesterUUID → leaderUUID
 *
 * Parties are session-only — nothing persisted to disk.
 */
public final class PartyManager {

    private final ARPGEssentialsX plugin;

    private final Map<UUID, Party> parties          = new ConcurrentHashMap<>();
    private final Map<UUID, UUID>  playerToParty    = new ConcurrentHashMap<>();
    private final Map<UUID, UUID>  pendingInvites   = new ConcurrentHashMap<>();
    private final Map<UUID, UUID>  pendingRequests  = new ConcurrentHashMap<>();

    public PartyManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpired, 1200L, 1200L);
    }

    // ── Party lifecycle ───────────────────────────────────────────────────────

    public Party createParty(UUID leaderUUID, String leaderName) {
        if (playerToParty.containsKey(leaderUUID)) return null;
        Party party = new Party(leaderUUID, leaderName + "'s Party");
        parties.put(party.getPartyId(), party);
        playerToParty.put(leaderUUID, party.getPartyId());
        return party;
    }

    public void disbandParty(UUID partyId) {
        Party party = parties.remove(partyId);
        if (party == null) return;
        for (UUID member : party.getMembers()) {
            playerToParty.remove(member);
        }
    }

    // ── Membership ───────────────────────────────────────────────────────────

    public boolean joinParty(UUID playerUUID, UUID partyId) {
        if (playerToParty.containsKey(playerUUID)) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;
        if (!party.addMember(playerUUID)) return false;
        playerToParty.put(playerUUID, partyId);
        return true;
    }

    public void leaveParty(UUID playerUUID) {
        UUID partyId = playerToParty.remove(playerUUID);
        if (partyId == null) return;
        Party party = parties.get(partyId);
        if (party == null) return;
        party.removeMember(playerUUID);
        if (party.size() == 0) parties.remove(partyId);
    }

    /**
     * Removes only the reverse lookup entry for a player without removing
     * them from the party's member list.
     *
     * Used when a player disconnects — we keep them in the party so they
     * can rejoin and have their HUD restored, but we remove the lookup so
     * they can't accidentally create a new party while offline.
     *
     * @param playerUUID The disconnecting player's UUID
     */
    public void removePlayerLookup(UUID playerUUID) {
        playerToParty.remove(playerUUID);
    }

    /**
     * Restores the reverse lookup for a player who has rejoined.
     * Called by PlayerJoinListener after confirming they're still
     * in an active party.
     *
     * @param playerUUID The rejoining player's UUID
     * @param partyId    The party they belong to
     */
    public void restorePlayerLookup(UUID playerUUID, UUID partyId) {
        playerToParty.put(playerUUID, partyId);
    }

    // ── Lookups ───────────────────────────────────────────────────────────────

    /**
     * Gets the party a player belongs to.
     * Works by scanning all parties for membership when the reverse lookup
     * is absent (e.g. player disconnected and lookup was removed).
     */
    public Party getPartyOf(UUID playerUUID) {
        // Fast path — reverse lookup exists
        UUID partyId = playerToParty.get(playerUUID);
        if (partyId != null) return parties.get(partyId);

        // Slow path — scan all parties (handles disconnect/reconnect case)
        for (Party party : parties.values()) {
            if (party.isMember(playerUUID)) return party;
        }
        return null;
    }

    public boolean inSameParty(UUID a, UUID b) {
        UUID partyA = playerToParty.get(a);
        if (partyA == null) return false;
        return partyA.equals(playerToParty.get(b));
    }

    public Collection<Party> getAllParties() {
        return Collections.unmodifiableCollection(parties.values());
    }

    // ── Invites ──────────────────────────────────────────────────────────────

    public void addInvite(UUID invitedUUID, UUID leaderUUID) {
        pendingInvites.put(invitedUUID, leaderUUID);
    }

    public UUID getInviteLeader(UUID invitedUUID) {
        return pendingInvites.get(invitedUUID);
    }

    public void removeInvite(UUID invitedUUID) {
        pendingInvites.remove(invitedUUID);
    }

    // ── Join Requests ─────────────────────────────────────────────────────────

    public void addRequest(UUID requesterUUID, UUID leaderUUID) {
        pendingRequests.put(requesterUUID, leaderUUID);
    }

    public UUID getPendingRequesterFor(UUID leaderUUID) {
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(leaderUUID)) return entry.getKey();
        }
        return null;
    }

    public void removeRequest(UUID requesterUUID) {
        pendingRequests.remove(requesterUUID);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private void cleanupExpired() {
        pendingInvites.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        pendingRequests.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }
}