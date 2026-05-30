package com.ahren.arpgessentialsx.relics.effects.aoe;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

/**
 * Forces all nearby mobs to target the caster.
 * The Tank's signature aggro tool.
 *
 * yml params:
 *   radius: 12.0   (default 12.0)
 */
public final class TauntEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        double radius = ctx.getDouble("radius", 12.0);

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Mob mob) {
                mob.setTarget(ctx.getCaster());
            }
        }
    }
}
