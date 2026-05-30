package com.ahren.arpgessentialsx.spells.effects.crowd_control;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies powder-snow freeze effect to targets in a radius.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 4.0      (default 4.0)
 *   duration: 3.0    (seconds, default 3.0)
 */
public final class FreezeEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 4.0);
        int ticks = (int) (ctx.getDouble("duration", 3.0) * 20);

        // Freeze is a negative effect
        boolean isPositive = false;

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                    // Powder snow freeze: set freeze ticks directly
                    le.setFreezeTicks(ticks);
                    // Also slow them significantly
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 4, false, true));
                }
            }
        }
    }
}