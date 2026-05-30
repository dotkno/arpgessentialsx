package com.ahren.arpgessentialsx.spells.effects.crowd_control;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Roots targets in place by applying extreme slowness.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 4.0     (default 4.0)
 *   duration: 3.0   (seconds, default 3.0)
 */
public final class RootEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 4.0);
        int ticks = (int)(ctx.getDouble("duration", 3.0) * 20);
        
        // Slowness is a negative effect
        boolean isPositive = false;
        
        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 255, false, false));
                }
            }
        }
    }
}