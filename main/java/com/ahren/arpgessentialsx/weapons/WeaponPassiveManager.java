package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manages weapon passive tracking and application for players.
 * Optimized to only recalculate when weapon changes, similar to SetBonusManager.
 */
public class WeaponPassiveManager {

    private final ARPGEssentialsX plugin;
    private final WeaponManager weaponManager;
    private final WeaponItemFactory weaponItemFactory;

    // Track active passives per player
    private final Map<UUID, Map<String, PassiveState>> activePassives = new HashMap<>();

    public WeaponPassiveManager(ARPGEssentialsX plugin, WeaponManager weaponManager, WeaponItemFactory weaponItemFactory) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.weaponItemFactory = weaponItemFactory;
    }

    /**
     * Updates weapon passives based on currently held weapon.
     * Only recalculates when weapon changes for optimization.
     */
    public void updateWeaponPassives(Player player, ItemStack heldItem) {
        UUID uuid = player.getUniqueId();
        Map<String, PassiveState> currentState = activePassives.computeIfAbsent(uuid, k -> new HashMap<>());

        // Get weapon from held item
        String weaponId = weaponItemFactory.getWeaponId(heldItem);
        Weapon weapon = weaponId != null ? weaponManager.getWeapon(weaponId) : null;

        // Collect all passives from held weapon
        Map<String, PassiveInfo> equippedPassives = new HashMap<>();

        if (weapon != null) {
            for (int i = 0; i < weapon.getPassives().size(); i++) {
                ConfigurationSection config = weapon.getPassiveConfigs().get(i);
                String passiveKey = weapon.getId() + "_" + i;
                equippedPassives.put(passiveKey, new PassiveInfo(weapon, config, i));
            }
        }

        // Apply new passives
        for (Map.Entry<String, PassiveInfo> entry : equippedPassives.entrySet()) {
            String passiveKey = entry.getKey();
            PassiveInfo info = entry.getValue();

            if (!currentState.containsKey(passiveKey)) {
                // Apply the passive
                applyPassive(player, info.weapon, info.config, info.index);
                currentState.put(passiveKey, new PassiveState(true));
            }
        }

        // Remove passives that are no longer equipped
        Iterator<Map.Entry<String, PassiveState>> iterator = currentState.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PassiveState> entry = iterator.next();
            String passiveKey = entry.getKey();

            if (!equippedPassives.containsKey(passiveKey)) {
                // Parse the passive key to get weapon ID and index
                String[] parts = passiveKey.split("_");
                String removedWeaponId = parts[0];
                int index = Integer.parseInt(parts[parts.length - 1]);

                Weapon removedWeapon = weaponManager.getWeapon(removedWeaponId);
                if (removedWeapon != null && index < removedWeapon.getPassiveConfigs().size()) {
                    ConfigurationSection config = removedWeapon.getPassiveConfigs().get(index);
                    removePassive(player, removedWeapon, config, index);
                }
                iterator.remove();
            }
        }
    }

    private void applyPassive(Player player, Weapon weapon, ConfigurationSection config, int index) {
        if (config == null) return;
        config.set("weapon_id", weapon.getId());
        config.set("passive_index", index);
        weaponManager.getPassiveRegistry().applyPassive(player, config);
    }

    private void removePassive(Player player, Weapon weapon, ConfigurationSection config, int index) {
        if (config == null) return;
        config.set("weapon_id", weapon.getId());
        config.set("passive_index", index);
        weaponManager.getPassiveRegistry().removePassive(player, config);
    }

    public void clearPlayerData(UUID uuid) {
        activePassives.remove(uuid);
    }

    /**
     * Inner class to track the state of a passive.
     */
    private static class PassiveState {
        private boolean active;

        public PassiveState(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }

    /**
     * Inner class to hold passive information.
     */
    private static class PassiveInfo {
        private final Weapon weapon;
        private final ConfigurationSection config;
        private final int index;

        public PassiveInfo(Weapon weapon, ConfigurationSection config, int index) {
            this.weapon = weapon;
            this.config = config;
            this.index = index;
        }
    }
}
