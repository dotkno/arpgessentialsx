package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.attributes.ClassAttributeApplier;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages set bonus tracking and application for players.
 * Optimized to only recalculate when armor changes.
 */
public class SetBonusManager {

    private final ARPGEssentialsX plugin;
    private final ArmorManager armorManager;

    // Track active set bonuses per player
    private final Map<UUID, Map<String, SetBonusState>> activeSetBonuses = new HashMap<>();

    public SetBonusManager(ARPGEssentialsX plugin, ArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
    }

    /**
     * Updates set bonuses based on currently equipped armor.
     * Uses incremental approach: removes old bonuses, adds new bonuses.
     */
    public void updateSetBonuses(Player player, Map<ArmorType, Armor> equippedArmor) {
        UUID uuid = player.getUniqueId();

        // Count pieces per set
        Map<String, Integer> setCounts = new HashMap<>();
        Map<String, Armor> representativeArmor = new HashMap<>();

        for (Armor armor : equippedArmor.values()) {
            if (armor.hasSetName()) {
                String setName = armor.getSetName();
                setCounts.merge(setName, 1, Integer::sum);
                representativeArmor.putIfAbsent(setName, armor);
            }
        }

        // Debug logging
        if (!setCounts.isEmpty()) {
            plugin.getLogger().info("[SetBonusManager] Player " + player.getName() + " set counts:");
            for (Map.Entry<String, Integer> entry : setCounts.entrySet()) {
                plugin.getLogger().info("  - " + entry.getKey() + ": " + entry.getValue() + " pieces");
            }
        }

        // Get previous state
        Map<String, SetBonusState> previousState = activeSetBonuses.getOrDefault(uuid, new HashMap<>());

        // Calculate new state
        Map<String, SetBonusState> newState = new HashMap<>();
        for (Map.Entry<String, Integer> entry : setCounts.entrySet()) {
            String setName = entry.getKey();
            int count = entry.getValue();
            Armor armor = representativeArmor.get(setName);

            SetBonusState state = new SetBonusState();
            if (count >= 2 && armor.hasTwoPieceBonus()) {
                state.setTwoPieceBonus(true);
                state.setTwoPieceConfig(armor.getTwoPieceBonusConfig());
            }
            if (count >= 4 && armor.hasFourPieceBonus()) {
                state.setFourPieceBonus(true);
                state.setFourPieceConfig(armor.getFourPieceBonusConfig());
            }
            newState.put(setName, state);
        }

        // Remove bonuses that are no longer active
        for (Map.Entry<String, SetBonusState> entry : previousState.entrySet()) {
            String setName = entry.getKey();
            SetBonusState prevState = entry.getValue();
            SetBonusState newStateForSet = newState.get(setName);

            plugin.getLogger().info("[SetBonusManager] Checking set " + setName + " for removal:");
            plugin.getLogger().info("  - Prev 2pc: " + prevState.hasTwoPieceBonus() + ", 4pc: " + prevState.hasFourPieceBonus());
            plugin.getLogger().info("  - New 2pc: " + (newStateForSet != null && newStateForSet.hasTwoPieceBonus()) + ", 4pc: " + (newStateForSet != null && newStateForSet.hasFourPieceBonus()));

            // Remove 2-piece bonus if no longer active
            if (prevState.hasTwoPieceBonus() && (newStateForSet == null || !newStateForSet.hasTwoPieceBonus())) {
                plugin.getLogger().info("[SetBonusManager] Removing 2-piece bonus for set " + setName);
                removeSetBonus(player, prevState.getTwoPieceConfig(), 2, setName);
            }

            // Remove 4-piece bonus if no longer active
            if (prevState.hasFourPieceBonus() && (newStateForSet == null || !newStateForSet.hasFourPieceBonus())) {
                plugin.getLogger().info("[SetBonusManager] Removing 4-piece bonus for set " + setName);
                removeSetBonus(player, prevState.getFourPieceConfig(), 4, setName);
            }
        }

        // Update state tracking BEFORE adding new bonuses
        activeSetBonuses.put(uuid, newState);

        // Add bonuses that became active
        for (Map.Entry<String, SetBonusState> entry : newState.entrySet()) {
            String setName = entry.getKey();
            SetBonusState state = entry.getValue();
            SetBonusState prevState = previousState.get(setName);

            // Add 2-piece bonus if newly active
            if (state.hasTwoPieceBonus() && (prevState == null || !prevState.hasTwoPieceBonus())) {
                plugin.getLogger().info("[SetBonusManager] Applying 2-piece bonus for set " + setName);
                applySetBonus(player, state.getTwoPieceConfig(), 2, setName);
            }

            // Add 4-piece bonus if newly active
            if (state.hasFourPieceBonus() && (prevState == null || !prevState.hasFourPieceBonus())) {
                plugin.getLogger().info("[SetBonusManager] Applying 4-piece bonus for set " + setName);
                applySetBonus(player, state.getFourPieceConfig(), 4, setName);
            }
        }
   }

    private void applySetBonus(Player player, ConfigurationSection config, int pieces, String setName) {
        if (config == null) return;
        armorManager.getSetBonusRegistry().applyBonus(player, config, pieces, setName);
    }

    private void removeSetBonus(Player player, ConfigurationSection config, int pieces, String setName) {
        if (config == null) return;
        armorManager.getSetBonusRegistry().removeBonus(player, config, pieces, setName);
    }


    public void clearPlayerData(UUID uuid) {
        activeSetBonuses.remove(uuid);
    }

    /**
     * Saves the player's active set bonuses to the database.
     * Called on logout to persist the current state.
     */
    public void savePlayerSetBonuses(UUID uuid) {
        Map<String, SetBonusState> bonuses = activeSetBonuses.get(uuid);
        if (bonuses == null || bonuses.isEmpty()) {
            plugin.getPlayerDataManager().setActiveSetBonuses(uuid, null);
            return;
        }

        // Serialize active set bonuses as comma-separated "setName_pieces" strings
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, SetBonusState> entry : bonuses.entrySet()) {
            String setName = entry.getKey();
            SetBonusState state = entry.getValue();
            if (state.hasTwoPieceBonus()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(setName).append("_2pc");
            }
            if (state.hasFourPieceBonus()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(setName).append("_4pc");
            }
        }
        plugin.getPlayerDataManager().setActiveSetBonuses(uuid, sb.toString());
    }

    /**
     * Loads and re-applies set bonuses for a player based on their currently equipped armor.
     * Called on login to restore set bonuses.
     */
    public void restorePlayerSetBonuses(Player player) {
        // Load saved data (optional, for reference/debugging)
        String savedBonuses = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getActiveSetBonuses();
        if (savedBonuses != null && !savedBonuses.isEmpty()) {
            plugin.getLogger().info("[SetBonusManager] Restoring set bonuses for " + player.getName() + " (saved: " + savedBonuses + ")");
        }

        // Re-apply based on currently equipped armor
        plugin.getArmorEquipListener().scanAndEquipArmor(player);
    }

    /**
     * Inner class to track the state of set bonuses for a specific set.
     */
    private static class SetBonusState {
        private boolean twoPieceBonus = false;
        private boolean fourPieceBonus = false;
        private ConfigurationSection twoPieceConfig;
        private ConfigurationSection fourPieceConfig;

        public boolean hasTwoPieceBonus() {
            return twoPieceBonus;
        }

        public void setTwoPieceBonus(boolean twoPieceBonus) {
            this.twoPieceBonus = twoPieceBonus;
        }

        public boolean hasFourPieceBonus() {
            return fourPieceBonus;
        }

        public void setFourPieceBonus(boolean fourPieceBonus) {
            this.fourPieceBonus = fourPieceBonus;
        }

        public ConfigurationSection getTwoPieceConfig() {
            return twoPieceConfig;
        }

        public void setTwoPieceConfig(ConfigurationSection config) {
            this.twoPieceConfig = config;
        }

        public ConfigurationSection getFourPieceConfig() {
            return fourPieceConfig;
        }

        public void setFourPieceConfig(ConfigurationSection config) {
            this.fourPieceConfig = config;
        }
    }
}
