package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;

/**
 * Multiplies hit damage by (1 + percent / 100).
 *
 * yml params:
 *   percent: 20.0   (e.g. 20.0 = +20% damage, default 10.0)
 *
 * Trigger: ON_HIT
 */
public final class PercentDamagePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;
        double percent = ctx.getDouble("percent", ctx.getDouble("bonus", 10.0));
        ctx.getEvent().setDamage(ctx.getEvent().getDamage() * (1.0 + percent / 100.0));
    }
}