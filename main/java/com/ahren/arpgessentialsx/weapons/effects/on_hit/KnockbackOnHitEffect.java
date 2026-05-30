package com.ahren.arpgessentialsx.weapons.effects.on_hit;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.util.Vector;

/**
 * Applies extra knockback to the hit target.
 *
 * yml params:
 *   strength: 1.5    (default 1.5)
 *   vertical: 0.2    (upward component, default 0.2)
 */
public final class KnockbackOnHitEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        double strength = ctx.getDouble("strength", 1.5);
        double vertical = ctx.getDouble("vertical", 0.2);

        Vector dir = ctx.getTarget().getLocation().toVector()
                .subtract(ctx.getAttacker().getLocation().toVector())
                .normalize()
                .multiply(strength);
        dir.setY(vertical);
        ctx.getTarget().setVelocity(dir);
    }
}