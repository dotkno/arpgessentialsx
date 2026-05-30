package com.ahren.arpgessentialsx.relics.effects.aoe;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import com.ahren.arpgessentialsx.util.PotionEffectUtil;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to all nearby entities.
 * Respects party membership - party members and their tamed pets only receive positive effects.
 * The workhorse behind smoke_bomb, caltrops, battle_cry debuffs, etc.
 *
 * yml params:
 *   effect: SLOWNESS   (Bukkit PotionEffectType name, default SLOWNESS)
 *   duration: 4.0      (seconds, default 4.0)
 *   amplifier: 0       (default 0)
 *   radius: 5.0        (default 5.0)
 *   affect_caster: false (whether to apply to caster too, default false)
 */
public final class AoePotionEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        String effectName = ctx.getString("effect", "SLOWNESS").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 4.0) * 20);
        int amp = ctx.getInt("amplifier", 0);
        double radius = ctx.getDouble("radius", 5.0);
        boolean affectCaster = ctx.getBoolean("affect_caster", false);

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) {
            ctx.getPlugin().getLogger().warning("[AoePotionEffect] Unknown effect: " + effectName);
            return;
        }

        PotionEffect effect = new PotionEffect(type, ticks, amp, false, true);

        // Determine if this is a positive effect (buff/heal) or negative (debuff/damage)
        boolean isPositive = PotionEffectUtil.isPositiveEffect(type);

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le) {
                if (e.equals(ctx.getCaster()) && !affectCaster) continue;

                // Use TargetFilter to respect party membership and tamed pets
                if (ctx.getCaster() instanceof Player caster) {
                    if (!TargetFilter.shouldApplyEffect(caster, e, isPositive)) continue;
                }

                le.addPotionEffect(effect);
            }
        }
    }
}