package com.ahren.arpgessentialsx.relics.effects.aoe;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Deals flat damage to all living entities in a radius around the caster.
 *
 * yml params:
 *   radius: 5.0    (default 5.0)
 *   damage: 4.0    (default 4.0)
 */
public final class AoeDamageEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        double radius = ctx.getDouble("radius", 5.0);
        double damage = ctx.getDouble("damage", 4.0);

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                le.damage(damage, ctx.getCaster());
            }
        }
    }
}