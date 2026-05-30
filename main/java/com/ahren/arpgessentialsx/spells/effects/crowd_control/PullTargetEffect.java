package com.ahren.arpgessentialsx.spells.effects.crowd_control;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.util.Vector;

/**
 * Yanks the looked-at target toward the caster.
 *
 * yml params:
 *   power: 2.0    (default 2.0)
 */
public final class PullTargetEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        double power = ctx.getDouble("power", 2.0);
        Vector pull = ctx.getCaster().getLocation().toVector()
                .subtract(ctx.getLookedAtTarget().getLocation().toVector())
                .normalize().multiply(power);
        ctx.getLookedAtTarget().setVelocity(pull);
    }
}