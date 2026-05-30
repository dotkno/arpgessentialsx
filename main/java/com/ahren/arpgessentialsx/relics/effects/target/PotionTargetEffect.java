package com.ahren.arpgessentialsx.relics.effects.target;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to the looked-at target.
 *
 * yml params:
 *   effect: WEAKNESS   (Bukkit PotionEffectType name, default WEAKNESS)
 *   duration: 5.0      (seconds, default 5.0)
 *   amplifier: 0       (default 0)
 */
public final class PotionTargetEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        if (!ctx.hasTarget()) return;

        String effectName = ctx.getString("effect", "WEAKNESS").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int amp = ctx.getInt("amplifier", 0);

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) {
            ctx.getPlugin().getLogger().warning("[PotionTargetEffect] Unknown effect: " + effectName);
            return;
        }

        ctx.getLookedAtTarget().addPotionEffect(
                new PotionEffect(type, ticks, amp, false, true));
    }
}