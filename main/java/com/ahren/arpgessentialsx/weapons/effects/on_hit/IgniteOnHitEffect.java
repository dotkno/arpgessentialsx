package com.ahren.arpgessentialsx.weapons.effects.on_hit;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;

/**
 * Sets the hit target on fire.
 *
 * yml params:
 *   fire_ticks: 80   (default 80 = 4 seconds)
 */
public final class IgniteOnHitEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        ctx.getTarget().setFireTicks(ctx.getInt("fire_ticks", 80));
    }
}