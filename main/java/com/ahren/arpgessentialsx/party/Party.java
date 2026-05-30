package com.ahren.arpgessentialsx.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single active party.
 *
 * Pure data class — holds state only, does nothing on its own.
 * Maximum 8 members including the leader.
 */
public final class Party {

    public static final int MAX_SIZE = 8;

    private final UUID partyId;

    /**
     * The current leader UUID. Mutable — leadership can be transferred
     * when the leader disconnects.
     */
    private UUID leaderUUID;

    /**
     * All members including the leader.
     * Leader is always members.get(0).
     */
    private final List<UUID> members;

    private String name;

    public Party(UUID leaderUUID, String defaultName) {
        this.partyId    = UUID.randomUUID();
        this.leaderUUID = leaderUUID;
        this.name       = defaultName;
        this.members    = new ArrayList<>();
        this.members.add(leaderUUID);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getPartyId()           { return partyId; }
    public UUID getLeaderUUID()        { return leaderUUID; }
    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    public List<UUID> getMembers() {
        return Collections.unmodifiableList(members);
    }

    // ── Membership ───────────────────────────────────────────────────────────

    public boolean addMember(UUID uuid) {
        if (members.size() >= MAX_SIZE) return false;
        if (members.contains(uuid)) return false;
        members.add(uuid);
        return true;
    }

    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }

    public boolean isMember(UUID uuid)  { return members.contains(uuid); }
    public boolean isLeader(UUID uuid)  { return leaderUUID.equals(uuid); }
    public boolean isFull()             { return members.size() >= MAX_SIZE; }
    public int size()                   { return members.size(); }

    // ── Leadership Transfer ───────────────────────────────────────────────────

    /**
     * Transfers leadership to the next oldest member (index 1 after leader
     * is removed, or whoever is first if leader hasn't been removed yet).
     *
     * Called when the leader disconnects. The old leader is removed from
     * the members list first, then this promotes whoever is now at index 0.
     *
     * @return The UUID of the new leader, or null if no members remain
     */
    public UUID transferLeadershipToNext() {
        // Remove old leader from list
        members.remove(leaderUUID);

        if (members.isEmpty()) return null;

        // Promote whoever is now first (oldest remaining member)
        leaderUUID = members.get(0);
        return leaderUUID;
    }
}