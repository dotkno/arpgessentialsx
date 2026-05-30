package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/** Restores health to the caster. yml: amount (half-hearts, default 4.0) */
public final class HealSelfEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double amount = ctx.getDouble("amount", 4.0);
        AttributeInstance maxHp = ctx.getCaster().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double max = maxHp != null ? maxHp.getValue() : 20.0;
        ctx.getCaster().setHealth(Math.min(ctx.getCaster().getHealth() + amount, max));
    }
}