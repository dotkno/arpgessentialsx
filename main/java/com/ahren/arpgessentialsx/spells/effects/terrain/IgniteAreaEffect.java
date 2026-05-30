package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Sets the ground on fire in a radius and ignites nearby entities.
 * Respects party membership - party members and their tamed pets are not ignited.
 *
 * yml params:
 *   radius: 4.0       (default 4.0)
 *   fire_ticks: 100   (entity fire duration in ticks, default 100)
 */
public final class IgniteAreaEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 4.0);
        int fireTicks = ctx.getInt("fire_ticks", 100);
        Location center = ctx.getCaster().getLocation();
        int r = (int) radius;

        // Place fire on solid blocks
        for (int x = -r; x <= r; x++)
            for (int z = -r; z <= r; z++) {
                if (x*x + z*z > r*r) continue;
                Location fl = center.clone().add(x, 0, z);
                if (fl.getBlock().getType().isAir() && fl.clone().subtract(0,1,0).getBlock().getType().isSolid())
                    fl.getBlock().setType(Material.FIRE);
            }

        // Ignite entities
        // Fire is a negative effect
        boolean isPositive = false;
        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity && !e.equals(ctx.getCaster())) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                    e.setFireTicks(fireTicks);
                }
            }
        }
    }
}