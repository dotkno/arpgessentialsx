package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * Handles weapon equip/unequip events and manages passive tracking.
 */
public class WeaponEquipListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final WeaponManager weaponManager;
    private final WeaponPassiveManager passiveManager;
    private final WeaponItemFactory weaponItemFactory;

    // Track currently held weapon per player
    private final Map<UUID, String> heldWeapons = new java.util.HashMap<>();

    public WeaponEquipListener(ARPGEssentialsX plugin, WeaponManager weaponManager, WeaponItemFactory weaponItemFactory) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.weaponItemFactory = weaponItemFactory;
        this.passiveManager = new WeaponPassiveManager(plugin, weaponManager, weaponItemFactory);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        String weaponId = weaponItemFactory.getWeaponId(held);
        heldWeapons.put(player.getUniqueId(), weaponId);
        
        // Update weapon passives for current weapon
        passiveManager.updateWeaponPassives(player, held);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        heldWeapons.remove(uuid);
        passiveManager.clearPlayerData(uuid);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        String newWeaponId = weaponItemFactory.getWeaponId(newItem);
        String previousWeaponId = heldWeapons.get(uuid);

        // Only update if weapon changed
        if (previousWeaponId == null ? newWeaponId != null : !previousWeaponId.equals(newWeaponId)) {
            heldWeapons.put(uuid, newWeaponId);
            passiveManager.updateWeaponPassives(player, newItem);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Check if the click involves the hotbar
        if (event.getSlotType() != org.bukkit.event.inventory.InventoryType.SlotType.QUICKBAR) {
            return;
        }

        // Delay check to allow inventory to update
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ItemStack held = player.getInventory().getItemInMainHand();
            UUID uuid = player.getUniqueId();
            String newWeaponId = weaponItemFactory.getWeaponId(held);
            String previousWeaponId = heldWeapons.get(uuid);

            // Only update if weapon changed
            if (previousWeaponId == null ? newWeaponId != null : !previousWeaponId.equals(newWeaponId)) {
                heldWeapons.put(uuid, newWeaponId);
                passiveManager.updateWeaponPassives(player, held);
            }
        }, 1L);
    }

    public WeaponPassiveManager getPassiveManager() {
        return passiveManager;
    }
}
