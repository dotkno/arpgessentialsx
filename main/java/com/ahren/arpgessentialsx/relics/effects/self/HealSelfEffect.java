package com.ahren.arpgessentialsx.relics.effects.self;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Restores a flat amount of health to the caster.
 *
 * yml params:
 *   amount: 4.0   (half-hearts, default 4.0)
 */
public final class HealSelfEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        double amount = ctx.getDouble("amount", 4.0);
        AttributeInstance maxHp = ctx.getCaster().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double max = maxHp != null ? maxHp.getValue() : 20.0;
        ctx.getCaster().setHealth(Math.min(ctx.getCaster().getHealth() + amount, max));
    }
}