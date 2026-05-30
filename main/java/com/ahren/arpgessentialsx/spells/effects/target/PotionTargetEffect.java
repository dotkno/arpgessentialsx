package com.ahren.arpgessentialsx.spells.effects.target;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.PotionEffectUtil;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to the looked-at target.
 * Respects party membership - party members and their tamed pets only receive positive effects.
 *
 * yml params:
 *   effect: SLOWNESS   (Bukkit PotionEffectType name, default SLOWNESS)
 *   duration: 5.0      (seconds, default 5.0)
 *   amplifier: 0       (default 0)
 *   radius: 0.0        (if > 0, applies to all in radius instead of single target)
 */
public final class PotionTargetEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String effectName = ctx.getString("effect", "SLOWNESS").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int amp = ctx.getInt("amplifier", 0);
        double radius = ctx.getDouble("radius", 0.0);

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) {
            ctx.getPlugin().getLogger().warning("[PotionTargetEffect] Unknown effect: " + effectName);
            return;
        }

        PotionEffect effect = new PotionEffect(type, ticks, amp, false, true);

        // Determine if this is a positive effect (buff/heal) or negative (debuff/damage)
        boolean isPositive = PotionEffectUtil.isPositiveEffect(type);

        if (radius > 0) {
            for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
                if (e instanceof org.bukkit.entity.LivingEntity le && !e.equals(ctx.getCaster())) {
                    // Use TargetFilter to respect party membership and tamed pets
                    if (!TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) continue;
                    le.addPotionEffect(effect);
                }
            }
        } else if (ctx.hasTarget()) {
            // Use TargetFilter to respect party membership and tamed pets
            if (TargetFilter.shouldApplyEffect(ctx.getCaster(), ctx.getLookedAtTarget(), isPositive)) {
                ctx.getLookedAtTarget().addPotionEffect(effect);
            }
        }
    }
}