package com.ahren.arpgessentialsx.relics.effects.target;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Drops the looked-at target player's held item.
 *
 * yml params:
 *   (no params — effect is instant)
 */
public final class DisarmEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        if (!(ctx.getLookedAtTarget() instanceof Player target)) return;

        ItemStack held = target.getInventory().getItemInMainHand();
        if (held.getType().isAir()) return;

        target.getInventory().setItemInMainHand(null);
        target.getWorld().dropItem(target.getLocation(), held);
        ColorUtil.sendActionBar(target, "&cYou were disarmed!");
    }
}