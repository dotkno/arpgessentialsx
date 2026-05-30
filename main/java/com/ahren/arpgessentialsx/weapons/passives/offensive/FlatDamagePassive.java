package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;

/**
 * Adds a flat amount to the hit's damage.
 *
 * yml params:
 *   amount: 2.0   (flat damage to add, default 2.0)
 *
 * Trigger: ON_HIT
 */
public final class FlatDamagePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;
        double amount = ctx.getDouble("amount", 2.0);
        ctx.getEvent().setDamage(ctx.getEvent().getDamage() + amount);
    }
}