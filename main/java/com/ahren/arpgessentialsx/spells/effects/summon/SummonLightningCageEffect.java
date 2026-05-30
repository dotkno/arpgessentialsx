package com.ahren.arpgessentialsx.spells.effects.summon;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Location;

/**
 * Strikes a ring of lightning around the target, creating a cage effect.
 *
 * yml params:
 *   radius: 3.0     (ring radius, default 3.0)
 *   count: 8        (number of lightning strikes in the ring, default 8)
 *   damage: true    (whether strikes deal damage, default true)
 */
public final class SummonLightningCageEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 3.0);
        int count = ctx.getInt("count", 8);
        boolean damage = ctx.getBoolean("damage", true);

        Location center = ctx.hasTarget()
                ? ctx.getLookedAtTarget().getLocation()
                : ctx.getCaster().getLocation();

        double angleStep = (2 * Math.PI) / count;
        for (int i = 0; i < count; i++) {
            double angle = i * angleStep;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location strikePos = new Location(center.getWorld(), x, center.getY(), z);

            if (damage) center.getWorld().strikeLightning(strikePos);
            else center.getWorld().strikeLightningEffect(strikePos);
        }
    }
}