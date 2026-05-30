package com.ahren.arpgessentialsx.spells.effects.target;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;

/** Sets the looked-at target on fire. yml: fire_ticks (default 100) */
public final class IgniteEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        ctx.getLookedAtTarget().setFireTicks(ctx.getInt("fire_ticks", 100));
    }
}