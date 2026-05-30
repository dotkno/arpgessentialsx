package com.ahren.arpgessentialsx.spells.effects.crowd_control;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Applies nausea to all targets in a radius. Respects party membership - allies are not affected. yml: radius, duration */
public final class ConfuseEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 5.0);
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        
        // Nausea is a negative effect
        boolean isPositive = false;
        
        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, ticks, 0, false, true));
                }
            }
        }
    }
}