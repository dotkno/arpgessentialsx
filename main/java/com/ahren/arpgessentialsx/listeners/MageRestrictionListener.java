package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents Mages from using Enchanting Tables and combining enchantments on Anvils.
 * They CAN still use Anvils for Name Tags and repairing non-enchanted items.
 */
public final class MageRestrictionListener implements Listener {

    private final ARPGEssentialsX plugin;

    public MageRestrictionListener(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    /**
     * Blocks Mages from opening Enchanting Tables entirely.
     */
    @EventHandler
    public void onEnchantingTableInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ENCHANTING_TABLE) return;

        Player player = event.getPlayer();
        if (isMage(player)) {
            event.setCancelled(true);
            ColorUtil.sendActionBar(player, "&cMages cannot enchant!");
        }
    }

    /**
     * Blocks Mages from taking enchanted items out of Anvils.
     * Slot 2 is the result slot in an Anvil inventory.
     */
    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 2) return; // Slot 2 is the result slot

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isMage(player)) return;

        // Check if the item they are trying to take has enchantments
        if (event.getCurrentItem() != null && !event.getCurrentItem().getEnchantments().isEmpty()) {
            event.setCancelled(true);
            ColorUtil.sendActionBar(player, "&cMages cannot combine enchantments!");
        }
    }

    /**
     * Helper method to check if a player is a Mage.
     */
    private boolean isMage(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        return data != null && data.hasClass() && data.getClassId().equalsIgnoreCase("mage");
    }
}