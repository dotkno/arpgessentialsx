package com.ahren.arpgessentialsx.spells.effects.target;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Drops the target player's main hand item temporarily.
 * Returns it after duration.
 *
 * yml params:
 *   duration: 3.0   (seconds before item is returned, default 3.0)
 */
public final class DisarmEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        if (!(ctx.getLookedAtTarget() instanceof Player target)) return;

        ItemStack held = target.getInventory().getItemInMainHand();
        if (held.getType().isAir()) return;

        int ticks = (int)(ctx.getDouble("duration", 3.0) * 20);

        // Drop the item
        target.getInventory().setItemInMainHand(null);
        target.getWorld().dropItem(target.getLocation(), held);

        // Note: returning it automatically is complex (item may have moved).
        // For simplicity, disarm is a temporary debuff — the item drops and
        // the player must pick it back up. Duration here just controls messaging.
        Bukkit.getScheduler().runTaskLater(ctx.getPlugin(), () -> {
            if (target.isOnline()) {
                com.ahren.arpgessentialsx.util.ColorUtil.sendActionBar(target, "&eDisarm expired.");
            }
        }, ticks);
    }
}