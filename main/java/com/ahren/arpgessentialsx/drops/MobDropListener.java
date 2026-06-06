package com.ahren.arpgessentialsx.drops;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class MobDropListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final DropManager dropManager;

    public MobDropListener(ARPGEssentialsX plugin, DropManager dropManager) {
        this.plugin = plugin;
        this.dropManager = dropManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();

        // Get drops for this mob type
        var drops = dropManager.getMobDrops(entityType);
        if (drops.isEmpty()) return;

        // Check if killed by player
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) entity;
        if (livingEntity.getKiller() == null || !(livingEntity.getKiller() instanceof Player)) {
            return;
        }

        // Roll for each drop
        for (DropManager.DropEntry drop : drops) {
            if (drop.rollChance()) {
                ItemStack dropItem = dropManager.createDropItem(drop.getItemId());
                if (dropItem != null) {
                    int amount = drop.rollAmount();
                    dropItem.setAmount(amount);
                    
                    // Drop the item at the entity's location
                    entity.getWorld().dropItemNaturally(entity.getLocation(), dropItem);
                    
                    plugin.getLogger().fine("[MobDropListener] " + entityType + " dropped " + drop.getItemId() + " x" + amount);
                }
            }
        }
    }
}
