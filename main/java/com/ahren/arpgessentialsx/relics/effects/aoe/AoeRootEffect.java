package com.ahren.arpgessentialsx.relics.effects.aoe;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Roots all nearby enemies in place using extreme slowness.
 * The mechanical backbone of caltrops.
 *
 * yml params:
 *   radius: 4.0      (default 4.0)
 *   duration: 2.5    (seconds, default 2.5)
 */
public final class AoeRootEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        double radius = ctx.getDouble("radius", 4.0);
        int ticks = (int)(ctx.getDouble("duration", 2.5) * 20);

        PotionEffect root = new PotionEffect(PotionEffectType.SLOWNESS, ticks, 255, false, false);

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                le.addPotionEffect(root);
            }
        }
    }
}