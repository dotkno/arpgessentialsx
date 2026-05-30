package com.ahren.arpgessentialsx.relics.effects.aoe;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Pushes all nearby entities away from the caster.
 *
 * yml params:
 *   radius: 5.0      (default 5.0)
 *   strength: 1.5    (knockback power, default 1.5)
 *   vertical: 0.3    (upward component, default 0.3)
 */
public final class AoeKnockbackEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        double radius = ctx.getDouble("radius", 5.0);
        double strength = ctx.getDouble("strength", 1.5);
        double vertical = ctx.getDouble("vertical", 0.3);

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity) || e.equals(ctx.getCaster())) continue;

            Vector dir = e.getLocation().toVector()
                    .subtract(ctx.getCaster().getLocation().toVector())
                    .normalize()
                    .multiply(strength);
            dir.setY(vertical);
            e.setVelocity(dir);
        }
    }
}