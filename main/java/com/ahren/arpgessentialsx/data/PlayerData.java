package com.ahren.arpgessentialsx.data;

import java.util.UUID;

/**
 * Holds the RPG data for a single player.
 *
 * Like RPGClass, this is a pure data object. It doesn't DO anything —
 * it just remembers "this UUID has this class."
 *
 * It's immutable (all fields are final) so it's safe to pass around
 * anywhere in the code without worrying about accidental changes.
 */
public final class PlayerData {

    /** The player's unique identifier */
    private final UUID uuid;

    /**
     * The ID of the player's chosen class (e.g., "fighter").
     * NULL means the player hasn't picked a class yet.
     *
     * Using null instead of an empty string is intentional:
     *   - null = "no choice made" (show class selection GUI)
     *   - "fighter" = "chose fighter" (apply fighter attributes)
     */
    private final String classId;

    /**
     * Serialized active set bonuses for persistence (optional, for reference).
     * Format: "setName_pieces" (e.g., "gladiators_finale_2pc")
     * This is used to track which set bonuses were active on logout,
     * but actual re-application is based on currently equipped armor.
     */
    private final String activeSetBonuses;

    /**
     * Creates a new PlayerData.
     *
     * @param uuid    The player's UUID
     * @param classId The class ID, or null if none selected
     */
    public PlayerData(UUID uuid, String classId) {
        this.uuid = uuid;
        this.classId = classId;
        this.activeSetBonuses = null;
    }

    /**
     * Creates a new PlayerData with set bonus persistence.
     *
     * @param uuid             The player's UUID
     * @param classId          The class ID, or null if none selected
     * @param activeSetBonuses Serialized active set bonuses (optional)
     */
    public PlayerData(UUID uuid, String classId, String activeSetBonuses) {
        this.uuid = uuid;
        this.classId = classId;
        this.activeSetBonuses = activeSetBonuses;
    }

    /** @return The player's UUID */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return The class ID this player has selected, or null if none.
     */
    public String getClassId() {
        return classId;
    }

    /**
     * @return true if the player has selected a class (classId is not null)
     */
    public boolean hasClass() {
        return classId != null && !classId.isEmpty();
    }

    /**
     * @return Serialized active set bonuses, or null if none
     */
    public String getActiveSetBonuses() {
        return activeSetBonuses;
    }
}