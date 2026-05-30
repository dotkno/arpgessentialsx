package com.ahren.arpgessentialsx.relics.effects.self;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;

/**
 * Grants absorption hearts to the caster.
 *
 * yml params:
 *   amount: 8.0   (half-hearts of absorption, default 8.0)
 */
public final class ShieldEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        double amount = ctx.getDouble("amount", 8.0);
        ctx.getCaster().setAbsorptionAmount(
                ctx.getCaster().getAbsorptionAmount() + amount);
    }
}