package com.ahren.arpgessentialsx.drops;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class MiningDropListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final DropManager dropManager;

    public MiningDropListener(ARPGEssentialsX plugin, DropManager dropManager) {
        this.plugin = plugin;
        this.dropManager = dropManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        // Only process if not cancelled
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Material material = block.getType();

        // Get drops for this block type
        var drops = dropManager.getMiningDrops(material);
        if (drops.isEmpty()) return;

        // Check if broken by player
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();

        // Roll for each drop
        for (DropManager.DropEntry drop : drops) {
            if (drop.rollChance()) {
                ItemStack dropItem = dropManager.createDropItem(drop.getItemId());
                if (dropItem != null) {
                    int amount = drop.rollAmount();
                    dropItem.setAmount(amount);
                    
                    // Drop the item at the block's location
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), dropItem);
                    
                    plugin.getLogger().fine("[MiningDropListener] " + player.getName() + " mined " + material + " and got " + drop.getItemId() + " x" + amount);
                }
            }
        }
    }
}
