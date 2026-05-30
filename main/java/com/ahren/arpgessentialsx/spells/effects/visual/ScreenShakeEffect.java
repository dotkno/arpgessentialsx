package com.ahren.arpgessentialsx.spells.effects.visual;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a brief nausea effect to simulate a screen shake for
 * all players in range. Respects party membership - allies are not affected.
 *
 * yml params:
 *   radius: 8.0          (players within this range feel the shake, default 8.0)
 *   duration: 1.0        (seconds of nausea, default 1.0)
 *   shake_caster: false  (whether caster also feels it, default false)
 */
public final class ScreenShakeEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 8.0);
        int ticks = (int)(ctx.getDouble("duration", 1.0) * 20);
        boolean shakeCaster = ctx.getBoolean("shake_caster", false);

        PotionEffect shake = new PotionEffect(PotionEffectType.NAUSEA, ticks, 0, false, false);

        // Nausea is a negative effect
        boolean isPositive = false;

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player p) {
                // Use TargetFilter to respect party membership
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                    p.addPotionEffect(shake);
                }
            }
        }

        if (shakeCaster) ctx.getCaster().addPotionEffect(shake);
    }
}