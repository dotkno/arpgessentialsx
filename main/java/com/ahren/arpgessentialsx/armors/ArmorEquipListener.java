package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Handles armor equip/unequip events and manages set bonus and passive tracking.
 */
public final class ArmorEquipListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final ArmorManager armorManager;
    private final SetBonusManager setBonusManager;
    private final ArmorPassiveManager passiveManager;

    // Track equipped armor per player for set bonus calculation
    private final Map<UUID, Map<ArmorType, Armor>> equippedArmor = new HashMap<>();

    public ArmorEquipListener(ARPGEssentialsX plugin, ArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
        this.setBonusManager = new SetBonusManager(plugin, armorManager);
        this.passiveManager = new ArmorPassiveManager(plugin, armorManager);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        equippedArmor.put(player.getUniqueId(), new HashMap<>());
        
        // Scan current armor on join
        scanAndEquipArmor(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        equippedArmor.remove(uuid);
        passiveManager.clearPlayerData(uuid);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if the click involves armor slots or shift-clicking armor
        boolean isArmorSlot = event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.ARMOR;
        boolean isCraftingSlot = event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.CRAFTING;
        boolean isShiftClick = event.isShiftClick();
        boolean isArmorItem = isArmorItem(event.getCurrentItem()) || isArmorItem(event.getCursor());

        if (!isArmorSlot && !isCraftingSlot && !(isShiftClick && isArmorItem)) {
            return;
        }

        // Delay check to allow inventory to update
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            scanAndEquipArmor(player);
        }, 2L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!(event.getItem() != null && isArmorItem(event.getItem()))) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        String type = item.getType().name();
        PlayerInventory inv = player.getInventory();

        // Determine armor slot and equip
        if (type.endsWith("_HELMET")) {
            if (inv.getHelmet() == null || inv.getHelmet().getType().isAir()) {
                inv.setHelmet(item);
                inv.setItemInMainHand(null);
                event.setCancelled(true);
            }
        } else if (type.endsWith("_CHESTPLATE")) {
            if (inv.getChestplate() == null || inv.getChestplate().getType().isAir()) {
                inv.setChestplate(item);
                inv.setItemInMainHand(null);
                event.setCancelled(true);
            }
        } else if (type.endsWith("_LEGGINGS")) {
            if (inv.getLeggings() == null || inv.getLeggings().getType().isAir()) {
                inv.setLeggings(item);
                inv.setItemInMainHand(null);
                event.setCancelled(true);
            }
        } else if (type.endsWith("_BOOTS")) {
            if (inv.getBoots() == null || inv.getBoots().getType().isAir()) {
                inv.setBoots(item);
                inv.setItemInMainHand(null);
                event.setCancelled(true);
            }
        }

        // Scan for armor changes after equip
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            scanAndEquipArmor(player);
        }, 1L);
    }

    private boolean isArmorItem(org.bukkit.inventory.ItemStack item) {
        if (item == null) return false;
        String type = item.getType().name();
        return type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE") || 
               type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS");
    }

    public void scanAndEquipArmor(Player player) {
        UUID uuid = player.getUniqueId();
        Map<ArmorType, Armor> currentEquipped = new HashMap<>();

        // Check each armor slot
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        Map<ArmorType, Armor> previousEquipped = equippedArmor.getOrDefault(uuid, new HashMap<>());

        // Process each slot
        processArmorSlot(player, uuid, helmet, ArmorType.HEAD, previousEquipped, currentEquipped);
        processArmorSlot(player, uuid, chestplate, ArmorType.CHEST, previousEquipped, currentEquipped);
        processArmorSlot(player, uuid, leggings, ArmorType.LEGS, previousEquipped, currentEquipped);
        processArmorSlot(player, uuid, boots, ArmorType.FEET, previousEquipped, currentEquipped);


        // Update equipped armor map
        equippedArmor.put(uuid, currentEquipped);

        // Update set bonuses
        setBonusManager.updateSetBonuses(player, currentEquipped);

        // Update armor passives
        passiveManager.updateArmorPassives(player, currentEquipped);
    }

    private void processArmorSlot(Player player, UUID uuid, ItemStack item, ArmorType type,
                                   Map<ArmorType, Armor> previousEquipped, Map<ArmorType, Armor> currentEquipped) {
        Armor previousArmor = previousEquipped.get(type);
        Armor newArmor = getArmorFromItem(item);

        // Unequip previous armor
        if (previousArmor != null && (newArmor == null || !previousArmor.getId().equals(newArmor.getId()))) {
            triggerUnequip(player, previousArmor);
        }

        // Equip new armor
        if (newArmor != null && (previousArmor == null || !previousArmor.getId().equals(newArmor.getId()))) {
            currentEquipped.put(type, newArmor);
            triggerEquip(player, newArmor);
        } else if (newArmor != null) {
            currentEquipped.put(type, newArmor);
        }
    }

    private Armor getArmorFromItem(ItemStack item) {
        if (item == null) return null;
        String armorId = armorManager.getItemFactory().getArmorId(item);
        if (armorId == null) return null;
        return armorManager.getArmor(armorId);
    }

    private void triggerEquip(Player player, Armor armor) {
        for (ArmorPassive passive : armor.getPassives()) {
            if (passive.getTrigger() == ArmorPassive.Trigger.ON_EQUIP) {
                ArmorPassiveContext ctx = ArmorPassiveContext.forEquip(player, armor);
                passive.apply(ctx);
            }
        }
    }

    private void triggerUnequip(Player player, Armor armor) {
        for (ArmorPassive passive : armor.getPassives()) {
            if (passive.getTrigger() == ArmorPassive.Trigger.ON_UNEQUIP) {
                ArmorPassiveContext ctx = ArmorPassiveContext.forUnequip(player, armor);
                passive.apply(ctx);
            }
        }
    }

    public Map<ArmorType, Armor> getEquippedArmor(Player player) {
        return equippedArmor.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public SetBonusManager getSetBonusManager() {
        return setBonusManager;
    }
}
