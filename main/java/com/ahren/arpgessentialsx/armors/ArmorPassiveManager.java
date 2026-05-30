package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.attributes.ClassAttributeApplier;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages armor passive tracking and application for players.
 * Optimized to only recalculate when armor changes, similar to SetBonusManager.
 */
public class ArmorPassiveManager {

    private final ARPGEssentialsX plugin;
    private final ArmorManager armorManager;

    // Track active passives per player
    private final Map<UUID, Map<String, PassiveState>> activePassives = new HashMap<>();

    public ArmorPassiveManager(ARPGEssentialsX plugin, ArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
    }

    /**
     * Updates armor passives based on currently equipped armor.
     * Only recalculates when armor changes for optimization.
     */
    public void updateArmorPassives(Player player, Map<ArmorType, Armor> equippedArmor) {
        UUID uuid = player.getUniqueId();
        Map<String, PassiveState> currentState = activePassives.computeIfAbsent(uuid, k -> new HashMap<>());

        // Collect all passives from equipped armor
        Map<String, PassiveInfo> equippedPassives = new HashMap<>();

        for (Armor armor : equippedArmor.values()) {
            for (int i = 0; i < armor.getPassives().size(); i++) {
                ConfigurationSection config = armor.getPassiveConfigs().get(i);
                String passiveKey = armor.getId() + "_" + i;
                equippedPassives.put(passiveKey, new PassiveInfo(armor, config, i));
            }
        }

        // Debug logging
        if (!equippedPassives.isEmpty()) {
            plugin.getLogger().info("[ArmorPassiveManager] Player " + player.getName() + " has " + equippedPassives.size() + " passives to apply:");
            for (Map.Entry<String, PassiveInfo> entry : equippedPassives.entrySet()) {
                String type = entry.getValue().config.getString("type", "unknown");
                plugin.getLogger().info("  - " + entry.getKey() + ": " + type);
            }
        }

        // Apply new passives
        for (Map.Entry<String, PassiveInfo> entry : equippedPassives.entrySet()) {
            String passiveKey = entry.getKey();
            PassiveInfo info = entry.getValue();

            if (!currentState.containsKey(passiveKey)) {
                // Apply the passive
                applyPassive(player, info.armor, info.config, info.index);
                PassiveState state = new PassiveState(true);
                state.setConfig(info.config);
                currentState.put(passiveKey, state);
            }
        }

        // Remove passives that are no longer equipped
        Iterator<Map.Entry<String, PassiveState>> iterator = currentState.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PassiveState> entry = iterator.next();
            String passiveKey = entry.getKey();

            if (!equippedPassives.containsKey(passiveKey)) {
                // Use stored config to remove the passive
                PassiveState state = entry.getValue();
                if (state.getConfig() != null) {
                    // Parse the passive key to get armor ID and index
                    String[] parts = passiveKey.split("_");
                    String armorId = parts[0];
                    int index = Integer.parseInt(parts[parts.length - 1]);

                    Armor armor = armorManager.getArmor(armorId);
                    if (armor != null) {
                        removePassive(player, armor, state.getConfig(), index);
                    }
                }
                iterator.remove();
            }
        }
    }

    private void applyPassive(Player player, Armor armor, ConfigurationSection config, int index) {
        if (config == null) return;
        String oldArmorId = config.getString("armor_id");
        Integer oldIndex = config.getInt("passive_index", -1);
        config.set("armor_id", armor.getId());
        config.set("passive_index", index);
        armorManager.getPassiveRegistry().applyPassive(player, config);
        config.set("armor_id", oldArmorId);
        config.set("passive_index", oldIndex);
    }

    private void removePassive(Player player, Armor armor, ConfigurationSection config, int index) {
        if (config == null) return;
        String oldArmorId = config.getString("armor_id");
        Integer oldIndex = config.getInt("passive_index", -1);
        config.set("armor_id", armor.getId());
        config.set("passive_index", index);
        armorManager.getPassiveRegistry().removePassive(player, config);
        config.set("armor_id", oldArmorId);
        config.set("passive_index", oldIndex);
    }

    public void clearPlayerData(UUID uuid) {
        activePassives.remove(uuid);
    }

    /**
     * Inner class to track the state of a passive.
     */
    private static class PassiveState {
        private boolean active;
        private ConfigurationSection config;

        public PassiveState(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        public ConfigurationSection getConfig() {
            return config;
        }

        public void setConfig(ConfigurationSection config) {
            this.config = config;
        }
    }

    /**
     * Inner class to hold passive information.
     */
    private static class PassiveInfo {
        private final Armor armor;
        private final ConfigurationSection config;
        private final int index;

        public PassiveInfo(Armor armor, ConfigurationSection config, int index) {
            this.armor = armor;
            this.config = config;
            this.index = index;
        }
    }
}
