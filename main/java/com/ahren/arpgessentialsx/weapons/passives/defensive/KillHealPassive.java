package com.ahren.arpgessentialsx.weapons.passives.defensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Heals the player for a flat amount on each kill.
 *
 * yml params:
 *   amount: 4.0   (HP to restore on kill, default 4.0 = 2 hearts)
 *
 * Trigger: ON_KILL
 */
public final class KillHealPassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_KILL;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_KILL) return;

        double amount = ctx.getDouble("amount", 4.0);
        AttributeInstance maxHp = ctx.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double max = maxHp != null ? maxHp.getValue() : 20.0;
        double newHp = Math.min(ctx.getPlayer().getHealth() + amount, max);
        ctx.getPlayer().setHealth(newHp);
    }
}