package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages set bonus tracking and application for players.
 * Uses independent set evaluation with thread-safe armor piece count tracking.
 */
public class SetBonusManager {

    private final ARPGEssentialsX plugin;
    private final ArmorManager armorManager;

    // Track armor piece counts per player: UUID -> Map<SetID -> Count>
    private final Map<UUID, Map<String, Integer>> playerArmorCounts = new ConcurrentHashMap<>();

    public SetBonusManager(ARPGEssentialsX plugin, ArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
    }

    /**
     * Updates set bonuses based on currently equipped armor.
     * Uses independent set evaluation comparing old vs new piece counts.
     */
    public void updateSetBonuses(Player player, Map<ArmorType, Armor> equippedArmor) {
        UUID uuid = player.getUniqueId();

        // Calculate NEW armor piece count per set from current gear
        Map<String, Integer> newCounts = new HashMap<>();
        Map<String, Armor> representativeArmor = new HashMap<>();

        for (Armor armor : equippedArmor.values()) {
            if (armor.hasSetName()) {
                String setName = armor.getSetName();
                newCounts.merge(setName, 1, Integer::sum);
                representativeArmor.putIfAbsent(setName, armor);
            }
        }

        // Retrieve OLD armor piece count map for that player
        Map<String, Integer> oldCounts = playerArmorCounts.get(uuid);
        if (oldCounts == null) {
            oldCounts = new HashMap<>();
        }

        // Get all possible Armor Set IDs from the system
        Map<String, Armor> allArmors = new HashMap<>();
        for (Armor armor : armorManager.getAllArmors()) {
            if (armor.hasSetName()) {
                allArmors.put(armor.getSetName(), armor);
            }
        }

        // Loop through every possible Armor Set ID and compare independently
        for (String setID : allArmors.keySet()) {
            int oldCount = oldCounts.getOrDefault(setID, 0);
            int newCount = newCounts.getOrDefault(setID, 0);
            Armor armor = allArmors.get(setID);

            // OLD count >= 2 AND NEW count < 2 -> Remove 2-piece bonus
            if (oldCount >= 2 && newCount < 2 && armor.hasTwoPieceBonus()) {
                remove2PieceBonus(player, setID, armor.getTwoPieceBonusConfig());
            }

            // OLD count >= 4 AND NEW count < 4 -> Remove 4-piece bonus
            if (oldCount >= 4 && newCount < 4 && armor.hasFourPieceBonus()) {
                remove4PieceBonus(player, setID, armor.getFourPieceBonusConfig());
            }

            // OLD count < 2 AND NEW count >= 2 -> Apply 2-piece bonus
            if (oldCount < 2 && newCount >= 2 && armor.hasTwoPieceBonus()) {
                apply2PieceBonus(player, setID, armor.getTwoPieceBonusConfig());
            }

            // OLD count < 4 AND NEW count >= 4 -> Apply 4-piece bonus
            if (oldCount < 4 && newCount >= 4 && armor.hasFourPieceBonus()) {
                apply4PieceBonus(player, setID, armor.getFourPieceBonusConfig());
            }
        }

        // Update playerArmorCounts with the NEW count map
        playerArmorCounts.put(uuid, newCounts);
    }

    private void apply2PieceBonus(Player player, String setID, ConfigurationSection config) {
        if (config == null) return;
        plugin.getLogger().info("[SetBonusManager] Applying 2-piece bonus for set " + setID + " to player " + player.getName());
        armorManager.getSetBonusRegistry().applyBonus(player, config, 2, setID);
    }

    private void apply4PieceBonus(Player player, String setID, ConfigurationSection config) {
        if (config == null) return;
        plugin.getLogger().info("[SetBonusManager] Applying 4-piece bonus for set " + setID + " to player " + player.getName());
        armorManager.getSetBonusRegistry().applyBonus(player, config, 4, setID);
    }

    private void remove2PieceBonus(Player player, String setID, ConfigurationSection config) {
        if (config == null) return;
        plugin.getLogger().info("[SetBonusManager] Removing 2-piece bonus for set " + setID + " from player " + player.getName());
        armorManager.getSetBonusRegistry().removeBonus(player, config, 2, setID);
    }

    private void remove4PieceBonus(Player player, String setID, ConfigurationSection config) {
        if (config == null) return;
        plugin.getLogger().info("[SetBonusManager] Removing 4-piece bonus for set " + setID + " from player " + player.getName());
        armorManager.getSetBonusRegistry().removeBonus(player, config, 4, setID);
    }

    /**
     * Saves the player's armor counts to the database.
     * Called on logout to persist the current state.
     */
    public void savePlayerArmorCounts(UUID uuid) {
        Map<String, Integer> counts = playerArmorCounts.get(uuid);
        if (counts == null || counts.isEmpty()) {
            plugin.getPlayerDataManager().setActiveSetBonuses(uuid, null);
            return;
        }

        // Serialize armor counts as comma-separated "SetID:count" strings
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        plugin.getPlayerDataManager().setActiveSetBonuses(uuid, sb.toString());
    }

    /**
     * Loads and re-applies set bonuses for a player based on their currently equipped armor.
     * Called on login to restore set bonuses.
     */
    public void restorePlayerSetBonuses(Player player) {
        // Load saved data (optional, for reference/debugging)
        String savedCounts = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getActiveSetBonuses();
        if (savedCounts != null && !savedCounts.isEmpty()) {
            plugin.getLogger().info("[SetBonusManager] Restoring set bonuses for " + player.getName() + " (saved: " + savedCounts + ")");
        }

        // Re-apply based on currently equipped armor
        plugin.getArmorEquipListener().scanAndEquipArmor(player);
    }

    /**
     * Clears all set bonus modifiers for a player.
     * Called on logout to strip all modifiers.
     */
    public void stripAllModifiers(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> counts = playerArmorCounts.get(uuid);
        if (counts == null || counts.isEmpty()) return;

        // Get all possible Armor Set IDs from the system
        for (Armor armor : armorManager.getAllArmors()) {
            if (!armor.hasSetName()) continue;
            String setID = armor.getSetName();

            // Remove 2-piece bonus if it was active
            if (counts.getOrDefault(setID, 0) >= 2 && armor.hasTwoPieceBonus()) {
                remove2PieceBonus(player, setID, armor.getTwoPieceBonusConfig());
            }

            // Remove 4-piece bonus if it was active
            if (counts.getOrDefault(setID, 0) >= 4 && armor.hasFourPieceBonus()) {
                remove4PieceBonus(player, setID, armor.getFourPieceBonusConfig());
            }
        }
    }

    public void clearPlayerData(UUID uuid) {
        playerArmorCounts.remove(uuid);
    }
}
